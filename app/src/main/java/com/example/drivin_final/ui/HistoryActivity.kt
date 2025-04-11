package com.example.drivin_final.ui

import android.os.Bundle
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.drivin_final.R
import com.example.drivin_final.data.repository.DriverRepository
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
        // In a future implementation, this would navigate to a detailed
        // session view showing all events and metrics from that session
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
} 