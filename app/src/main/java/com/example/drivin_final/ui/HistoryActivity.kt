package com.example.drivin_final.ui

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.drivin_final.R
import com.example.drivin_final.data.repository.DriverRepository
import com.example.drivin_final.data.repository.DrivingEvent
import com.example.drivin_final.data.repository.DrivingEventType
import com.example.drivin_final.data.repository.DrivingSession
import com.example.drivin_final.ui.adapter.SessionAdapter
import com.example.drivin_final.ui.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class HistoryActivity : AppCompatActivity() {

    @Inject
    lateinit var repository: DriverRepository
    
    private val viewModel: MainViewModel by viewModels()
    private lateinit var sessionAdapter: SessionAdapter
    private lateinit var averageScoreTextView: TextView
    private lateinit var totalSessionsTextView: TextView
    private lateinit var mostRecentSessionTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)
        
        initializeViews()
        setupRecyclerView()
        setupObservers()
        
        // Set up title and back button
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "Driving History"
        }
    }
    
    private fun initializeViews() {
        averageScoreTextView = findViewById(R.id.tvAverageScore)
        totalSessionsTextView = findViewById(R.id.tvTotalSessions)
        mostRecentSessionTextView = findViewById(R.id.tvMostRecentSession)
    }
    
    private fun setupRecyclerView() {
        val sessionsRecyclerView: RecyclerView = findViewById(R.id.rvAllSessions)
        sessionAdapter = SessionAdapter(repository)
        sessionsRecyclerView.layoutManager = LinearLayoutManager(this)
        sessionsRecyclerView.adapter = sessionAdapter
        
        // Set click listener for session items
        sessionAdapter.setOnItemClickListener { session ->
            showSessionDetails(session)
        }
    }
    
    private fun setupObservers() {
        viewModel.allSessions.observe(this) { sessions ->
            // Update adapter
            sessionAdapter.updateSessions(sessions)
            
            // Update summary statistics
            updateSummaryStatistics(sessions)
        }
    }
    
    private fun updateSummaryStatistics(sessions: List<DrivingSession>) {
        if (sessions.isEmpty()) {
            averageScoreTextView.text = "No data"
            totalSessionsTextView.text = "0 sessions"
            mostRecentSessionTextView.text = "No recent sessions"
            return
        }
        
        // Calculate average score
        val averageScore = sessions.map { it.safetyScore }.average()
        averageScoreTextView.text = "%.1f".format(averageScore)
        
        // Set total sessions
        totalSessionsTextView.text = "${sessions.size} sessions"
        
        // Most recent session
        val mostRecent = sessions.maxByOrNull { it.date.time }
        mostRecent?.let {
            mostRecentSessionTextView.text = repository.formatSessionDate(it.date)
        }
    }
    
    private fun showSessionDetails(session: DrivingSession) {
        // Create a dialog to show session details
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setTitle("Session: ${repository.formatSessionDate(session.date)}")
        
        // Create a ScrollView to hold the content
        val scrollView = ScrollView(this)
        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )
        scrollView.layoutParams = layoutParams
        
        // Create the content layout
        val contentLayout = LinearLayout(this)
        contentLayout.orientation = LinearLayout.VERTICAL
        contentLayout.setPadding(30, 30, 30, 30)
        
        // Add score information
        val scoreText = TextView(this)
        scoreText.text = "Safety Score: ${session.safetyScore}"
        scoreText.textSize = 18f
        scoreText.setTextColor(getScoreColor(session.safetyScore))
        contentLayout.addView(scoreText)
        
        // Add event count
        val eventCountText = TextView(this)
        eventCountText.text = "Events Detected: ${session.events.size}"
        eventCountText.textSize = 16f
        eventCountText.setPadding(0, 20, 0, 20)
        contentLayout.addView(eventCountText)
        
        // Add list of events if there are any
        if (session.events.isNotEmpty()) {
            val eventsTitle = TextView(this)
            eventsTitle.text = "Issues Detected:"
            eventsTitle.textSize = 16f
            eventsTitle.setTypeface(null, Typeface.BOLD)
            contentLayout.addView(eventsTitle)
            
            // Group by event type for better organization
            val groupedEvents = session.events.groupBy { it.type }
            
            // Display fatal events first
            addEventTypeGroup(contentLayout, groupedEvents[DrivingEventType.FATAL], "Critical")
            
            // Then display serious events
            addEventTypeGroup(contentLayout, groupedEvents[DrivingEventType.SERIOUS], "Serious")
            
            // Finally display rookie events
            addEventTypeGroup(contentLayout, groupedEvents[DrivingEventType.ROOKIE], "Minor")
        }
        
        // Add worst event if exists
        val worstEvent = repository.getWorstEventInSession(session.id)
        if (worstEvent != null) {
            val worstEventTitle = TextView(this)
            worstEventTitle.text = "Most Critical Issue:"
            worstEventTitle.textSize = 16f
            worstEventTitle.setTypeface(null, Typeface.BOLD)
            worstEventTitle.setPadding(0, 20, 0, 10)
            contentLayout.addView(worstEventTitle)
            
            val worstEventText = TextView(this)
            worstEventText.text = worstEvent.description
            worstEventText.textSize = 14f
            if (worstEvent.type == DrivingEventType.FATAL) {
                worstEventText.setTextColor(Color.RED)
            }
            contentLayout.addView(worstEventText)
            
            val suggestionText = TextView(this)
            suggestionText.text = "Suggestion: ${worstEvent.suggestionForImprovement}"
            suggestionText.textSize = 14f
            suggestionText.setTypeface(null, Typeface.ITALIC)
            suggestionText.setPadding(0, 10, 0, 0)
            contentLayout.addView(suggestionText)
        }
        
        // Attach content layout to scroll view
        scrollView.addView(contentLayout)
        
        // Set up dialog buttons
        dialogBuilder.setView(scrollView)
        dialogBuilder.setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
        dialogBuilder.setNeutralButton("Export Report") { dialog, _ ->
            exportSessionReport(session)
        }
        
        // Show the dialog
        dialogBuilder.create().show()
    }
    
    private fun getScoreColor(score: Int): Int {
        return when {
            score >= 90 -> Color.parseColor("#4CAF50") // Green
            score >= 70 -> Color.parseColor("#FFC107") // Yellow
            else -> Color.parseColor("#F44336") // Red
        }
    }
    
    private fun addEventTypeGroup(layout: LinearLayout, events: List<DrivingEvent>?, typeLabel: String) {
        if (events.isNullOrEmpty()) return
        
        val typeText = TextView(this)
        typeText.text = "$typeLabel Issues (${events.size})"
        typeText.textSize = 14f
        typeText.setTypeface(null, Typeface.BOLD)
        typeText.setPadding(20, 10, 0, 5)
        layout.addView(typeText)
        
        events.forEach { event ->
            val eventText = TextView(this)
            eventText.text = "• ${event.description}"
            eventText.textSize = 14f
            eventText.setPadding(40, 5, 0, 5)
            layout.addView(eventText)
        }
    }
    
    private fun exportSessionReport(session: DrivingSession) {
        val report = repository.exportSessionSummary(session.id)
        
        // Create intent to share the report
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "text/plain"
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Driving Report - ${repository.formatSessionDate(session.date)}")
        shareIntent.putExtra(Intent.EXTRA_TEXT, report)
        
        // Start the sharing activity
        startActivity(Intent.createChooser(shareIntent, "Share Report Via"))
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
} 