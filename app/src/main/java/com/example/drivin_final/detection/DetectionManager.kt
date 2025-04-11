package com.example.drivin_final.detection

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.drivin_final.util.SoundGenerator
import java.util.Date

/**
 * Manager class that handles detection events and corresponding alerts
 */
class DetectionManager(private val context: Context) {
    private val TAG = "DetectionManager"
    private val soundGenerator = SoundGenerator(context)
    
    // Tracking for persistent detection states
    private var lastDrowsinessEvent: DetectionEvent? = null
    private var lastDistractionEvent: DetectionEvent? = null
    
    // Alert threshold times in milliseconds
    private val WARNING_THRESHOLD_MS = 1000L  // 1 second
    private val SERIOUS_THRESHOLD_MS = 3000L  // 3 seconds
    private val FATAL_THRESHOLD_MS = 5000L    // 5 seconds
    
    // Handler for delayed alerts
    private val handler = Handler(Looper.getMainLooper())
    
    /**
     * Process a new detection event and trigger appropriate alerts
     */
    fun processDetectionEvent(event: DetectionEvent) {
        if (!event.isDetected()) {
            resetDetectionState(event.type)
            return
        }
        
        when (event.type) {
            DetectionType.DROWSINESS -> processDrowsinessEvent(event)
            DetectionType.DISTRACTION -> processDistractionEvent(event)
        }
    }
    
    private fun processDrowsinessEvent(event: DetectionEvent) {
        val now = Date()
        
        if (lastDrowsinessEvent == null) {
            // First detection
            lastDrowsinessEvent = event
            soundGenerator.playInfoAlert()
            Log.d(TAG, "Drowsiness detected: Initial warning")
            scheduleEscalatedAlert(DetectionType.DROWSINESS)
        } else {
            // Continued detection
            val duration = now.time - lastDrowsinessEvent!!.timestamp.time
            lastDrowsinessEvent = event.copy(timestamp = event.timestamp, durationMs = duration)
            
            // Alert based on duration
            determineSeverityAndAlert(DetectionType.DROWSINESS, duration)
        }
    }
    
    private fun processDistractionEvent(event: DetectionEvent) {
        val now = Date()
        
        if (lastDistractionEvent == null) {
            // First detection
            lastDistractionEvent = event
            soundGenerator.playInfoAlert()
            Log.d(TAG, "Distraction detected: Initial warning")
            scheduleEscalatedAlert(DetectionType.DISTRACTION)
        } else {
            // Continued detection
            val duration = now.time - lastDistractionEvent!!.timestamp.time
            lastDistractionEvent = event.copy(timestamp = event.timestamp, durationMs = duration)
            
            // Alert based on duration
            determineSeverityAndAlert(DetectionType.DISTRACTION, duration)
        }
    }
    
    private fun determineSeverityAndAlert(type: DetectionType, durationMs: Long) {
        when {
            durationMs >= FATAL_THRESHOLD_MS -> {
                soundGenerator.playDangerAlert()
                Log.d(TAG, "$type detected: FATAL level (${durationMs}ms)")
            }
            durationMs >= SERIOUS_THRESHOLD_MS -> {
                soundGenerator.playWarningAlert()
                Log.d(TAG, "$type detected: SERIOUS level (${durationMs}ms)")
            }
            durationMs >= WARNING_THRESHOLD_MS -> {
                soundGenerator.playInfoAlert()
                Log.d(TAG, "$type detected: WARNING level (${durationMs}ms)")
            }
        }
    }
    
    private fun scheduleEscalatedAlert(type: DetectionType) {
        // Schedule escalated alerts if detection persists
        handler.postDelayed({
            if ((type == DetectionType.DROWSINESS && lastDrowsinessEvent != null) ||
                (type == DetectionType.DISTRACTION && lastDistractionEvent != null)) {
                soundGenerator.playWarningAlert()
                Log.d(TAG, "$type detected: Escalating to warning alert")
            }
        }, SERIOUS_THRESHOLD_MS)
        
        handler.postDelayed({
            if ((type == DetectionType.DROWSINESS && lastDrowsinessEvent != null) ||
                (type == DetectionType.DISTRACTION && lastDistractionEvent != null)) {
                soundGenerator.playDangerAlert()
                Log.d(TAG, "$type detected: Escalating to danger alert")
            }
        }, FATAL_THRESHOLD_MS)
    }
    
    private fun resetDetectionState(type: DetectionType) {
        when (type) {
            DetectionType.DROWSINESS -> {
                if (lastDrowsinessEvent != null) {
                    Log.d(TAG, "Drowsiness state reset")
                    lastDrowsinessEvent = null
                }
            }
            DetectionType.DISTRACTION -> {
                if (lastDistractionEvent != null) {
                    Log.d(TAG, "Distraction state reset")
                    lastDistractionEvent = null
                }
            }
        }
    }
    
    /**
     * Release resources when no longer needed
     */
    fun release() {
        handler.removeCallbacksAndMessages(null)
        soundGenerator.release()
    }
} 