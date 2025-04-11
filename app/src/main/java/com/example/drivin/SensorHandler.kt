package com.example.drivin

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf

// Object (hoặc class) quản lý sensor
object SensorHandler : SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null

    // Biến lưu giá trị accelerometer trước đó
    private var prevX: Float = 0f
    private var prevY: Float = 0f

    // Ngưỡng so sánh để xác định hành vi nguy hiểm
    private const val brakeThreshold: Float = 1.0f
    private const val directionThreshold: Float = 0.8f

    // State để lưu điểm an toàn (safeScore)
    var safeScoreState: MutableState<Int> = mutableStateOf(100)
    
    // Counters for dangerous driving behaviors
    var suddenBrakesCount: MutableState<Int> = mutableStateOf(0)
    var suddenAccelerationCount: MutableState<Int> = mutableStateOf(0)
    var suddenDirectionChangesCount: MutableState<Int> = mutableStateOf(0)

    // Khởi tạo sensor ở đây, cần truyền vào Context
    fun init(context: Context) {
        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    }

    // Đăng ký SensorEventListener
    fun register() {
        accelerometer?.also { sensor ->
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    // Hủy đăng ký SensorEventListener
    fun unregister() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            val currentX = it.values[0]
            val currentY = it.values[1]
            
            // Check for sudden braking (significant negative Y change)
            if ((prevY - currentY) >= brakeThreshold) {
                safeScoreState.value = (safeScoreState.value - 5).coerceAtLeast(0)
                suddenBrakesCount.value++
            } 
            
            // Check for sudden acceleration (significant positive Y change)
            else if ((currentY - prevY) >= brakeThreshold) {
                safeScoreState.value = (safeScoreState.value - 3).coerceAtLeast(0)
                suddenAccelerationCount.value++
            }
            
            // Check for sudden direction changes (significant X change)
            if (Math.abs(currentX - prevX) >= directionThreshold) {
                safeScoreState.value = (safeScoreState.value - 2).coerceAtLeast(0)
                suddenDirectionChangesCount.value++
            }
            
            prevX = currentX
            prevY = currentY
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Không cần xử lý trong demo này.
    }
    
    // Reset all counters
    fun resetCounters() {
        suddenBrakesCount.value = 0
        suddenAccelerationCount.value = 0
        suddenDirectionChangesCount.value = 0
    }
}
