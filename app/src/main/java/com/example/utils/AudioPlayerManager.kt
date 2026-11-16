package com.example.utils

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.audiofx.LoudnessEnhancer
import android.net.Uri
import android.os.PowerManager
import com.example.data.models.Reciter
import com.example.data.models.Surah
import com.example.data.sources.QuranDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class PlaybackState {
    IDLE,
    BUFFERING,
    PLAYING,
    PAUSED,
    ERROR
}

data class AudioPlayerUiState(
    val playbackState: PlaybackState = PlaybackState.IDLE,
    val currentSurah: Surah? = null,
    val currentReciter: Reciter = QuranDataSource.reciters[0],
    val currentPositionMs: Int = 0,
    val durationMs: Int = 0,
    val errorMessage: String? = null,
    val volumeGainMb: Int = 1200, // +12 dB boost by default for crystal clear loud recitation
    val isBoostEnabled: Boolean = true,
    val isNormalizationEnabled: Boolean = true, // Audio Normalization across reciters
    val audioBitrateKbps: Int = 192, // High bitrate for pristine sound quality (192 kbps)
    val playerVolume: Float = 1.0f, // Max volume by default
    val isUsingFallbackServer: Boolean = false
)

class AudioPlayerManager(private val context: Context) {

    private var mediaPlayer: MediaPlayer? = null
    private var loudnessEnhancer: LoudnessEnhancer? = null
    private val playerScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var progressJob: Job? = null

    private val _uiState = MutableStateFlow(AudioPlayerUiState())
    val uiState: StateFlow<AudioPlayerUiState> = _uiState.asStateFlow()

    private fun applyAudioBoost(audioSessionId: Int) {
        try {
            loudnessEnhancer?.release()
            loudnessEnhancer = null

            // Apply normalization and loudness enhancement
            if ((_uiState.value.isBoostEnabled || _uiState.value.isNormalizationEnabled) && _uiState.value.volumeGainMb > 0) {
                loudnessEnhancer = LoudnessEnhancer(audioSessionId).apply {
                    val targetGain = if (_uiState.value.isNormalizationEnabled) {
                        _uiState.value.volumeGainMb.coerceAtLeast(800) // Ensure baseline normalized level
                    } else {
                        _uiState.value.volumeGainMb
                    }
                    setTargetGain(targetGain)
                    enabled = true
                }
            }
        } catch (_: Exception) {
            // Safe fallback if device audiofx is unavailable
        }
    }

    /**
     * رفع مستوى صوت الوسائط في النظام إلى الحد الأقصى أو المرتفع جداً
     */
    fun maximizeSystemVolume() {
        try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager ?: return
            val maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            val currentVol = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
            if (currentVol < (maxVol * 0.85).toInt()) {
                audioManager.setStreamVolume(
                    AudioManager.STREAM_MUSIC,
                    (maxVol * 0.90).toInt(),
                    AudioManager.FLAG_SHOW_UI
                )
            }
        } catch (_: Exception) {}
    }

    private fun requestAudioFocus() {
        try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager ?: return
            audioManager.requestAudioFocus(
                null,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
            )
        } catch (_: Exception) {}
    }

    fun setVolumeBoostLevel(gainMb: Int) {
        val clampedGain = gainMb.coerceIn(0, 2000)
        _uiState.value = _uiState.value.copy(
            volumeGainMb = clampedGain,
            isBoostEnabled = clampedGain > 0
        )
        try {
            loudnessEnhancer?.setTargetGain(clampedGain)
            loudnessEnhancer?.enabled = clampedGain > 0
        } catch (_: Exception) {}
    }

    fun setAudioBitrate(bitrate: Int) {
        _uiState.value = _uiState.value.copy(audioBitrateKbps = bitrate)
    }

    fun setNormalizationEnabled(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(isNormalizationEnabled = enabled)
        try {
            mediaPlayer?.audioSessionId?.let { applyAudioBoost(it) }
        } catch (_: Exception) {}
    }

    fun setPlayerVolume(volume: Float) {
        val clamped = volume.coerceIn(0.0f, 1.0f)
        _uiState.value = _uiState.value.copy(playerVolume = clamped)
        try {
            mediaPlayer?.setVolume(clamped, clamped)
        } catch (_: Exception) {}
    }

    fun selectReciter(reciter: Reciter) {
        val wasPlaying = _uiState.value.playbackState == PlaybackState.PLAYING
        val currentSurah = _uiState.value.currentSurah
        _uiState.value = _uiState.value.copy(currentReciter = reciter)
        if (wasPlaying && currentSurah != null) {
            playSurah(currentSurah, reciter)
        }
    }

    fun playSurah(surah: Surah, reciter: Reciter = _uiState.value.currentReciter) {
        playInternal(surah, reciter, useFallback = false)
    }

    private fun playInternal(surah: Surah, reciter: Reciter, useFallback: Boolean) {
        try {
            stop(resetPosition = false)
            _uiState.value = _uiState.value.copy(
                currentSurah = surah,
                currentReciter = reciter,
                playbackState = PlaybackState.BUFFERING,
                errorMessage = null,
                isUsingFallbackServer = useFallback
            )

            // Auto-check and boost system media volume so recitation is heard loud & clear
            maximizeSystemVolume()
            requestAudioFocus()

            val url = QuranDataSource.getAudioUrl(reciter, surah.id, useFallback = useFallback)

            mediaPlayer = MediaPlayer().apply {
                // Background playback wake lock to avoid CPU sleep killing playback
                try {
                    setWakeMode(context, PowerManager.PARTIAL_WAKE_LOCK)
                } catch (_: Exception) {}

                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )

                // Pass browser headers for maximum CDN compatibility and prevent 403 blocks
                val uri = Uri.parse(url)
                val headers = mapOf(
                    "User-Agent" to "Mozilla/5.0 (Linux; Android 14; Mobile) SakinaQuranApp/2.0",
                    "Accept" to "*/*"
                )
                setDataSource(context, uri, headers)

                // Default player volume is maximum (1.0f)
                val currentVol = _uiState.value.playerVolume.coerceIn(0.1f, 1.0f)
                setVolume(currentVol, currentVol)

                setOnPreparedListener { mp ->
                    applyAudioBoost(mp.audioSessionId)
                    mp.start()
                    _uiState.value = _uiState.value.copy(
                        playbackState = PlaybackState.PLAYING,
                        durationMs = mp.duration
                    )
                    startProgressTracker()
                }

                setOnCompletionListener {
                    stopProgressTracker()
                    _uiState.value = _uiState.value.copy(
                        playbackState = PlaybackState.IDLE,
                        currentPositionMs = 0
                    )
                    // Auto-advance to next Surah if available
                    playNextSurah()
                }

                setOnErrorListener { _, what, extra ->
                    stopProgressTracker()
                    if (!useFallback && reciter.fallbackBaseUrl != null) {
                        // Automatically switch to backup high-availability CDN
                        playInternal(surah, reciter, useFallback = true)
                    } else {
                        _uiState.value = _uiState.value.copy(
                            playbackState = PlaybackState.ERROR,
                            errorMessage = "تعذر تشغيل تلاوة ${surah.nameArabic} للقارئ ${reciter.nameArabic}. يرجى التحقق من اتصال الإنترنت."
                        )
                    }
                    true
                }

                prepareAsync()
            }
        } catch (e: Exception) {
            stopProgressTracker()
            if (!useFallback && reciter.fallbackBaseUrl != null) {
                playInternal(surah, reciter, useFallback = true)
            } else {
                _uiState.value = _uiState.value.copy(
                    playbackState = PlaybackState.ERROR,
                    errorMessage = e.localizedMessage ?: "حدث خطأ أثناء تشغيل التلاوة"
                )
            }
        }
    }

    private fun startProgressTracker() {
        stopProgressTracker()
        progressJob = playerScope.launch {
            while (isActive) {
                val mp = mediaPlayer
                if (mp != null && _uiState.value.playbackState == PlaybackState.PLAYING) {
                    try {
                        val pos = mp.currentPosition
                        val dur = mp.duration
                        _uiState.value = _uiState.value.copy(
                            currentPositionMs = pos,
                            durationMs = if (dur > 0) dur else _uiState.value.durationMs
                        )
                    } catch (_: Exception) {}
                }
                delay(500)
            }
        }
    }

    private fun stopProgressTracker() {
        progressJob?.cancel()
        progressJob = null
    }

    fun seekTo(positionMs: Int) {
        try {
            val mp = mediaPlayer ?: return
            val clamped = positionMs.coerceIn(0, mp.duration)
            mp.seekTo(clamped)
            _uiState.value = _uiState.value.copy(currentPositionMs = clamped)
        } catch (_: Exception) {}
    }

    fun playNextSurah() {
        val current = _uiState.value.currentSurah ?: return
        val nextId = if (current.id >= 114) 1 else current.id + 1
        val nextSurah = QuranDataSource.surahs.find { it.id == nextId } ?: return
        playSurah(nextSurah)
    }

    fun playPreviousSurah() {
        val current = _uiState.value.currentSurah ?: return
        val prevId = if (current.id <= 1) 114 else current.id - 1
        val prevSurah = QuranDataSource.surahs.find { it.id == prevId } ?: return
        playSurah(prevSurah)
    }

    fun togglePlayPause() {
        val mp = mediaPlayer
        val currentSurah = _uiState.value.currentSurah ?: return

        when (_uiState.value.playbackState) {
            PlaybackState.PLAYING -> {
                try {
                    mp?.pause()
                } catch (_: Exception) {}
                stopProgressTracker()
                _uiState.value = _uiState.value.copy(playbackState = PlaybackState.PAUSED)
            }
            PlaybackState.PAUSED -> {
                requestAudioFocus()
                try {
                    mp?.start()
                } catch (_: Exception) {}
                _uiState.value = _uiState.value.copy(playbackState = PlaybackState.PLAYING)
                startProgressTracker()
            }
            PlaybackState.IDLE, PlaybackState.ERROR -> {
                playSurah(currentSurah)
            }
            else -> Unit
        }
    }

    fun stop(resetPosition: Boolean = true) {
        stopProgressTracker()
        try {
            loudnessEnhancer?.release()
        } catch (_: Exception) {}
        loudnessEnhancer = null

        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (_: Exception) {}
        mediaPlayer = null

        _uiState.value = _uiState.value.copy(
            playbackState = PlaybackState.IDLE,
            currentPositionMs = if (resetPosition) 0 else _uiState.value.currentPositionMs
        )
    }

    fun release() {
        stop(resetPosition = true)
    }
}
