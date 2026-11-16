package com.example.utils

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * إدارة وتخزين حجم خط القراءة والأذكار وإعدادات الوضع الليلي عالي التباين
 */
object QuranFontSettings {
    private const val PREF_NAME = "sakina_font_prefs"
    private const val KEY_FONT_SIZE = "quran_font_size"
    private const val KEY_DISPLAY_SCALE = "quran_display_scale"
    private const val KEY_NIGHT_MODE = "quran_night_mode"
    private const val KEY_FULLSCREEN = "quran_fullscreen_mode"
    private const val KEY_USE_IMAGE_MODE = "quran_use_image_mode"

    const val DEFAULT_FONT_SIZE = 24f
    const val MIN_FONT_SIZE = 18f
    const val MAX_FONT_SIZE = 36f

    const val DEFAULT_SCALE = 1.0f
    const val MIN_SCALE = 0.80f
    const val MAX_SCALE = 1.45f

    private val _fontSizeSp = MutableStateFlow(DEFAULT_FONT_SIZE)
    val fontSizeSp: StateFlow<Float> = _fontSizeSp.asStateFlow()

    private val _displayScale = MutableStateFlow(DEFAULT_SCALE)
    val displayScale: StateFlow<Float> = _displayScale.asStateFlow()

    private val _isNightMode = MutableStateFlow(false)
    val isNightMode: StateFlow<Boolean> = _isNightMode.asStateFlow()

    private val _isFullscreen = MutableStateFlow(false)
    val isFullscreen: StateFlow<Boolean> = _isFullscreen.asStateFlow()

    private val _useImageMushaf = MutableStateFlow(false)
    val useImageMushaf: StateFlow<Boolean> = _useImageMushaf.asStateFlow()

    private var prefs: SharedPreferences? = null

    fun init(context: Context) {
        if (prefs == null) {
            prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            val savedFontSize = prefs?.getFloat(KEY_FONT_SIZE, DEFAULT_FONT_SIZE) ?: DEFAULT_FONT_SIZE
            val savedScale = prefs?.getFloat(KEY_DISPLAY_SCALE, DEFAULT_SCALE) ?: DEFAULT_SCALE
            val hasReset = prefs?.getBoolean("quran_reset_night_v3", false) ?: false
            val savedNightMode = if (!hasReset) {
                prefs?.edit()?.putBoolean(KEY_NIGHT_MODE, false)?.putBoolean("quran_reset_night_v3", true)?.apply()
                false
            } else {
                prefs?.getBoolean(KEY_NIGHT_MODE, false) ?: false
            }
            val savedFullscreen = prefs?.getBoolean(KEY_FULLSCREEN, false) ?: false
            val savedImageMode = prefs?.getBoolean(KEY_USE_IMAGE_MODE, false) ?: false

            _fontSizeSp.value = savedFontSize
            _displayScale.value = savedScale.coerceIn(MIN_SCALE, MAX_SCALE)
            _isNightMode.value = savedNightMode
            _isFullscreen.value = savedFullscreen
            _useImageMushaf.value = savedImageMode
        }
    }

    fun setFontSize(size: Float) {
        val clamped = size.coerceIn(MIN_FONT_SIZE, MAX_FONT_SIZE)
        _fontSizeSp.value = clamped
        prefs?.edit()?.putFloat(KEY_FONT_SIZE, clamped)?.apply()
    }

    fun increaseFont() {
        setFontSize(_fontSizeSp.value + 2f)
    }

    fun decreaseFont() {
        setFontSize(_fontSizeSp.value - 2f)
    }

    fun setDisplayScale(scale: Float) {
        val clamped = scale.coerceIn(MIN_SCALE, MAX_SCALE)
        _displayScale.value = clamped
        prefs?.edit()?.putFloat(KEY_DISPLAY_SCALE, clamped)?.apply()
    }

    fun increaseDisplayScale() {
        setDisplayScale(_displayScale.value + 0.08f)
    }

    fun decreaseDisplayScale() {
        setDisplayScale(_displayScale.value - 0.08f)
    }

    fun resetDisplayScale() {
        setDisplayScale(DEFAULT_SCALE)
    }

    fun setNightMode(enabled: Boolean) {
        _isNightMode.value = enabled
        prefs?.edit()?.putBoolean(KEY_NIGHT_MODE, enabled)?.apply()
    }

    fun toggleNightMode() {
        setNightMode(!_isNightMode.value)
    }

    fun setFullscreen(enabled: Boolean) {
        _isFullscreen.value = enabled
        prefs?.edit()?.putBoolean(KEY_FULLSCREEN, enabled)?.apply()
    }

    fun toggleFullscreen() {
        setFullscreen(!_isFullscreen.value)
    }

    fun setUseImageMushaf(enabled: Boolean) {
        _useImageMushaf.value = enabled
        prefs?.edit()?.putBoolean(KEY_USE_IMAGE_MODE, enabled)?.apply()
    }
}
