package com.example.drivin_final.data.model

import java.util.Date

/**
 * Represents the severity level of a detection event
 */
enum class SeverityLevel {
    WARNING,  // Initial detection, low severity
    SERIOUS,  // Persistent detection, medium severity
    FATAL     // Critical detection, high severity
}

/**
 * Represents the type of detection event
 */
enum class DetectionType {
    DROWSINESS,   // Driver drowsiness detection
    DISTRACTION   // Driver distraction detection
}

/**
 * Represents a detection event with metadata
 */
data class DetectionEvent(
    val type: DetectionType,
    val severity: SeverityLevel,
    val timestamp: Date = Date(),
    val confidence: Float = 1.0f,
    val metadata: Map<String, Any> = emptyMap()
) 