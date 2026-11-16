package com.example.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

data class LocationData(
    val cityName: String = "مرسى مطروح",
    val countryName: String = "مصر",
    val latitude: Double = 31.3543,
    val longitude: Double = 27.2373,
    val isAuto: Boolean = false
)

object LocationPrayerManager {

    private const val PREFS_NAME = "sakina_location_prefs"
    private const val KEY_CITY = "city"
    private const val KEY_COUNTRY = "country"
    private const val KEY_LAT = "lat"
    private const val KEY_LNG = "lng"
    private const val KEY_IS_AUTO = "is_auto"

    private val _locationData = MutableStateFlow(LocationData())
    val locationData: StateFlow<LocationData> = _locationData.asStateFlow()

    private var prefs: SharedPreferences? = null
    private var appContext: Context? = null

    val PRESET_CITIES = listOf(
        // جميع محافظات جمهورية مصر العربية (معتمدة رسمياً طبقاً لهيئة المساحة)
        LocationData("مرسى مطروح", "مصر", 31.3543, 27.2373, false),
        LocationData("القاهرة", "مصر", 30.0444, 31.2357, false),
        LocationData("الإسكندرية", "مصر", 31.2001, 29.9187, false),
        LocationData("الجيزة", "مصر", 30.0131, 31.2089, false),
        LocationData("القليوبية (بنها)", "مصر", 30.4660, 31.1853, false),
        LocationData("الدقهلية (المنصورة)", "مصر", 31.0409, 31.3785, false),
        LocationData("الغربية (طنطا)", "مصر", 30.7865, 31.0004, false),
        LocationData("المنوفية (شبين الكوم)", "مصر", 30.5526, 31.0101, false),
        LocationData("الشرقية (الزقازيق)", "مصر", 30.5877, 31.5020, false),
        LocationData("البحيرة (دمنهور)", "مصر", 31.0364, 30.4695, false),
        LocationData("كفر الشيخ", "مصر", 31.1107, 30.9388, false),
        LocationData("دمياط", "مصر", 31.4175, 31.8144, false),
        LocationData("بورسعيد", "مصر", 31.2653, 32.3019, false),
        LocationData("الإسماعيلية", "مصر", 30.5965, 32.2715, false),
        LocationData("السويس", "مصر", 29.9668, 32.5498, false),
        LocationData("شمال سيناء (العريش)", "مصر", 31.1316, 33.8034, false),
        LocationData("جنوب سيناء (شرم الشيخ)", "مصر", 27.9158, 34.3299, false),
        LocationData("جنوب سيناء (طور سيناء)", "مصر", 28.2415, 33.6228, false),
        LocationData("البحر الأحمر (الغردقة)", "مصر", 27.2579, 33.8116, false),
        LocationData("الفيوم", "مصر", 29.3084, 30.8428, false),
        LocationData("بني سويف", "مصر", 29.0661, 31.0994, false),
        LocationData("المنيا", "مصر", 28.1099, 30.7503, false),
        LocationData("أسيوط", "مصر", 27.1783, 31.1859, false),
        LocationData("سوهاج", "مصر", 26.5570, 31.6948, false),
        LocationData("قنا", "مصر", 26.1551, 32.7160, false),
        LocationData("الأقصر", "مصر", 25.6872, 32.6396, false),
        LocationData("أسوان", "مصر", 24.0889, 32.8998, false),
        LocationData("الوادي الجديد (الخارجة)", "مصر", 25.4514, 30.5464, false),

        // العواصم والمدن الإسلامية
        LocationData("مكة المكرمة", "المملكة العربية السعودية", 21.4225, 39.8262, false),
        LocationData("المدينة المنورة", "المملكة العربية السعودية", 24.4672, 39.6111, false),
        LocationData("القدس الشريف", "فلسطين", 31.7683, 35.2137, false),
        LocationData("الرياض", "المملكة العربية السعودية", 24.7136, 46.6753, false),
        LocationData("دبي", "الإمارات العربية المتحدة", 25.2048, 55.2708, false),
        LocationData("عمّان", "الأردن", 31.9454, 35.9284, false),
        LocationData("الرباط", "المغرب", 34.0209, -6.8416, false),
        LocationData("الجزائر العاصمة", "الجزائر", 36.7538, 3.0588, false),
        LocationData("تونس", "تونس", 36.8065, 10.1815, false),
        LocationData("إسطنبول", "تركيا", 41.0082, 28.9784, false)
    )

    /**
     * ضمان تحميل الموقع المحفوظ دائماً من التخزين الدائم SharedPreferences
     * وتجنب أي إعادة تعيين افتراضية إلى القاهرة عند تشغيل الخدمات في الخلفية أو الأذان.
     */
    fun ensureInitialized(context: Context): LocationData {
        if (appContext == null) {
            appContext = context.applicationContext
        }
        val sp = prefs ?: context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).also {
            prefs = it
        }

        if (sp.contains(KEY_CITY)) {
            val city = cleanCityName(sp.getString(KEY_CITY, "مرسى مطروح") ?: "مرسى مطروح")
            val country = sp.getString(KEY_COUNTRY, "مصر") ?: "مصر"
            val lat = sp.getFloat(KEY_LAT, 31.3543f).toDouble()
            val lng = sp.getFloat(KEY_LNG, 27.2373f).toDouble()
            val isAuto = sp.getBoolean(KEY_IS_AUTO, false)
            val loaded = LocationData(city, country, lat, lng, isAuto)
            _locationData.value = loaded
            return loaded
        } else {
            // حفظ مرسى مطروح كموقع افتراضي دائم أول مرة
            val defaultMatruh = LocationData("مرسى مطروح", "مصر", 31.3543, 27.2373, false)
            _locationData.value = defaultMatruh
            sp.edit().apply {
                putString(KEY_CITY, defaultMatruh.cityName)
                putString(KEY_COUNTRY, defaultMatruh.countryName)
                putFloat(KEY_LAT, defaultMatruh.latitude.toFloat())
                putFloat(KEY_LNG, defaultMatruh.longitude.toFloat())
                putBoolean(KEY_IS_AUTO, false)
                commit()
            }
            return defaultMatruh
        }
    }

    fun initialize(context: Context) {
        val currentLoc = ensureInitialized(context)
        // جدولة الأذان القادم بحسب الموقع المحفوظ
        appContext?.let { ctx ->
            PrayerTimeManager.scheduleAllPrayers(ctx, currentLoc.latitude, currentLoc.longitude)
        }
    }

    /**
     * الحصول على الموقع المحفوظ مع التأكد من قراءته من SharedPreferences أولاً
     */
    fun getLocation(context: Context? = null): LocationData {
        if (context != null) {
            ensureInitialized(context)
        }
        return _locationData.value
    }

    /**
     * حفظ وتثبيت المحافظة/المدينة يدوياً وتخزينها دائماً
     */
    fun setManualCity(preset: LocationData, context: Context? = null) {
        val cleanedCity = cleanCityName(preset.cityName)
        val updated = preset.copy(cityName = cleanedCity, isAuto = false)
        _locationData.value = updated
        saveToPrefs(context)
    }

    @SuppressLint("MissingPermission")
    fun requestDeviceLocation(context: Context, onResult: (Boolean, String) -> Unit) {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        if (locationManager == null) {
            onResult(false, "خدمة تحديد الموقع غير متوفرة بالجهاز")
            return
        }

        try {
            val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            val isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

            if (!isGpsEnabled && !isNetworkEnabled) {
                onResult(false, "يرجى تفعيل خدمة الموقع (GPS) من إعدادات الهاتف")
                return
            }

            var bestLocation: Location? = null
            if (isNetworkEnabled) {
                bestLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            }
            if (isGpsEnabled && bestLocation == null) {
                bestLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            }

            if (bestLocation != null) {
                applyLocation(context, bestLocation)
                onResult(true, "تم تحديد موقعك بدقة بنجاح 📍")
            } else {
                val provider = if (isNetworkEnabled) LocationManager.NETWORK_PROVIDER else LocationManager.GPS_PROVIDER
                locationManager.requestSingleUpdate(provider, object : LocationListener {
                    override fun onLocationChanged(loc: Location) {
                        applyLocation(context, loc)
                        onResult(true, "تم تحديث الموقع بنجاح 📍")
                    }
                    override fun onStatusChanged(p: String?, s: Int, e: Bundle?) {}
                    override fun onProviderEnabled(p: String) {}
                    override fun onProviderDisabled(p: String) {}
                }, context.mainLooper)
            }
        } catch (e: SecurityException) {
            onResult(false, "يرجى منح إذن الوصول إلى الموقع في التطبيق")
        } catch (e: Exception) {
            Log.e("LocationPrayerManager", "Error requesting location: ${e.message}")
            onResult(false, "تعذر جلب الموقع: ${e.localizedMessage}")
        }
    }

    fun cleanCityName(raw: String?): String {
        if (raw.isNullOrBlank()) return "مرسى مطروح"
        var result = raw.trim()
        if (result.contains("(") && result.contains(")")) {
            val inside = result.substringAfter("(").substringBefore(")").trim()
            if (inside.isNotEmpty()) {
                result = inside
            }
        }
        val prefixes = listOf(
            "قسم أول ", "قسم اول ", "قسم ثان ", "قسم ثاني ", "قسم ثالث ", "قسم ",
            "مركز ", "بندر ", "مدينة ", "حي ", "محافظة "
        )
        for (prefix in prefixes) {
            if (result.startsWith(prefix)) {
                result = result.removePrefix(prefix).trim()
                break
            }
        }
        return result.ifBlank { "مرسى مطروح" }
    }

    private fun applyLocation(context: Context, loc: Location) {
        var cityName = "مرسى مطروح"
        var countryName = "مصر"

        try {
            val geocoder = Geocoder(context, Locale("ar"))
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                geocoder.getFromLocation(loc.latitude, loc.longitude, 1) { addresses ->
                    if (addresses.isNotEmpty()) {
                        val addr = addresses[0]
                        val rawCity = addr.locality ?: addr.subAdminArea ?: addr.adminArea ?: "مرسى مطروح"
                        val city = cleanCityName(rawCity)
                        val country = addr.countryName ?: "مصر"
                        _locationData.value = LocationData(city, country, loc.latitude, loc.longitude, true)
                        saveToPrefs(context)
                    }
                }
            } else {
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(loc.latitude, loc.longitude, 1)
                if (!addresses.isNullOrEmpty()) {
                    val addr = addresses[0]
                    val rawCity = addr.locality ?: addr.subAdminArea ?: addr.adminArea ?: "مرسى مطروح"
                    cityName = cleanCityName(rawCity)
                    countryName = addr.countryName ?: "مصر"
                }
                _locationData.value = LocationData(cityName, countryName, loc.latitude, loc.longitude, true)
                saveToPrefs(context)
            }
        } catch (_: Exception) {
            // في حال فشل الاتصال، لا نغير الموقع المحفوظ إطلاقاً
            ensureInitialized(context)
        }
    }

    /**
     * حفظ دائم ومتزامن في التخزين الدائم SharedPreferences مع إعادة جدولة الصلوات
     */
    fun saveToPrefs(context: Context? = null) {
        val ctx = context?.applicationContext ?: appContext
        val sp = prefs ?: ctx?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)?.also { prefs = it }

        sp?.edit()?.apply {
            val currentLoc = _locationData.value
            val cleanedCity = cleanCityName(currentLoc.cityName)
            putString(KEY_CITY, cleanedCity)
            putString(KEY_COUNTRY, currentLoc.countryName)
            putFloat(KEY_LAT, currentLoc.latitude.toFloat())
            putFloat(KEY_LNG, currentLoc.longitude.toFloat())
            putBoolean(KEY_IS_AUTO, currentLoc.isAuto)
            commit() // كتابة متزامنة وفورية على القرص لمنع فقدان الموقع في الخلفية
        }

        ctx?.let { c ->
            val loc = _locationData.value
            PrayerTimeManager.scheduleAllPrayers(c, loc.latitude, loc.longitude)
            OngoingPrayerNotificationManager.updateNotification(c)
        }
    }
}
