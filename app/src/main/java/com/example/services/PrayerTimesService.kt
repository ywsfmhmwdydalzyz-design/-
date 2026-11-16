package com.example.services

import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import com.example.utils.LocationPrayerManager
import com.example.utils.OngoingPrayerNotificationManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * خدمة مواقيت الصلاة المستمرة في الخلفية (Foreground Service)
 * تضمن استمرار العداد التنازلي المباشر وتحديث الإشعار الثابت ثانية بثانية بدون توقف أو تجمد
 */
class PrayerTimesService : Service() {

    companion object {
        const val ACTION_START = "com.example.services.ACTION_START_PRAYER_SERVICE"
        const val ACTION_STOP = "com.example.services.ACTION_STOP_PRAYER_SERVICE"
    }

    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var tickerJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        LocationPrayerManager.ensureInitialized(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopForegroundService()
            return START_NOT_STICKY
        }

        LocationPrayerManager.ensureInitialized(this)

        // 1. تشغيل الخدمة كـ Foreground Service مع الإشعار الدائم فورياً
        val notification = OngoingPrayerNotificationManager.buildNotification(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                OngoingPrayerNotificationManager.NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else {
            startForeground(OngoingPrayerNotificationManager.NOTIFICATION_ID, notification)
        }

        // 2. تشغيل التحديث المستمر للعداد ثانية بثانية
        startLiveTicker()

        return START_STICKY // يعيد النظام تشغيل الخدمة تلقائياً في حال إغلاقها
    }

    private fun startLiveTicker() {
        tickerJob?.cancel()
        tickerJob = serviceScope.launch {
            while (isActive) {
                OngoingPrayerNotificationManager.updateNotification(this@PrayerTimesService)
                delay(30_000L) // تحديث دوري هادئ كل 30 ثانية لتجنب تقييد النظام مع استمرار عداد النظام التنازلي المباشر
            }
        }
    }

    private fun stopForegroundService() {
        tickerJob?.cancel()
        tickerJob = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }
        stopSelf()
    }

    override fun onDestroy() {
        stopForegroundService()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
