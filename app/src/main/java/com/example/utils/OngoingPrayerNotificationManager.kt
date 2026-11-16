package com.example.utils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.services.PrayerTimesService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Calendar

/**
 * مدير الإشعار الدائم والمثبت (Ongoing Notification)
 * تصميم مدمج وأنيق (Compact) وثابت مع عداد تنازلي مباشر عبر ساعة النظام (Chronometer):
 * 1. العنوان الرئيسي: المدينة المختصرة • اليوم، التاريخ الهجري (مثال: مرسى مطروح • السبت، 20 ربيع الآخر 1448 هـ)
 * 2. النص الفرعي:
 *    🕋 الصلاة القادمة: صلاة العشاء
 *    ⏱️ متبقي: 00:45:00
 * 3. تفعيل عداد النظام التنازلي التلقائي (Chronometer CountDown) مع setShowWhen(true) ليعمل مباشرة وبدقة تامة
 * 4. حماية تامة من المسح بواسطة setOngoing(true) و (FLAG_NO_CLEAR | FLAG_ONGOING_EVENT)
 * 5. دعم تشغيل خدمة Foreground Service للحفاظ على التحديث المستمر
 */
object OngoingPrayerNotificationManager {

    const val CHANNEL_ID = "sakina_ongoing_channel"
    const val CHANNEL_NAME = "الإشعار الثابت لمواقيت الصلاة"
    const val NOTIFICATION_ID = 55555

    private val _isOngoingActive = MutableStateFlow(true)
    val isOngoingActive: StateFlow<Boolean> = _isOngoingActive.asStateFlow()

    private var cachedPendingIntent: PendingIntent? = null

    private fun getPendingIntent(context: Context): PendingIntent {
        return cachedPendingIntent ?: synchronized(this) {
            cachedPendingIntent ?: run {
                val launchIntent = Intent(context.applicationContext, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                PendingIntent.getActivity(
                    context.applicationContext,
                    NOTIFICATION_ID,
                    launchIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                ).also { cachedPendingIntent = it }
            }
        }
    }

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "إشعار دائم ومثبت لمواقيت الصلاة والعد التنازلي المباشر"
                setShowBadge(false)
                enableVibration(false)
                setSound(null, null)
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    /**
     * بناء الإشعار الثابت بالبيانات المنسقة والمختصرة والعداد التنازلي المباشر للنظام
     */
    fun buildNotification(context: Context): Notification {
        createChannel(context)

        val loc = LocationPrayerManager.getLocation(context)
        val lat = loc.latitude
        val lng = loc.longitude

        // تنظيف اسم المدينة وإزالة البادئات المكررة
        val cleanCity = LocationPrayerManager.cleanCityName(loc.cityName)

        // حساب موعد الصلاة القادمة والعد التنازلي الزمني الدقيق
        val countdown = PrayerTimeCalculator.getNextPrayerCountdown(lat, lng)
        val nextPrayerName = countdown.prayerName
        val targetPrayerEpochMillis = PrayerTimeCalculator.getNextPrayerTargetEpochMillis(lat, lng)

        // التاريخ الهجري بتنسيق: السبت، 20 ربيع الآخر 1448 هـ
        val hijriDate = HijriCalendarHelper.gregorianToHijri(Calendar.getInstance())
        val hijriFormatted = hijriDate.fullHijriFormatted

        // العنوان الرئيسي المدمج: مرسى مطروح • السبت، 20 ربيع الآخر 1448 هـ
        val title = "$cleanCity • $hijriFormatted"

        // النصوص الفرعية المدمجة بدقة تامة ودون أي تكرار
        val prayerLine = "🕋 الصلاة القادمة: صلاة $nextPrayerName (${countdown.nextPrayerTimeFormatted})"
        val countdownLine = "⏱️ موعد الأذان: ${countdown.nextPrayerTimeFormatted} (متبقي ${countdown.remainingFormatted})"

        val collapsedText = "$prayerLine  |  $countdownLine"
        val compactBigText = "$prayerLine\n$countdownLine"

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(collapsedText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(compactBigText))
            .setOngoing(true) // تثبيت الإشعار في أعلى اللوحة ومنع حذفه بالسحب
            .setAutoCancel(false)
            .setOnlyAlertOnce(true) // تحديث صامت وسلس بدون رنين أو اهتزاز
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(getPendingIntent(context))
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

        // تفعيل العداد التنازلي المباشر للنظام (Chronometer) مع إظهاره بالترويسة بالزمن الثابت لموعد الصلاة
        if (targetPrayerEpochMillis > System.currentTimeMillis()) {
            builder.setShowWhen(true)
            builder.setWhen(targetPrayerEpochMillis)
            builder.setUsesChronometer(true)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                builder.setChronometerCountDown(true)
            }
        } else {
            builder.setShowWhen(false)
        }

        // تعيين الأعلام الصارمة لضمان بقائه ثابتاً ولا يمكن مسحه بـ "مسح الكل"
        return builder.build().apply {
            flags = flags or Notification.FLAG_NO_CLEAR or Notification.FLAG_ONGOING_EVENT
        }
    }

    /**
     * تحديث الإشعار الثابت فوراً
     */
    fun updateNotification(context: Context) {
        if (!_isOngoingActive.value) return
        try {
            val notification = buildNotification(context)
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
        } catch (_: SecurityException) {
        } catch (_: Exception) {}
    }

    /**
     * تشغيل خدمة Foreground Service للحفاظ على تحديث العداد في الخلفية ثانية بثانية
     */
    fun startService(context: Context) {
        _isOngoingActive.value = true
        try {
            val serviceIntent = Intent(context.applicationContext, PrayerTimesService::class.java).apply {
                action = PrayerTimesService.ACTION_START
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.applicationContext.startForegroundService(serviceIntent)
            } else {
                context.applicationContext.startService(serviceIntent)
            }
        } catch (_: Exception) {
            // في حال تعذر تشغيل Foreground Service لأي سبب، نقوم بتحديث الإشعار يدوياً
            updateNotification(context)
        }
    }

    /**
     * إيقاف الخدمة والإشعار الثابت
     */
    fun stopService(context: Context) {
        _isOngoingActive.value = false
        try {
            val serviceIntent = Intent(context.applicationContext, PrayerTimesService::class.java).apply {
                action = PrayerTimesService.ACTION_STOP
            }
            context.applicationContext.startService(serviceIntent)
        } catch (_: Exception) {}
        try {
            NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID)
        } catch (_: Exception) {}
    }

    /**
     * إلغاء الإشعار الثابت
     */
    fun cancel(context: Context) {
        stopService(context)
    }
}
