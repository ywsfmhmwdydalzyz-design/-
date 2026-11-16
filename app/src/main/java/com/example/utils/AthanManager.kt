package com.example.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.R
import com.example.data.models.MuadhinVoice
import com.example.utils.PrayerTimeCalculator.muadhinVoices
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AthanManager(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("sakina_athan_prefs", Context.MODE_PRIVATE)

    private val _isAutoAthanEnabled = MutableStateFlow(prefs.getBoolean("auto_athan_enabled", true))
    val isAutoAthanEnabled: StateFlow<Boolean> = _isAutoAthanEnabled.asStateFlow()

    private val _selectedVoiceId = MutableStateFlow(
        prefs.getString("selected_voice_id", null) ?: "islam_sobhi"
    )
    val selectedVoiceId: StateFlow<String> = _selectedVoiceId.asStateFlow()

    private val _isPlayingAthan = MutableStateFlow(false)
    val isPlayingAthan: StateFlow<Boolean> = _isPlayingAthan.asStateFlow()

    private var mediaPlayer: MediaPlayer? = null

    companion object {
        const val CHANNEL_ID = "sakina_prayer_notifications"
        const val CHANNEL_NAME = "مواقيت الصلاة والأذكار"
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "إشعارات مواقيت الصلاة والأذان وتذكيرات الأذكار اليومية"
                enableVibration(true)
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    fun setAutoAthanEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("auto_athan_enabled", enabled).apply()
        _isAutoAthanEnabled.value = enabled
    }

    fun selectVoice(voiceId: String) {
        prefs.edit().putString("selected_voice_id", voiceId).apply()
        _selectedVoiceId.value = voiceId
    }

    fun getSelectedVoice(): MuadhinVoice {
        return muadhinVoices.find { it.id == _selectedVoiceId.value } ?: muadhinVoices[0]
    }

    fun playAthan(voice: MuadhinVoice = getSelectedVoice()) {
        stopAthan()
        _isPlayingAthan.value = true

        try {
            val rawRes = if (voice.id == "egypt" || voice.id == "custom") {
                R.raw.my_custom_azan
            } else {
                R.raw.azan
            }
            mediaPlayer = MediaPlayer.create(context, rawRes)?.apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .build()
                )
                setOnCompletionListener {
                    stopAthan()
                }
                setOnErrorListener { _, _, _ ->
                    stopAthan()
                    true
                }
                start()
            }
            if (mediaPlayer == null) {
                _isPlayingAthan.value = false
            }
        } catch (_: Exception) {
            stopAthan()
        }
    }

    fun stopAthan() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (_: Exception) {}
        mediaPlayer = null
        _isPlayingAthan.value = false
    }

    fun sendPrayerNotification(prayerName: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("حان الآن موعد أذان $prayerName")
            .setContentText("حي على الصلاة، حي على الفلاح.. أقبل على ربك بسكينة وطمأنينة.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(101, notification)
        } catch (_: SecurityException) {}
    }

    fun sendReminderNotification(title: String, message: String) {
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context, (System.currentTimeMillis() % 10000).toInt(), intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(
                (System.currentTimeMillis() % 10000).toInt(),
                notification
            )
        } catch (_: SecurityException) {}
    }
}
