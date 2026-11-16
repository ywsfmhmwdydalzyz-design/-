package com.example.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.receivers.AzanReceiver
import java.util.Calendar

object PrayerTimeManager {

    private const val TAG = "PrayerTimeManager"

    // طلبات المنبه لكل صلاة على حدة لضمان عملها بالتوازي
    private const val REQ_FAJR = 1001
    private const val REQ_DHUHR = 1002
    private const val REQ_ASR = 1003
    private const val REQ_MAGHRIB = 1004
    private const val REQ_ISHA = 1005

    /**
     * جدولة موعد صلاة محددة باستخدام AlarmManager بدقة تامة وبدون أي تقديم أو تأخير
     */
    fun scheduleSinglePrayerAlarm(
        context: Context,
        prayerTimeMillis: Long,
        prayerName: String,
        requestCode: Int
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, AzanReceiver::class.java).apply {
            putExtra("PRAYER_NAME", prayerName)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            val canSchedule = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                alarmManager.canScheduleExactAlarms()
            } else {
                true
            }

            if (canSchedule) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        prayerTimeMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        prayerTimeMillis,
                        pendingIntent
                    )
                }
                Log.d(TAG, "تمت جدولة موعد أذان $prayerName بنجاح في الوقت: $prayerTimeMillis بدقة متناهية")
            } else {
                alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    prayerTimeMillis,
                    pendingIntent
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "خطأ أثناء جدولة المنبه: ${e.message}")
        }
    }

    /**
     * جدولة جميع الصلوات الخمس بدقة متناهية لكل محافظة بحسب إحداثياتها الرسمية
     */
    fun scheduleAllPrayers(
        context: Context,
        lat: Double = 0.0,
        lng: Double = 0.0
    ) {
        val loc = LocationPrayerManager.getLocation(context)
        val finalLat = if (lat != 0.0) lat else loc.latitude
        val finalLng = if (lng != 0.0) lng else loc.longitude
        val now = Calendar.getInstance()
        val nowMillis = now.timeInMillis
        val prayerTimes = PrayerTimeCalculator.computePrayerTimes(finalLat, finalLng, now)

        val prayersList = listOf(
            Triple("الفجر", prayerTimes.fajrH to prayerTimes.fajrM, REQ_FAJR),
            Triple("الظهر", prayerTimes.dhuhrH to prayerTimes.dhuhrM, REQ_DHUHR),
            Triple("العصر", prayerTimes.asrH to prayerTimes.asrM, REQ_ASR),
            Triple("المغرب", prayerTimes.maghribH to prayerTimes.maghribM, REQ_MAGHRIB),
            Triple("العشاء", prayerTimes.ishaH to prayerTimes.ishaM, REQ_ISHA)
        )

        var scheduledAny = false

        for ((name, time, req) in prayersList) {
            val cal = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, time.first)
                set(Calendar.MINUTE, time.second)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            if (cal.timeInMillis > nowMillis) {
                scheduleSinglePrayerAlarm(
                    context = context,
                    prayerTimeMillis = cal.timeInMillis,
                    prayerName = name,
                    requestCode = req
                )
                scheduledAny = true
            }
        }

        // إذا انقضت كل صلوات اليوم، نجدول فجر الغد مباشرة
        if (!scheduledAny) {
            val tomorrow = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }
            val tomorrowTimes = PrayerTimeCalculator.computePrayerTimes(finalLat, finalLng, tomorrow)
            val tomorrowFajrCal = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, 1)
                set(Calendar.HOUR_OF_DAY, tomorrowTimes.fajrH)
                set(Calendar.MINUTE, tomorrowTimes.fajrM)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            scheduleSinglePrayerAlarm(
                context = context,
                prayerTimeMillis = tomorrowFajrCal.timeInMillis,
                prayerName = "الفجر",
                requestCode = REQ_FAJR
            )
        }
    }

    /**
     * للتوافق مع استدعاءات الكود الحالية
     */
    fun scheduleNextPrayerForGovernorate(context: Context, lat: Double, lng: Double) {
        scheduleAllPrayers(context, lat, lng)
    }

    /**
     * إلغاء جميع منبهات الأذان
     */
    fun cancelAllAlarms(context: Context) {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val reqs = listOf(REQ_FAJR, REQ_DHUHR, REQ_ASR, REQ_MAGHRIB, REQ_ISHA)
            for (req in reqs) {
                val intent = Intent(context, AzanReceiver::class.java)
                val pi = PendingIntent.getBroadcast(
                    context,
                    req,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                alarmManager.cancel(pi)
            }
        } catch (e: Exception) {
            Log.e(TAG, "خطأ أثناء إلغاء المنبهات: ${e.message}")
        }
    }
}
