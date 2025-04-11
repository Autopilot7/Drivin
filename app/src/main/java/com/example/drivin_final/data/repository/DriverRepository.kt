package com.example.drivin_final.data.repository

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

// Define the type of driving events.
enum class DrivingEventType {
    ROOKIE, // Minor issues
    SERIOUS, // More concerning issues
    FATAL // Very dangerous behaviors
}

// Event categories for more detailed analysis
enum class EventCategory {
    ACCELERATION,
    BRAKING,
    TURNING,
    LANE_MANAGEMENT,
    ATTENTION,
    SPEED
}

// A data class representing a driving event with timestamp
data class DrivingEvent(
    val type: DrivingEventType,
    val description: String,
    val category: EventCategory,
    val timestamp: Date = Date(),
    val suggestionForImprovement: String
)

// Data class for driving session
data class DrivingSession(
    val id: String,
    val date: Date,
    val safetyScore: Int,
    val events: List<DrivingEvent>,
    val duration: Long // in minutes
)

@Singleton
class DriverRepository @Inject constructor() {
    
    // Store driving sessions
    private val drivingSessions = mutableListOf<DrivingSession>()
    
    // Current driving events for the active session
    private var currentDrivingEvents = mutableListOf<DrivingEvent>()

    // Hard-coded list of driving events (simulating historical logs).
    init {
        // Initialize with sample data
        currentDrivingEvents = mutableListOf(
            DrivingEvent(
                DrivingEventType.ROOKIE, 
                "Braking too suddenly", 
                EventCategory.BRAKING,
                suggestionForImprovement = "Apply brakes earlier and more gradually to ensure smoother stops."
            ),
            DrivingEvent(
                DrivingEventType.FATAL, 
                "Risk of collision due to abrupt lane change", 
                EventCategory.LANE_MANAGEMENT,
                suggestionForImprovement = "Signal earlier and check blind spots before changing lanes. Maintain safe distance from other vehicles."
            ),
            DrivingEvent(
                DrivingEventType.ROOKIE, 
                "Minor swerving detected", 
                EventCategory.LANE_MANAGEMENT,
                suggestionForImprovement = "Keep both hands on the wheel and maintain focus on staying centered in your lane."
            ),
            DrivingEvent(
                DrivingEventType.SERIOUS, 
                "Potential distraction detected", 
                EventCategory.ATTENTION,
                suggestionForImprovement = "Avoid looking away from the road. If you need to check something, pull over safely first."
            ),
            DrivingEvent(
                DrivingEventType.FATAL, 
                "High-speed maneuver in heavy traffic", 
                EventCategory.SPEED,
                suggestionForImprovement = "Adjust your speed according to traffic conditions and maintain safe following distance."
            ),
            DrivingEvent(
                DrivingEventType.SERIOUS, 
                "Sharp turn at high speed", 
                EventCategory.TURNING,
                suggestionForImprovement = "Slow down before entering turns and accelerate gradually when exiting."
            )
        )
        
        // Add a sample past driving session
        val pastEvents = listOf(
            DrivingEvent(
                DrivingEventType.ROOKIE, 
                "Accelerated too quickly", 
                EventCategory.ACCELERATION,
                Date(System.currentTimeMillis() - 86400000), // 1 day ago
                suggestionForImprovement = "Accelerate more gradually, especially when starting from a stop."
            ),
            DrivingEvent(
                DrivingEventType.SERIOUS, 
                "Tailgating detected", 
                EventCategory.SPEED,
                Date(System.currentTimeMillis() - 86400000),
                suggestionForImprovement = "Maintain at least a 3-second following distance from the vehicle ahead."
            )
        )
        
        drivingSessions.add(
            DrivingSession(
                id = "session-001",
                date = Date(System.currentTimeMillis() - 86400000),
                safetyScore = 85,
                events = pastEvents,
                duration = 35 // 35 minutes
            )
        )
    }

    fun getCurrentEvents(): List<DrivingEvent> = currentDrivingEvents

    fun getAllSessions(): List<DrivingSession> = drivingSessions

    // Add a new driving event to the current session
    fun addDrivingEvent(event: DrivingEvent) {
        currentDrivingEvents.add(event)
    }

    // End the current driving session and save it
    fun endCurrentSession(): DrivingSession {
        val session = DrivingSession(
            id = "session-${drivingSessions.size + 1}",
            date = Date(),
            safetyScore = computeSafetyScore(),
            events = currentDrivingEvents.toList(),
            duration = 45 // Simulated 45 minutes for demo
        )
        drivingSessions.add(session)
        return session
    }

    // Computes the safety score with more nuanced calculations:
    // - 2 points for rookie mistakes
    // - 5 points for serious issues
    // - 10 points for fatal mistakes
    fun computeSafetyScore(): Int {
        var score = 100
        for (event in currentDrivingEvents) {
            when (event.type) {
                DrivingEventType.ROOKIE -> score -= 2
                DrivingEventType.SERIOUS -> score -= 5
                DrivingEventType.FATAL -> score -= 10
            }
        }
        if (score < 0) score = 0
        return score
    }

    // Get event count by category
    fun getEventCountByCategory(category: EventCategory): Int {
        return currentDrivingEvents.count { it.category == category }
    }

    // Get most common issue category
    fun getMostCommonIssueCategory(): EventCategory? {
        return currentDrivingEvents
            .groupBy { it.category }
            .maxByOrNull { it.value.size }
            ?.key
    }

    // Get all improvement suggestions for current session
    fun getAllImprovementSuggestions(): List<String> {
        return currentDrivingEvents.map { it.suggestionForImprovement }.distinct()
    }

    // Provides detailed personalized feedback based on the computed score and specific issues.
    fun getPersonalizedFeedback(): String {
        val score = computeSafetyScore()
        val mostCommonIssue = getMostCommonIssueCategory()
        
        val baseMessage = when {
            score >= 90 -> "Excellent driving! Your safety awareness is commendable."
            score >= 80 -> "Good driving overall, with a few areas for improvement."
            score >= 70 -> "Moderate driving performance. Several risky behaviors were detected."
            score >= 60 -> "Concerning driving patterns detected. Please review safety recommendations."
            else -> "High-risk driving behaviors detected. Immediate improvement is necessary for your safety."
        }
        
        val specificFeedback = when (mostCommonIssue) {
            EventCategory.ACCELERATION -> "You tend to accelerate too aggressively, which can reduce vehicle control and increase fuel consumption."
            EventCategory.BRAKING -> "Your braking pattern is abrupt. This increases wear on your vehicle and risk of rear-end collisions."
            EventCategory.TURNING -> "You take turns too sharply or at excessive speeds, increasing rollover risk."
            EventCategory.LANE_MANAGEMENT -> "Your lane management needs improvement. Proper signaling and smooth transitions are essential."
            EventCategory.ATTENTION -> "Distraction was your most common issue. Always keep your eyes on the road and hands on the wheel."
            EventCategory.SPEED -> "Speeding and improper speed for conditions were frequent issues. Always observe speed limits."
            else -> ""
        }
        
        return if (specificFeedback.isEmpty()) baseMessage else "$baseMessage $specificFeedback"
    }
    
    // Get key improvement suggestions based on the most common issues
    fun getKeySuggestionsForImprovement(): List<String> {
        val mostCommonCategory = getMostCommonIssueCategory() ?: return emptyList()
        
        return currentDrivingEvents
            .filter { it.category == mostCommonCategory }
            .map { it.suggestionForImprovement }
            .distinct()
            .take(3) // Limit to top 3 suggestions
    }
    
    // Format session date for display
    fun formatSessionDate(date: Date): String {
        val formatter = SimpleDateFormat("MMM dd, yyyy - HH:mm", Locale.getDefault())
        return formatter.format(date)
    }

    // Add additional history functionality
    
    // Get average safety score across all sessions
    fun getAverageSafetyScore(): Float {
        if (drivingSessions.isEmpty()) return 0f
        return drivingSessions.map { it.safetyScore }.average().toFloat()
    }
    
    // Get the most common issue category across all sessions
    fun getMostCommonIssueCategoryAcrossSessions(): EventCategory? {
        if (drivingSessions.isEmpty()) return null
        
        val allEvents = drivingSessions.flatMap { it.events }
        return allEvents
            .groupBy { it.category }
            .maxByOrNull { it.value.size }
            ?.key
    }
    
    // Export a session summary as text
    fun exportSessionSummary(sessionId: String): String {
        val session = drivingSessions.find { it.id == sessionId } ?: return "Session not found"
        
        val builder = StringBuilder()
        builder.append("DRIVING SESSION REPORT\n")
        builder.append("=====================\n\n")
        builder.append("Date: ${formatSessionDate(session.date)}\n")
        builder.append("Duration: ${session.duration} minutes\n")
        builder.append("Safety Score: ${session.safetyScore}\n\n")
        
        builder.append("EVENTS DETECTED:\n")
        if (session.events.isEmpty()) {
            builder.append("No events detected during this session.\n")
        } else {
            // Group events by category
            val eventsByCategory = session.events.groupBy { it.category }
            eventsByCategory.forEach { (category, events) ->
                builder.append("\n${category.name} (${events.size}):\n")
                events.forEach { event ->
                    val severity = when (event.type) {
                        DrivingEventType.ROOKIE -> "Minor"
                        DrivingEventType.SERIOUS -> "Serious"
                        DrivingEventType.FATAL -> "Critical"
                    }
                    builder.append("- [$severity] ${event.description}\n")
                }
            }
            
            builder.append("\nSUGGESTIONS FOR IMPROVEMENT:\n")
            val suggestions = session.events.map { it.suggestionForImprovement }.distinct()
            suggestions.forEach { suggestion ->
                builder.append("- $suggestion\n")
            }
        }
        
        return builder.toString()
    }
    
    // Get driving trends showing improvement or deterioration
    fun getDrivingTrend(): String {
        if (drivingSessions.size < 2) return "Not enough data to establish a trend."
        
        // Sort sessions by date
        val sortedSessions = drivingSessions.sortedBy { it.date }
        
        // Calculate score differences
        val scoreDeltas = mutableListOf<Int>()
        for (i in 1 until sortedSessions.size) {
            scoreDeltas.add(sortedSessions[i].safetyScore - sortedSessions[i-1].safetyScore)
        }
        
        // Calculate average improvement/deterioration
        val avgChange = scoreDeltas.average()
        
        return when {
            avgChange > 5 -> "Your driving is showing significant improvement!"
            avgChange > 0 -> "Your driving is gradually improving."
            avgChange == 0.0 -> "Your driving performance is consistent."
            avgChange > -5 -> "Your driving has slightly deteriorated."
            else -> "Your driving has significantly deteriorated. Please review the safety suggestions."
        }
    }
    
    // Find the worst driving event in a session
    fun getWorstEventInSession(sessionId: String): DrivingEvent? {
        val session = drivingSessions.find { it.id == sessionId } ?: return null
        
        // Find the most severe event
        val fatalEvents = session.events.filter { it.type == DrivingEventType.FATAL }
        if (fatalEvents.isNotEmpty()) return fatalEvents.first()
        
        val seriousEvents = session.events.filter { it.type == DrivingEventType.SERIOUS }
        if (seriousEvents.isNotEmpty()) return seriousEvents.first()
        
        val rookieEvents = session.events.filter { it.type == DrivingEventType.ROOKIE }
        if (rookieEvents.isNotEmpty()) return rookieEvents.first()
        
        return null
    }
}
