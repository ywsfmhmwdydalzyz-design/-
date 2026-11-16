package com.example.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class SakinaNotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val notificationManager = SakinaNotificationManager(context)
        val settings = notificationManager.settings.value

        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED -> {
                notificationManager.scheduleAllNotifications()
                OngoingPrayerNotificationManager.startService(context)
            }

            SakinaNotificationManager.ACTION_EXACT_PRAYER -> {
                val prayerName = intent.getStringExtra(SakinaNotificationManager.EXTRA_PRAYER_NAME) ?: "الصلاة"

                if (settings.exactPrayerAlerts) {
                    notificationManager.postNotification(
                        id = 702,
                        title = "حان الآن وقت صلاة $prayerName 🕋",
                        content = "الله أكبر .. الله أكبر. حي على الصلاة، حي على الفلاح.",
                        channelId = SakinaNotificationManager.CHANNEL_PRAYERS_ID
                    )
                }

                // تحديث الإشعار الثابت فور دخول وقت الصلاة
                OngoingPrayerNotificationManager.updateNotification(context)
                notificationManager.scheduleAllNotifications()
            }

            SakinaNotificationManager.ACTION_PERIODIC_DUA -> {
                if (settings.periodicDuas) {
                    notificationManager.postRandomDuaNotification()
                }
                notificationManager.scheduleAllNotifications()
            }

            SakinaNotificationManager.ACTION_PROPHET_PRAYER -> {
                if (settings.prophetReminders) {
                    notificationManager.postNotification(
                        id = 703,
                        title = "ﷺ أكثر من الصلاة والسلام على نبينا محمد",
                        content = "إن الله وملائكته يصلون على النبي، يا أيها الذين آمنوا صلوا عليه وسلموا تسليمًا.",
                        channelId = SakinaNotificationManager.CHANNEL_FAITH_ID
                    )
                }
                notificationManager.scheduleAllNotifications()
            }

            SakinaNotificationManager.ACTION_MORNING_AZKAR -> {
                if (settings.morningAzkar) {
                    notificationManager.postNotification(
                        id = 704,
                        title = "صباح الخير.. أذكار الصباح ☀️",
                        content = "صباح الخير.. لا تنسَ أذكار الصباح ليبدأ يومك ببركة ونور وسكينة.",
                        channelId = SakinaNotificationManager.CHANNEL_FAITH_ID
                    )
                }
                notificationManager.scheduleAllNotifications()
            }

            SakinaNotificationManager.ACTION_EVENING_AZKAR -> {
                if (settings.eveningAzkar) {
                    notificationManager.postNotification(
                        id = 705,
                        title = "أذكار المساء حصنك حصين 🌙",
                        content = "أذكار المساء حصنك حصين.. ألقِ نظرة عليها الآن واذكر ربك.",
                        channelId = SakinaNotificationManager.CHANNEL_FAITH_ID
                    )
                }
                notificationManager.scheduleAllNotifications()
            }

            SakinaNotificationManager.ACTION_SLEEP_AZKAR -> {
                if (settings.sleepAzkar) {
                    notificationManager.postNotification(
                        id = 706,
                        title = "سورة الملك وأذكار النوم ✨",
                        content = "استغفر الله العظيم وتذكر قراءة سورة الملك وأذكار النوم.",
                        channelId = SakinaNotificationManager.CHANNEL_FAITH_ID
                    )
                }
                notificationManager.scheduleAllNotifications()
            }

            SakinaNotificationManager.ACTION_FRIDAY_SUNAN -> {
                if (settings.fridaySunan) {
                    notificationManager.postNotification(
                        id = 707,
                        title = "🕌 تذكير بسنن يوم الجمعة المباركة",
                        content = "تذكير بقراءة سورة الكهف، والإكثار من الصلاة على النبي ﷺ، والدعاء في ساعة الاستجابة.",
                        channelId = SakinaNotificationManager.CHANNEL_FAITH_ID
                    )
                }
                notificationManager.scheduleAllNotifications()
            }

            SakinaNotificationManager.ACTION_DAILY_WIRD -> {
                if (settings.dailyWirdReminder) {
                    notificationManager.postNotification(
                        id = 709,
                        title = "📖 وردك اليومي من القرآن والأذكار",
                        content = "هل قرأت وردك اليومي من القرآن والأذكار اليوم؟ اضغط هنا لفتح مصحفك وأذكارك واستثمار يومك.",
                        channelId = SakinaNotificationManager.CHANNEL_FAITH_ID
                    )
                }
                notificationManager.scheduleAllNotifications()
            }
        }
    }
}
