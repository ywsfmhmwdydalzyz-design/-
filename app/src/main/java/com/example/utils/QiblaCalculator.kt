package com.example.utils

import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

/**
 * Astronomical and Mathematical calculations for the Qibla (direction towards the Holy Kaaba in Makkah).
 */
object QiblaCalculator {

    // Precise coordinates of the Holy Kaaba in Makkah Al-Mukarramah
    const val MAKKAH_LATITUDE = 21.422487
    const val MAKKAH_LONGITUDE = 39.826206

    /**
     * Calculates the true bearing in degrees [0, 360) from any coordinates on Earth to the Holy Kaaba.
     * 0° = North, 90° = East, 180° = South, 270° = West.
     */
    fun calculateQiblaBearing(userLat: Double, userLng: Double): Float {
        val phi1 = Math.toRadians(userLat)
        val phi2 = Math.toRadians(MAKKAH_LATITUDE)
        val deltaLambda = Math.toRadians(MAKKAH_LONGITUDE - userLng)

        val y = sin(deltaLambda)
        val x = cos(phi1) * tan(phi2) - sin(phi1) * cos(deltaLambda)
        val qiblaRad = atan2(y, x)
        val qiblaDeg = Math.toDegrees(qiblaRad)

        return (((qiblaDeg + 360.0) % 360.0)).toFloat()
    }

    /**
     * Calculates the great-circle distance to the Holy Kaaba in kilometers using the Haversine formula.
     */
    fun calculateDistanceToMakkahKm(userLat: Double, userLng: Double): Double {
        val r = 6371.0 // Earth's mean radius in km
        val dLat = Math.toRadians(MAKKAH_LATITUDE - userLat)
        val dLng = Math.toRadians(MAKKAH_LONGITUDE - userLng)
        val phi1 = Math.toRadians(userLat)
        val phi2 = Math.toRadians(MAKKAH_LATITUDE)

        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(phi1) * cos(phi2) *
                sin(dLng / 2) * sin(dLng / 2)
        val c = 2 * asin(sqrt(a))
        return r * c
    }

    /**
     * Translates bearing in degrees to cardinal Arabic description.
     */
    fun getDirectionArabic(bearing: Float): String {
        val normalized = ((bearing % 360f) + 360f) % 360f
        return when {
            normalized in 337.5f..360f || normalized in 0f..22.5f -> "شمال"
            normalized in 22.5f..67.5f -> "شمال شرق"
            normalized in 67.5f..112.5f -> "شرق"
            normalized in 112.5f..157.5f -> "جنوب شرق"
            normalized in 157.5f..202.5f -> "جنوب"
            normalized in 202.5f..247.5f -> "جنوب غرب"
            normalized in 247.5f..292.5f -> "غرب"
            else -> "شمال غرب"
        }
    }
}
