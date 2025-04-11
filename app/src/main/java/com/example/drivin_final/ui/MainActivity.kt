package com.example.drivin_final.ui

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.drivin_final.R
import com.example.drivin_final.data.repository.DriverRepository
import com.example.drivin_final.ui.adapter.SessionAdapter
import com.example.drivin_final.ui.adapter.SuggestionAdapter
import com.example.drivin_final.ui.viewmodel.AlertType
import com.example.drivin_final.ui.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var repository: DriverRepository
    
    // Obtain the MainViewModel via Hilt.
    private val viewModel: MainViewModel by viewModels()
    
    // UI elements
    private lateinit var scoreTextView: TextView
    private lateinit var feedbackTextView: TextView
    private lateinit var alertPanel: CardView
    private lateinit var alertTitleTextView: TextView
    private lateinit var alertMessageTextView: TextView
    private lateinit var alertSuggestionTextView: TextView
    
    // Adapters
    private lateinit var suggestionAdapter: SuggestionAdapter
    private lateinit var sessionAdapter: SessionAdapter
    
    // Media player for sound alerts
    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize UI components
        initializeViews()
        setupRecyclerViews()
        setupButtonListeners()
        setupObservers()
    }
    
    private fun initializeViews() {
        scoreTextView = findViewById(R.id.tvScore)
        feedbackTextView = findViewById(R.id.tvFeedback)
        alertPanel = findViewById(R.id.alertPanel)
        alertTitleTextView = findViewById(R.id.tvAlertTitle)
        alertMessageTextView = findViewById(R.id.tvAlertMessage)
        alertSuggestionTextView = findViewById(R.id.tvAlertSuggestion)
    }
    
    private fun setupRecyclerViews() {
        // Setup suggestion recycler view
        val suggestionsRecyclerView: RecyclerView = findViewById(R.id.rvSuggestions)
        suggestionAdapter = SuggestionAdapter()
        suggestionsRecyclerView.layoutManager = LinearLayoutManager(this)
        suggestionsRecyclerView.adapter = suggestionAdapter
        
        // Setup sessions recycler view
        val sessionsRecyclerView: RecyclerView = findViewById(R.id.rvSessions)
        sessionAdapter = SessionAdapter(repository)
        sessionsRecyclerView.layoutManager = LinearLayoutManager(this)
        sessionsRecyclerView.adapter = sessionAdapter
        
        // Set click listener for session items
        sessionAdapter.setOnItemClickListener { session ->
            Toast.makeText(this, "Session from ${repository.formatSessionDate(session.date)} selected", Toast.LENGTH_SHORT).show()
            // You could navigate to a detailed view of the session here
        }
    }
    
    private fun setupButtonListeners() {
        // Camera detection button
        findViewById<Button>(R.id.btnCamera).setOnClickListener {
            startActivity(Intent(this, DriverBehaviorCameraActivity::class.java))
        }
        
        // History button - future implementation could show a dedicated history screen
        findViewById<Button>(R.id.btnHistory).setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }
        
        // Dismiss alert button
        findViewById<Button>(R.id.btnDismissAlert).setOnClickListener {
            viewModel.clearAlert()
            stopAlertSound()
        }
        
        // Simulate alert (for demo purposes)
        findViewById<Button>(R.id.btnAlert).setOnClickListener {
            simulateRandomAlert()
        }
    }
    
    private fun setupObservers() {
        // Observe safety score updates
        viewModel.safetyScore.observe(this) { score ->
            scoreTextView.text = score.toString()
            
            // Update color based on score
            val color = when {
                score >= 90 -> "#4CAF50" // Green
                score >= 70 -> "#FFC107" // Yellow
                else -> "#F44336" // Red
            }
            scoreTextView.setTextColor(android.graphics.Color.parseColor(color))
        }
        
        // Observe personalized feedback updates
        viewModel.personalizedFeedback.observe(this) { feedback ->
            feedbackTextView.text = feedback
        }
        
        // Observe improvement suggestions
        viewModel.improvementSuggestions.observe(this) { suggestions ->
            suggestionAdapter.updateSuggestions(suggestions)
        }
        
        // Observe driving sessions
        viewModel.allSessions.observe(this) { sessions ->
            sessionAdapter.updateSessions(sessions)
        }
        
        // Observe alert messages
        viewModel.alertMessage.observe(this) { alertMessage ->
            if (alertMessage != null) {
                // Configure and show alert
                alertTitleTextView.text = when (alertMessage.type) {
                    AlertType.WARNING -> "WARNING"
                    AlertType.DANGER -> "DANGER"
                    else -> "INFORMATION"
                }
                
                // Set background color based on alert type
                val backgroundColor = when (alertMessage.type) {
                    AlertType.WARNING -> "#FFC107" // Yellow
                    AlertType.DANGER -> "#F44336" // Red
                    else -> "#2196F3" // Blue
                }
                alertPanel.setCardBackgroundColor(android.graphics.Color.parseColor(backgroundColor))
                
                alertMessageTextView.text = alertMessage.message
                alertSuggestionTextView.text = alertMessage.suggestion
                alertPanel.visibility = View.VISIBLE
                
                // Play appropriate sound
                playAlertSound(alertMessage.type)
            } else {
                // Hide alert
                alertPanel.visibility = View.GONE
                stopAlertSound()
            }
        }
    }
    
    private fun simulateRandomAlert() {
        val demoAlerts = listOf(
            Pair("Distracted driving detected", "Keep your eyes on the road at all times"),
            Pair("Excessive speed detected", "Slow down and observe the speed limit"),
            Pair("Signs of drowsiness detected", "Consider taking a break or pulling over to rest")
        )
        
        val randomAlert = demoAlerts.random()
        
        // Show a simulated alert
        Toast.makeText(this, "Simulating alert: ${randomAlert.first}", Toast.LENGTH_SHORT).show()
        
        // Trigger a proper alert through the ViewModel
        viewModel.triggerDemoAlert(randomAlert.first, randomAlert.second)
    }
    
    private fun playAlertSound(alertType: AlertType) {
        // Stop any existing sound
        stopAlertSound()
        
        // Select the appropriate sound resource
        val soundResource = when (alertType) {
            AlertType.WARNING -> R.raw.warning_sound
            AlertType.DANGER -> R.raw.danger_sound
            else -> R.raw.info_sound
        }
        
        // Play the sound
        try {
            mediaPlayer = MediaPlayer.create(this, soundResource)
            if (mediaPlayer == null) {
                // Fallback if the MediaPlayer couldn't create the sound (possibly due to empty/invalid sound files)
                Toast.makeText(this, "Alert: ${alertType.name}", Toast.LENGTH_SHORT).show()
                return
            }
            mediaPlayer?.start()
        } catch (e: Exception) {
            // Fallback to a simple Toast if sound cannot be played
            Toast.makeText(this, "Alert: ${alertType.name}", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun stopAlertSound() {
        mediaPlayer?.apply {
            if (isPlaying) {
                stop()
            }
            release()
        }
        mediaPlayer = null
    }
    
    override fun onDestroy() {
        super.onDestroy()
        stopAlertSound()
    }
}
