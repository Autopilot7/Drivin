package com.example.drivin_final.ui.viewmodel

import android.media.MediaPlayer
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.drivin_final.data.repository.DriverRepository
import com.example.drivin_final.data.repository.DrivingEvent
import com.example.drivin_final.data.repository.DrivingEventType
import com.example.drivin_final.data.repository.DrivingSession
import com.example.drivin_final.data.repository.EventCategory
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: DriverRepository
) : ViewModel() {

    // LiveData for real-time safety score updates
    private val _safetyScore = MutableLiveData<Int>(repository.computeSafetyScore())
    val safetyScore: LiveData<Int> = _safetyScore

    // LiveData for personalized feedback
    private val _personalizedFeedback = MutableLiveData<String>(repository.getPersonalizedFeedback())
    val personalizedFeedback: LiveData<String> = _personalizedFeedback

    // For improvement suggestions
    private val _improvementSuggestions = MutableLiveData<List<String>>(repository.getKeySuggestionsForImprovement())
    val improvementSuggestions: LiveData<List<String>> = _improvementSuggestions

    // LiveData for current driving events
    private val _currentEvents = MutableLiveData<List<DrivingEvent>>(repository.getCurrentEvents())
    val currentEvents: LiveData<List<DrivingEvent>> = _currentEvents

    // LiveData for all driving sessions
    private val _allSessions = MutableLiveData<List<DrivingSession>>(repository.getAllSessions())
    val allSessions: LiveData<List<DrivingSession>> = _allSessions

    // Alert messages for UI display
    private val _alertMessage = MutableLiveData<AlertMessage?>(null)
    val alertMessage: LiveData<AlertMessage?> = _alertMessage

    // Track if a dangerous behavior is currently being detected
    private val _dangerousBehaviorDetected = MutableLiveData<DangerousBehavior?>(null)
    val dangerousBehaviorDetected: LiveData<DangerousBehavior?> = _dangerousBehaviorDetected

    // Refresh all data from repository
    fun refreshData() {
        _safetyScore.value = repository.computeSafetyScore()
        _personalizedFeedback.value = repository.getPersonalizedFeedback()
        _improvementSuggestions.value = repository.getKeySuggestionsForImprovement()
        _currentEvents.value = repository.getCurrentEvents()
        _allSessions.value = repository.getAllSessions()
    }

    // Add a new driving event and update scores
    fun addDrivingEvent(event: DrivingEvent) {
        repository.addDrivingEvent(event)
        refreshData()
        
        // Show alert for serious or fatal events
        if (event.type != DrivingEventType.ROOKIE) {
            triggerAlert(event)
        }
    }

    // End the current driving session
    fun endCurrentSession(): DrivingSession {
        val session = repository.endCurrentSession()
        _allSessions.value = repository.getAllSessions()
        return session
    }

    // Trigger alert based on the detected event
    private fun triggerAlert(event: DrivingEvent) {
        val alertType = when(event.type) {
            DrivingEventType.SERIOUS -> AlertType.WARNING
            DrivingEventType.FATAL -> AlertType.DANGER
            else -> AlertType.INFO
        }
        
        _alertMessage.value = AlertMessage(
            message = event.description,
            type = alertType,
            suggestion = event.suggestionForImprovement
        )
    }

    // Clear the current alert
    fun clearAlert() {
        _alertMessage.value = null
    }

    // Report a dangerous behavior from camera detection
    fun reportDangerousBehavior(behavior: DangerousBehavior) {
        _dangerousBehaviorDetected.value = behavior
        
        // Create a corresponding driving event
        val event = DrivingEvent(
            type = behavior.severity,
            description = behavior.description,
            category = behavior.category,
            suggestionForImprovement = behavior.suggestion
        )
        
        addDrivingEvent(event)
    }

    // Clear dangerous behavior detection
    fun clearDangerousBehaviorDetection() {
        _dangerousBehaviorDetected.value = null
    }
    
    // For demo purposes - trigger a simulated alert
    fun triggerDemoAlert(message: String, suggestion: String) {
        // Create a random alert type, weighted toward warnings
        val alertType = when ((0..10).random()) {
            in 0..7 -> AlertType.WARNING  // 80% chance
            in 8..9 -> AlertType.DANGER   // 20% chance
            else -> AlertType.INFO        // 10% chance
        }
        
        // Determine appropriate event type and category
        val eventType = when(alertType) {
            AlertType.WARNING -> DrivingEventType.SERIOUS
            AlertType.DANGER -> DrivingEventType.FATAL
            else -> DrivingEventType.ROOKIE
        }
        
        // Choose a random category for the event
        val categories = EventCategory.values()
        val randomCategory = categories.random()
        
        // Create and trigger the alert
        _alertMessage.value = AlertMessage(
            message = message,
            type = alertType,
            suggestion = suggestion
        )
        
        // Add a corresponding event
        val event = DrivingEvent(
            type = eventType,
            description = message,
            category = randomCategory,
            suggestionForImprovement = suggestion
        )
        
        addDrivingEvent(event)
    }
}

// Alert message data class
data class AlertMessage(
    val message: String,
    val type: AlertType,
    val suggestion: String
)

// Alert types
enum class AlertType {
    INFO,
    WARNING,
    DANGER
}

// Dangerous behavior detection model
data class DangerousBehavior(
    val description: String,
    val severity: DrivingEventType,
    val category: EventCategory,
    val suggestion: String,
    val requiresImmediateAction: Boolean = false
)
