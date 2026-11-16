package com.example.utils

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.abs

/**
 * Robust sensor manager integrating hardware Magnetometer, Accelerometer, and Rotation Vector
 * to determine live real-time device heading relative to True/Magnetic North for Qibla calculation.
 */
class QiblaSensorManager(private val context: Context) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    private var rotationVectorSensor: Sensor? = null
    private var accelerometerSensor: Sensor? = null
    private var magnetometerSensor: Sensor? = null

    private val _heading = MutableStateFlow(0f)
    val heading: StateFlow<Float> = _heading.asStateFlow()

    private val _pitch = MutableStateFlow(0f)
    val pitch: StateFlow<Float> = _pitch.asStateFlow()

    private val _sensorAccuracy = MutableStateFlow(SensorManager.SENSOR_STATUS_ACCURACY_HIGH)
    val sensorAccuracy: StateFlow<Int> = _sensorAccuracy.asStateFlow()

    private val _isSensorAvailable = MutableStateFlow(true)
    val isSensorAvailable: StateFlow<Boolean> = _isSensorAvailable.asStateFlow()

    private val _isSimulatedMode = MutableStateFlow(false)
    val isSimulatedMode: StateFlow<Boolean> = _isSimulatedMode.asStateFlow()

    // Sensor measurement buffers
    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)
    private val gravityValues = FloatArray(3)
    private val geomagneticValues = FloatArray(3)
    private var hasGravity = false
    private var hasGeomagnetic = false

    private var lastVibratedHeading: Float? = null

    init {
        rotationVectorSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        accelerometerSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        magnetometerSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        val hasHardwareCompass = rotationVectorSensor != null || (accelerometerSensor != null && magnetometerSensor != null)
        _isSensorAvailable.value = hasHardwareCompass
        if (!hasHardwareCompass) {
            _isSimulatedMode.value = true
        }
    }

    fun startListening() {
        if (sensorManager == null) {
            _isSensorAvailable.value = false
            _isSimulatedMode.value = true
            return
        }

        if (accelerometerSensor != null && magnetometerSensor != null) {
            sensorManager.registerListener(
                this,
                accelerometerSensor,
                SensorManager.SENSOR_DELAY_UI
            )
            sensorManager.registerListener(
                this,
                magnetometerSensor,
                SensorManager.SENSOR_DELAY_UI
            )
        } else if (rotationVectorSensor != null) {
            sensorManager.registerListener(
                this,
                rotationVectorSensor,
                SensorManager.SENSOR_DELAY_UI
            )
        } else {
            _isSensorAvailable.value = false
            _isSimulatedMode.value = true
        }
    }

    fun stopListening() {
        sensorManager?.unregisterListener(this)
    }

    fun setSimulatedHeading(newHeading: Float) {
        _isSimulatedMode.value = true
        _heading.value = ((newHeading % 360f) + 360f) % 360f
    }

    fun toggleSimulatedMode(enabled: Boolean) {
        _isSimulatedMode.value = enabled
        if (!enabled && _isSensorAvailable.value) {
            startListening()
        }
    }

    /**
     * Circular interpolation to avoid 360->0 boundary jumping or spinning
     */
    private fun smoothAngle(current: Float, target: Float, factor: Float = 0.2f): Float {
        var diff = (target - current) % 360f
        if (diff > 180f) diff -= 360f
        if (diff < -180f) diff += 360f
        return ((current + diff * factor) + 360f) % 360f
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null || _isSimulatedMode.value) return

        when (event.sensor.type) {
            Sensor.TYPE_ROTATION_VECTOR -> {
                SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                SensorManager.getOrientation(rotationMatrix, orientationAngles)

                val azimuthRad = orientationAngles[0]
                val pitchRad = orientationAngles[1]

                val targetHeading = ((Math.toDegrees(azimuthRad.toDouble()).toFloat() + 360f) % 360f)
                _heading.value = smoothAngle(_heading.value, targetHeading)
                _pitch.value = Math.toDegrees(pitchRad.toDouble()).toFloat()
            }
            Sensor.TYPE_ACCELEROMETER -> {
                // Low-pass filter for gravity
                val alpha = 0.8f
                gravityValues[0] = alpha * gravityValues[0] + (1 - alpha) * event.values[0]
                gravityValues[1] = alpha * gravityValues[1] + (1 - alpha) * event.values[1]
                gravityValues[2] = alpha * gravityValues[2] + (1 - alpha) * event.values[2]
                hasGravity = true
                calculateOrientationFromSensors()
            }
            Sensor.TYPE_MAGNETIC_FIELD -> {
                val alpha = 0.8f
                geomagneticValues[0] = alpha * geomagneticValues[0] + (1 - alpha) * event.values[0]
                geomagneticValues[1] = alpha * geomagneticValues[1] + (1 - alpha) * event.values[1]
                geomagneticValues[2] = alpha * geomagneticValues[2] + (1 - alpha) * event.values[2]
                hasGeomagnetic = true
                calculateOrientationFromSensors()
            }
        }
    }

    private fun calculateOrientationFromSensors() {
        if (!hasGravity || !hasGeomagnetic) return

        val success = SensorManager.getRotationMatrix(
            rotationMatrix,
            null,
            gravityValues,
            geomagneticValues
        )
        if (success) {
            SensorManager.getOrientation(rotationMatrix, orientationAngles)
            val azimuthRad = orientationAngles[0]
            val pitchRad = orientationAngles[1]

            val targetHeading = ((Math.toDegrees(azimuthRad.toDouble()).toFloat() + 360f) % 360f)
            _heading.value = smoothAngle(_heading.value, targetHeading)
            _pitch.value = Math.toDegrees(pitchRad.toDouble()).toFloat()
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        if (sensor?.type == Sensor.TYPE_MAGNETIC_FIELD || sensor?.type == Sensor.TYPE_ROTATION_VECTOR) {
            _sensorAccuracy.value = accuracy
        }
    }

    /**
     * Triggers a subtle tactile haptic vibration when the user aligns with the Holy Kaaba direction.
     */
    fun triggerQiblaAlignedHaptic() {
        try {
            val current = _heading.value
            // Avoid buzzing repeatedly on the exact same micro-degree
            if (lastVibratedHeading != null && abs(current - (lastVibratedHeading ?: 0f)) < 3f) {
                return
            }
            lastVibratedHeading = current

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(
                    VibrationEffect.createOneShot(45L, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(45L)
            }
        } catch (e: Exception) {
            Log.e("QiblaSensorManager", "Error triggering haptic feedback", e)
        }
    }
}
