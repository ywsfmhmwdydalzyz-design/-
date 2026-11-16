package com.example.utils

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.data.sources.DuasDataSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Calendar

data class NotificationPreferences(
    val exactPrayerAlerts: Boolean = true, // الأذان ينطلق مرة واحدة فقط في وقت الصلاة الدقيق 100%
    val periodicDuas: Boolean = true, // إشعارات دورية متجددة للأدعية
    val prophetReminders: Boolean = true,
    val prophetIntervalHours: Int = 1, // 1 or 2
    val morningAzkar: Boolean = true,
    val eveningAzkar: Boolean = true,
    val sleepAzkar: Boolean = true,
    val fridaySunan: Boolean = true,
    val dailyWirdReminder: Boolean = true, // تذكير يومي لطيف: هل قرأت وردك اليومي من القرآن والأذكار اليوم؟
    val ongoingNotification: Boolean = true // الإشعار الثابت والمستمر
)

class SakinaNotificationManager(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("sakina_notification_prefs", Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<NotificationPreferences> = _settings.asStateFlow()

    companion object {
        const val CHANNEL_PRAYERS_ID = "sakina_prayer_channel"
        const val CHANNEL_PRAYERS_NAME = "مواقيت الصلاة والأذان"

        const val CHANNEL_FAITH_ID = "sakina_faith_channel"
        const val CHANNEL_FAITH_NAME = "التذكيرات الإيمانية والأدعية والأذكار"

        const val ACTION_EXACT_PRAYER = "com.example.ACTION_EXACT_PRAYER"
        const val ACTION_PERIODIC_DUA = "com.example.ACTION_PERIODIC_DUA"
        const val ACTION_PROPHET_PRAYER = "com.example.ACTION_PROPHET_PRAYER"
        const val ACTION_MORNING_AZKAR = "com.example.ACTION_MORNING_AZKAR"
        const val ACTION_EVENING_AZKAR = "com.example.ACTION_EVENING_AZKAR"
        const val ACTION_SLEEP_AZKAR = "com.example.ACTION_SLEEP_AZKAR"
        const val ACTION_FRIDAY_SUNAN = "com.example.ACTION_FRIDAY_SUNAN"
        const val ACTION_DAILY_WIRD = "com.example.ACTION_DAILY_WIRD"

        const val EXTRA_PRAYER_NAME = "extra_prayer_name"
    }

    init {
        createNotificationChannels()
        scheduleAllNotifications()
        if (_settings.value.ongoingNotification) {
            OngoingPrayerNotificationManager.updateNotification(context)
        }
    }

    private fun loadSettings(): NotificationPreferences {
        return NotificationPreferences(
            exactPrayerAlerts = prefs.getBoolean("exact_prayer_alerts", true),
            periodicDuas = prefs.getBoolean("periodic_duas", true),
            prophetReminders = prefs.getBoolean("prophet_reminders", true),
            prophetIntervalHours = prefs.getInt("prophet_interval_hours", 1),
            morningAzkar = prefs.getBoolean("morning_azkar", true),
            eveningAzkar = prefs.getBoolean("evening_azkar", true),
            sleepAzkar = prefs.getBoolean("sleep_azkar", true),
            fridaySunan = prefs.getBoolean("friday_sunan", true),
            dailyWirdReminder = prefs.getBoolean("daily_wird_reminder", true),
            ongoingNotification = prefs.getBoolean("ongoing_notification", true)
        )
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val prayerChannel = NotificationChannel(
                CHANNEL_PRAYERS_ID,
                CHANNEL_PRAYERS_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "إشعارات مواقيت الصلاة والأذان الفورية لتطبيق سكينة"
                enableVibration(true)
            }

            val faithChannel = NotificationChannel(
                CHANNEL_FAITH_ID,
                CHANNEL_FAITH_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "التذكير بالأدعية المستجابة، الصلاة على النبي ﷺ، وأذكار الصباح والمساء"
                enableVibration(true)
            }

            notificationManager.createNotificationChannel(prayerChannel)
            notificationManager.createNotificationChannel(faithChannel)
        }

        // إنشاء قناة الإشعار الدائم
        OngoingPrayerNotificationManager.createChannel(context)
    }

    fun updateSettings(newSettings: NotificationPreferences) {
        prefs.edit()
            .putBoolean("exact_prayer_alerts", newSettings.exactPrayerAlerts)
            .putBoolean("periodic_duas", newSettings.periodicDuas)
            .putBoolean("prophet_reminders", newSettings.prophetReminders)
            .putInt("prophet_interval_hours", newSettings.prophetIntervalHours)
            .putBoolean("morning_azkar", newSettings.morningAzkar)
            .putBoolean("evening_azkar", newSettings.eveningAzkar)
            .putBoolean("sleep_azkar", newSettings.sleepAzkar)
            .putBoolean("friday_sunan", newSettings.fridaySunan)
            .putBoolean("daily_wird_reminder", newSettings.dailyWirdReminder)
            .putBoolean("ongoing_notification", newSettings.ongoingNotification)
            .apply()

        _settings.value = newSettings
        scheduleAllNotifications()

        if (newSettings.ongoingNotification) {
            OngoingPrayerNotificationManager.updateNotification(context)
        } else {
            OngoingPrayerNotificationManager.cancel(context)
        }
    }

    fun scheduleAllNotifications() {
        val currentSettings = _settings.value
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        // 1. أذكار الصباح (07:00 AM)
        if (currentSettings.morningAzkar) {
            scheduleDailyAlarm(alarmManager, ACTION_MORNING_AZKAR, 1001, 7, 0)
        } else {
            cancelAlarm(alarmManager, ACTION_MORNING_AZKAR, 1001)
        }

        // 2. أذكار المساء (05:00 PM = 17:00)
        if (currentSettings.eveningAzkar) {
            scheduleDailyAlarm(alarmManager, ACTION_EVENING_AZKAR, 1002, 17, 0)
        } else {
            cancelAlarm(alarmManager, ACTION_EVENING_AZKAR, 1002)
        }

        // 3. أذكار النوم وسورة الملك (10:30 PM = 22:30)
        if (currentSettings.sleepAzkar) {
            scheduleDailyAlarm(alarmManager, ACTION_SLEEP_AZKAR, 1003, 22, 30)
        } else {
            cancelAlarm(alarmManager, ACTION_SLEEP_AZKAR, 1003)
        }

        // 4. سنن يوم الجمعة (الجمعة 09:00 AM)
        if (currentSettings.fridaySunan) {
            scheduleWeeklyAlarm(alarmManager, ACTION_FRIDAY_SUNAN, 1004, Calendar.FRIDAY, 9, 0)
        } else {
            cancelAlarm(alarmManager, ACTION_FRIDAY_SUNAN, 1004)
        }

        // 5. التذكير اليومي بالورد (هل قرأت وردك اليومي من القرآن والأذكار اليوم؟) الساعة 08:30 PM = 20:30
        if (currentSettings.dailyWirdReminder) {
            scheduleDailyAlarm(alarmManager, ACTION_DAILY_WIRD, 1008, 20, 30)
        } else {
            cancelAlarm(alarmManager, ACTION_DAILY_WIRD, 1008)
        }

        // 6. التذكير الدوري بالصلاة على النبي ﷺ (كل ساعة أو ساعتين)
        if (currentSettings.prophetReminders) {
            scheduleIntervalAlarm(alarmManager, ACTION_PROPHET_PRAYER, 1005, currentSettings.prophetIntervalHours)
        } else {
            cancelAlarm(alarmManager, ACTION_PROPHET_PRAYER, 1005)
        }

        // 7. نظام إشعارات الأدعية الدورية المتجددة في أوقات متفرقة يومياً
        if (currentSettings.periodicDuas) {
            scheduleDailyAlarm(alarmManager, ACTION_PERIODIC_DUA, 1101, 9, 30)   // ضحى
            scheduleDailyAlarm(alarmManager, ACTION_PERIODIC_DUA, 1102, 13, 30)  // بعد الظهر
            scheduleDailyAlarm(alarmManager, ACTION_PERIODIC_DUA, 1103, 16, 45)  // بعد العصر
            scheduleDailyAlarm(alarmManager, ACTION_PERIODIC_DUA, 1104, 20, 30)  // المساء
        } else {
            cancelAlarm(alarmManager, ACTION_PERIODIC_DUA, 1101)
            cancelAlarm(alarmManager, ACTION_PERIODIC_DUA, 1102)
            cancelAlarm(alarmManager, ACTION_PERIODIC_DUA, 1103)
            cancelAlarm(alarmManager, ACTION_PERIODIC_DUA, 1104)
        }

        // 7. إلغاء أي منبهات قديمة خاصة بالتنبيه المسبق بـ 15 دقيقة تماماً لمنع التكرار
        for (i in 0..5) {
            cancelLegacyAlarm(alarmManager, 2000 + i)
        }

        // 8. جدولة مواعيد الأذان الدقيقة فقط (تنطلق مرة واحدة بالضبط في وقت الصلاة 100%)
        scheduleExactPrayerAlarmsOnly(alarmManager, currentSettings)

        // 9. تحديث الإشعار الثابت
        if (currentSettings.ongoingNotification) {
            OngoingPrayerNotificationManager.updateNotification(context)
        }
    }

    private fun scheduleDailyAlarm(alarmManager: AlarmManager, action: String, requestCode: Int, hour: Int, minute: Int) {
        val intent = Intent(context, SakinaNotificationReceiver::class.java).apply {
            this.action = action
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }
        } catch (_: SecurityException) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
        }
    }

    private fun scheduleWeeklyAlarm(alarmManager: AlarmManager, action: String, requestCode: Int, dayOfWeek: Int, hour: Int, minute: Int) {
        val intent = Intent(context, SakinaNotificationReceiver::class.java).apply {
            this.action = action
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, dayOfWeek)
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.WEEK_OF_YEAR, 1)
            }
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }
        } catch (_: SecurityException) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
        }
    }

    private fun scheduleIntervalAlarm(alarmManager: AlarmManager, action: String, requestCode: Int, intervalHours: Int) {
        val intent = Intent(context, SakinaNotificationReceiver::class.java).apply {
            this.action = action
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerTime = System.currentTimeMillis() + (intervalHours * 3600 * 1000L)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            }
        } catch (_: SecurityException) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
        }
    }

    /**
     * جدولة الأذان بدقة تامة 100% بدون أي منبه قبل موعد الصلاة بـ 15 دقيقة إطلاقاً
     */
    private fun scheduleExactPrayerAlarmsOnly(alarmManager: AlarmManager, currentSettings: NotificationPreferences) {
        if (!currentSettings.exactPrayerAlerts) return

        val loc = LocationPrayerManager.getLocation(context)
        val todayTimes = PrayerTimeCalculator.calculateTodayPrayers(loc.latitude, loc.longitude)
        val nowMillis = System.currentTimeMillis()

        val prayerList = listOf(
            todayTimes.fajr,
            todayTimes.dhuhr,
            todayTimes.asr,
            todayTimes.maghrib,
            todayTimes.isha
        )

        prayerList.forEachIndexed { index, prayer ->
            val pCal = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, prayer.hour)
                set(Calendar.MINUTE, prayer.minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            val exactMillis = pCal.timeInMillis
            // تنطلق فقط وفور حلول وقت الأذان
            if (exactMillis > nowMillis) {
                val exactIntent = Intent(context, SakinaNotificationReceiver::class.java).apply {
                    action = ACTION_EXACT_PRAYER
                    putExtra(EXTRA_PRAYER_NAME, prayer.nameArabic)
                }
                val pi = PendingIntent.getBroadcast(
                    context,
                    3000 + index,
                    exactIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, exactMillis, pi)
                    } else {
                        alarmManager.setExact(AlarmManager.RTC_WAKEUP, exactMillis, pi)
                    }
                } catch (_: Exception) {}
            }
        }
    }

    private fun cancelAlarm(alarmManager: AlarmManager, action: String, requestCode: Int) {
        val intent = Intent(context, SakinaNotificationReceiver::class.java).apply {
            this.action = action
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

    private fun cancelLegacyAlarm(alarmManager: AlarmManager, requestCode: Int) {
        val intent = Intent(context, SakinaNotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

    /**
     * إرسال إشعار دعاء متجدد من قائمة الأدعية الشاملة
     */
    fun postRandomDuaNotification() {
        val duas = DuasDataSource.allDuas
        val randomDua = duas.randomOrNull() ?: return

        postNotification(
            id = 708,
            title = "🤲 دعاء مستجاب: ${randomDua.title}",
            content = "${randomDua.text}\n\n﴿ ${randomDua.translationOrFadl} ﴾",
            channelId = CHANNEL_FAITH_ID
        )
    }

    fun sendTestNotification(type: String) {
        when (type) {
            "exact_prayer" -> {
                postNotification(
                    id = 902,
                    title = "حان الآن موعد أذان المغرب حسب توقيتك المحلي 🕋",
                    content = "حي على الصلاة، حي على الفلاح.. أقبل على ربك بقلب خاشع.",
                    channelId = CHANNEL_PRAYERS_ID
                )
            }
            "dua" -> {
                postRandomDuaNotification()
            }
            "prophet" -> {
                postNotification(
                    id = 903,
                    title = "ﷺ تذكير الصلاة على النبي الحبيب",
                    content = "اللهم صلِّ وسلم وبارك على نبينا وحبيبنا محمد وعلى آله وصحبه أجمعين.",
                    channelId = CHANNEL_FAITH_ID
                )
            }
            "morning" -> {
                postNotification(
                    id = 904,
                    title = "صباح الخير.. أذكار الصباح ☀️",
                    content = "لا تنسَ أذكار الصباح ليبدأ يومك ببركة ونور وحفظ من الرحمن.",
                    channelId = CHANNEL_FAITH_ID
                )
            }
            "evening" -> {
                postNotification(
                    id = 905,
                    title = "أذكار المساء حصنك حصين 🌙",
                    content = "أذكار المساء حصنك حصين.. ألقِ نظرة عليها الآن واذكر ربك كثيراً.",
                    channelId = CHANNEL_FAITH_ID
                )
            }
            "sleep" -> {
                postNotification(
                    id = 906,
                    title = "سورة الملك وأذكار النوم ✨",
                    content = "استغفر الله العظيم وتذكر قراءة سورة الملك المانعة من عذاب القبر وأذكار النوم.",
                    channelId = CHANNEL_FAITH_ID
                )
            }
            "friday" -> {
                postNotification(
                    id = 907,
                    title = "🕌 تذكير بسنن يوم الجمعة المباركة",
                    content = "سورة الكهف نور ما بين الجمعتين، وأكثروا من الصلاة على النبي ﷺ والدعاء في ساعة الإجابة.",
                    channelId = CHANNEL_FAITH_ID
                )
            }
            "daily_wird" -> {
                postNotification(
                    id = 908,
                    title = "📖 وردك اليومي من القرآن والأذكار",
                    content = "هل قرأت وردك اليومي من القرآن والأذكار اليوم؟ اجعل لقلبك نصيباً من نور الوحي والذكر.",
                    channelId = CHANNEL_FAITH_ID
                )
            }
            "ongoing" -> {
                OngoingPrayerNotificationManager.updateNotification(context)
            }
            else -> {
                postNotification(
                    id = 900,
                    title = "إشعار تجريبي من سكينة الإسلامي",
                    content = "نظام الإشعارات الذكي ومواقيت الصلاة يعمل بكفاءة ودقة متناهية.",
                    channelId = CHANNEL_FAITH_ID
                )
            }
        }
    }

    fun postNotification(id: Int, title: String, content: String, channelId: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            id,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content))
            .setPriority(
                if (channelId == CHANNEL_PRAYERS_ID) NotificationCompat.PRIORITY_HIGH
                else NotificationCompat.PRIORITY_DEFAULT
            )
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(id, notification)
        } catch (_: SecurityException) {}
    }
}
