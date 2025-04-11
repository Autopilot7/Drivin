package com.example.drivin_final.detection

import java.util.Date

/**
 * Enum representing the type of detection event
 */
enum class DetectionType {
    DROWSINESS,
    DISTRACTION
}

/**
 * Enum representing the severity level of an alert
 */
enum class SeverityLevel {
    WARNING,   // Initial detection, mild alert
    SERIOUS,   // Persistent detection, stronger alert
    FATAL      // Critical detection, strongest alert
}

/**
 * Data class representing a detection event from the ML model
 */
data class DetectionEvent(
    val type: DetectionType,
    val confidence: Float,
    val timestamp: Date = Date(),
    val durationMs: Long = 0
) {
    /**
     * Returns true if the confidence level is above the detection threshold
     */
    fun isDetected(): Boolean = confidence > DETECTION_THRESHOLD
    
    companion object {
        const val DETECTION_THRESHOLD = 0.75f
    }
} 