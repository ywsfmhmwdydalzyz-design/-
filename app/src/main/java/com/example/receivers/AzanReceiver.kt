package com.example.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.services.AzanService
import com.example.ui.screens.AzanActivity
import com.example.utils.LocationPrayerManager
import com.example.utils.PrayerTimeManager

class AzanReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val prayerName = intent.getStringExtra("PRAYER_NAME") ?: "الصلاة"

        // 1. تشغيل خدمة الأذان الفورية في الواجهة مع صوت الشيخ إسلام صبحي
        val serviceIntent = Intent(context, AzanService::class.java).apply {
            putExtra("PRAYER_NAME", prayerName)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }

        // 2. إطلاق شاشة الأذان الكاملة الفورية (Full Screen UI)
        try {
            val fullScreenIntent = Intent(context, AzanActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or 
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or 
                        Intent.FLAG_ACTIVITY_SINGLE_TOP
                putExtra("PRAYER_NAME", prayerName)
            }
            context.startActivity(fullScreenIntent)
        } catch (_: Exception) {}

        // 3. إعادة جدولة جميع الصلوات القادمة بالاعتماد دائماً على الموقع المحفوظ بالتخزين الدائم
        try {
            val location = LocationPrayerManager.getLocation(context)
            PrayerTimeManager.scheduleAllPrayers(
                context = context,
                lat = location.latitude,
                lng = location.longitude
            )
            com.example.utils.OngoingPrayerNotificationManager.updateNotification(context)
        } catch (_: Exception) {}
    }
}
