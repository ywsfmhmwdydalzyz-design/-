package com.example.ui.screens

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.MainActivity
import com.example.services.AzanService
import com.example.services.AzanStateHolder
import com.example.ui.theme.SakinaTheme

import android.content.BroadcastReceiver
import android.content.IntentFilter
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch

class AzanActivity : ComponentActivity() {

    private val azanCompletionReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == AzanService.ACTION_AZAN_COMPLETED || intent?.action == AzanService.ACTION_STOP_AZAN) {
                if (!isFinishing && !isDestroyed) {
                    finish()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // إيقاظ الشاشة وعرض الشاشة فوق شاشة القفل عند الأذان
        setupLockscreenFlags()

        val filter = IntentFilter().apply {
            addAction(AzanService.ACTION_AZAN_COMPLETED)
            addAction(AzanService.ACTION_STOP_AZAN)
        }
        ContextCompat.registerReceiver(
            this,
            azanCompletionReceiver,
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )

        // مراقبة مباشرة لاكتمال الأذان لإغلاق الشاشة فوراً وبشكل مضمون 100%
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                AzanStateHolder.isCompleted.collect { completed ->
                    if (completed && !isFinishing && !isDestroyed) {
                        finish()
                    }
                }
            }
        }

        val prayerName = intent.getStringExtra("PRAYER_NAME") ?: "الصلاة"

        setContent {
            SakinaTheme {
                AzanPlayerScreen(
                    prayerName = prayerName,
                    onDismiss = {
                        val stopIntent = Intent(this, AzanService::class.java).apply {
                            action = AzanService.ACTION_STOP_AZAN
                        }
                        startService(stopIntent)
                        finish()
                    },
                    onOpenApp = {
                        val stopIntent = Intent(this, AzanService::class.java).apply {
                            action = AzanService.ACTION_STOP_AZAN
                        }
                        startService(stopIntent)
                        val appIntent = Intent(this, MainActivity::class.java).apply {
                            setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        }
                        startActivity(appIntent)
                        finish()
                    }
                )
            }
        }
    }

    private fun setupLockscreenFlags() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
            keyguardManager?.requestDismissKeyguard(this, null)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    override fun onDestroy() {
        try {
            unregisterReceiver(azanCompletionReceiver)
        } catch (_: Exception) {
            // Ignored if already unregistered
        }
        super.onDestroy()
    }
}
