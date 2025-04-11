package com.example.drivin_final.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.drivin_final.R
import com.example.drivin_final.data.repository.DrivingSession
import com.example.drivin_final.data.repository.DriverRepository
import javax.inject.Inject

class SessionAdapter @Inject constructor(
    private val repository: DriverRepository,
    private var sessions: List<DrivingSession> = emptyList()
) : RecyclerView.Adapter<SessionAdapter.SessionViewHolder>() {

    private var onItemClickListener: ((DrivingSession) -> Unit)? = null

    class SessionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dateText: TextView = itemView.findViewById(R.id.tvSessionDate)
        val scoreText: TextView = itemView.findViewById(R.id.tvSessionScore)
        val durationText: TextView = itemView.findViewById(R.id.tvSessionDuration)
        val eventCountText: TextView = itemView.findViewById(R.id.tvEventCount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SessionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_session, parent, false)
        return SessionViewHolder(view)
    }

    override fun getItemCount(): Int = sessions.size

    override fun onBindViewHolder(holder: SessionViewHolder, position: Int) {
        val session = sessions[position]
        
        holder.dateText.text = repository.formatSessionDate(session.date)
        holder.scoreText.text = "Score: ${session.safetyScore}"
        
        // Apply color based on score
        val scoreColor = when {
            session.safetyScore >= 90 -> "#4CAF50" // Green
            session.safetyScore >= 70 -> "#FFC107" // Yellow
            else -> "#F44336" // Red
        }
        holder.scoreText.setTextColor(android.graphics.Color.parseColor(scoreColor))
        
        holder.durationText.text = "${session.duration} min"
        holder.eventCountText.text = "${session.events.size} events"
        
        // Set click listener
        holder.itemView.setOnClickListener {
            onItemClickListener?.invoke(session)
        }
    }

    fun updateSessions(newSessions: List<DrivingSession>) {
        sessions = newSessions
        notifyDataSetChanged()
    }
    
    fun setOnItemClickListener(listener: (DrivingSession) -> Unit) {
        onItemClickListener = listener
    }
} 