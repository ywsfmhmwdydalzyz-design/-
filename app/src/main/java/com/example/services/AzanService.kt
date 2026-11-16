package com.example.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.R
import com.example.ui.screens.AzanActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * حامل حالة الأذان التفاعلية على مستوى التطبيق لمزامنة الواجهات وإغلاقها تلقائياً
 */
object AzanStateHolder {
    val isPlaying = MutableStateFlow(false)
    val currentPrayer = MutableStateFlow("الصلاة")
    val playbackProgress = MutableStateFlow(0f)
    val remainingSeconds = MutableStateFlow(173)
    val isCompleted = MutableStateFlow(false)

    fun start(prayer: String, totalSeconds: Int = 173) {
        currentPrayer.value = prayer
        isPlaying.value = true
        isCompleted.value = false
        playbackProgress.value = 0f
        remainingSeconds.value = totalSeconds
    }

    fun updateProgress(currentMs: Int, totalMs: Int) {
        if (totalMs > 0) {
            playbackProgress.value = (currentMs.toFloat() / totalMs).coerceIn(0f, 1f)
            val left = ((totalMs - currentMs) / 1000).coerceAtLeast(0)
            remainingSeconds.value = left
        }
    }

    fun markCompleted() {
        isPlaying.value = false
        playbackProgress.value = 1f
        remainingSeconds.value = 0
        isCompleted.value = true
    }
}

class AzanService : Service() {

    companion object {
        const val ACTION_START_AZAN = "com.example.ACTION_START_AZAN"
        const val ACTION_STOP_AZAN = "com.example.ACTION_STOP_AZAN"
        const val ACTION_AZAN_COMPLETED = "com.example.ACTION_AZAN_COMPLETED"
        const val CHANNEL_ID = "AZAN_FULLSCREEN_CHANNEL"
        const val NOTIFICATION_ID = 9001
    }

    private var mediaPlayer: MediaPlayer? = null
    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private var progressTrackingJob: Job? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP_AZAN) {
            stopAzan()
            return START_NOT_STICKY
        }

        val prayerName = intent?.getStringExtra("PRAYER_NAME") ?: "الصلاة"

        com.example.utils.LocationPrayerManager.ensureInitialized(this)
        createNotificationChannel()

        // نية الشاشة الكاملة Full Screen Intent
        val fullScreenIntent = Intent(this, AzanActivity::class.java).apply {
            setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra("PRAYER_NAME", prayerName)
        }
        val fullScreenPendingIntent = PendingIntent.getActivity(
            this,
            1001,
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // نية زر إيقاف الأذان الفوري من شريط الإشعارات
        val stopIntent = Intent(this, AzanService::class.java).apply {
            action = ACTION_STOP_AZAN
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            1002,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("حان الآن وقت صلاة $prayerName")
            .setContentText("الله أكبر .. الله أكبر (الشيخ إسلام صبحي)")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(true)
            .setAutoCancel(false)
            .setContentIntent(fullScreenPendingIntent)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .addAction(R.drawable.ic_launcher_foreground, "⏹ إيقاف الأذان", stopPendingIntent)
            .build()

        startForeground(NOTIFICATION_ID, notification)

        // تشغيل صوت الأذان العذب بصوت الشيخ إسلام صبحي
        playAzanSound(prayerName)

        return START_NOT_STICKY
    }

    private fun playAzanSound(prayerName: String) {
        try {
            stopProgressTracking()
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null

            AzanStateHolder.start(prayerName, 173)

            mediaPlayer = MediaPlayer.create(this, R.raw.azan).apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .build()
                )
                start()
                val totalDurationMs = duration
                if (totalDurationMs > 0) {
                    AzanStateHolder.remainingSeconds.value = totalDurationMs / 1000
                }

                setOnCompletionListener {
                    // إغلاق شاشة الأذان تلقائياً فور اكتمال الصوت بالكامل
                    AzanStateHolder.markCompleted()
                    val completedIntent = Intent(ACTION_AZAN_COMPLETED).apply {
                        setPackage(packageName)
                    }
                    sendBroadcast(completedIntent)
                    stopAzan()
                }

                setOnErrorListener { _, _, _ ->
                    AzanStateHolder.markCompleted()
                    stopAzan()
                    true
                }
            }

            startProgressTracking()
        } catch (e: Exception) {
            e.printStackTrace()
            AzanStateHolder.markCompleted()
            stopAzan()
        }
    }

    private fun startProgressTracking() {
        stopProgressTracking()
        progressTrackingJob = serviceScope.launch {
            while (isActive) {
                try {
                    val player = mediaPlayer
                    if (player != null && player.isPlaying) {
                        val currentMs = player.currentPosition
                        val totalMs = player.duration
                        AzanStateHolder.updateProgress(currentMs, totalMs)
                    } else if (player != null && !player.isPlaying && AzanStateHolder.isPlaying.value) {
                        // انتهى التشغيل
                        AzanStateHolder.markCompleted()
                        break
                    }
                } catch (_: Exception) {}
                delay(500)
            }
        }
    }

    private fun stopProgressTracking() {
        progressTrackingJob?.cancel()
        progressTrackingJob = null
    }

    private fun stopAzan() {
        try {
            stopProgressTracking()
            AzanStateHolder.markCompleted()

            val completedIntent = Intent(ACTION_AZAN_COMPLETED).apply {
                setPackage(packageName)
            }
            sendBroadcast(completedIntent)

            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.stop()
            }
            mediaPlayer?.release()
            mediaPlayer = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }
        stopSelf()
    }

    override fun onDestroy() {
        stopAzan()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "تنبيهات وأذان الصلاة (شاشة كاملة)",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "قناة إشعارات مواقيت الصلاة وشاشة الأذان بصوت الشيخ إسلام صبحي"
                setBypassDnd(true)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
}
