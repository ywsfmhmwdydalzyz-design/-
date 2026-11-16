package com.example.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AppThemeMode(val titleArabic: String, val subtitleArabic: String) {
    LIGHT("الوضع النهاري (Light Mode)", "الوضع الافتراضي: ألوان عادية ناصعة ومريحة للرؤية النهارية"),
    DARK("الوضع الليلي (Dark Mode)", "خلفية داكنة فحمية مريحة للعين في الظلام"),
    SYSTEM("التلقائي حسب النظام (System Default)", "يتوافق تلقائياً مع إعدادات جهازك")
}

/**
 * إدارة وتخزين خيارات الوضع النهاري (Light Mode الافتراضي) والوضع الليلي (Dark Mode)
 * يعتمد على الحالة المباشرة التفاعلية (Compose State) لإعادة بناء الواجهة فورياً عند النقر.
 */
object ThemeManager {
    private const val PREFS_NAME = "sakina_theme_prefs"
    private const val KEY_THEME_MODE = "app_theme_mode"
    private const val KEY_RESET_LIGHT_V3 = "sakina_reset_light_v3"

    // حالة Compose تفاعلية مباشرة لتحديث الواجهة فورياً دون أي تأخير
    private val _isDarkState = mutableStateOf(false)
    val isDark: Boolean
        get() = _isDarkState.value

    private val _themeMode = MutableStateFlow(AppThemeMode.LIGHT)
    val themeMode: StateFlow<AppThemeMode> = _themeMode.asStateFlow()

    val currentMode: AppThemeMode
        get() = _themeMode.value

    private var prefs: SharedPreferences? = null

    fun init(context: Context) {
        if (prefs == null) {
            prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

            // إزالة أي قيمة قديمة تفرض الوضع الليلي قسرياً، وضمان بدء التطبيق دائماً بالوضع النهاري الفاتح
            val hasReset = prefs?.getBoolean(KEY_RESET_LIGHT_V3, false) ?: false
            if (!hasReset) {
                prefs?.edit()
                    ?.remove(KEY_THEME_MODE)
                    ?.remove("isDarkMode")
                    ?.remove("dark_theme")
                    ?.putString(KEY_THEME_MODE, AppThemeMode.LIGHT.name)
                    ?.putBoolean(KEY_RESET_LIGHT_V3, true)
                    ?.apply()

                _themeMode.value = AppThemeMode.LIGHT
                _isDarkState.value = false
                QuranFontSettings.setNightMode(false)
            } else {
                val savedModeName = prefs?.getString(KEY_THEME_MODE, AppThemeMode.LIGHT.name) ?: AppThemeMode.LIGHT.name
                val mode = try {
                    AppThemeMode.valueOf(savedModeName)
                } catch (_: Exception) {
                    AppThemeMode.LIGHT
                }
                _themeMode.value = mode
                _isDarkState.value = (mode == AppThemeMode.DARK)
            }
        }
    }

    /**
     * تحديث وضع الثيم فوراً وحفظه محلياً في SharedPreferences
     */
    fun setThemeMode(mode: AppThemeMode, systemIsDark: Boolean = false) {
        _themeMode.value = mode
        val newIsDark = when (mode) {
            AppThemeMode.LIGHT -> false
            AppThemeMode.DARK -> true
            AppThemeMode.SYSTEM -> systemIsDark
        }
        _isDarkState.value = newIsDark

        prefs?.edit()?.putString(KEY_THEME_MODE, mode.name)?.apply()

        // مزامنة الوضع الليلي للمصحف الشريف
        QuranFontSettings.setNightMode(newIsDark)
    }

    /**
     * تبديل مباشر وسلس بين الوضع النهاري (Light) والوضع الليلي (Dark)
     * يتم تحديث حالة Compose فوراً لضمان إعادة الرسم اللحظي للواجهة
     */
    fun toggleTheme(systemIsDark: Boolean = false) {
        if (_isDarkState.value) {
            setThemeMode(AppThemeMode.LIGHT, systemIsDark)
        } else {
            setThemeMode(AppThemeMode.DARK, systemIsDark)
        }
    }

    fun isDarkTheme(systemIsDark: Boolean = false): Boolean {
        return _isDarkState.value
    }
}
