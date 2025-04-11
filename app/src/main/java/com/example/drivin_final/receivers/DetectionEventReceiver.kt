package com.example.drivin_final.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import com.example.drivin_final.ui.viewmodel.MainViewModel

/**
 * BroadcastReceiver that handles driver behavior detection events from the camera
 * and forwards them to the MainViewModel to trigger appropriate alerts.
 */
class DetectionEventReceiver(private val viewModel: MainViewModel) : BroadcastReceiver() {

    companion object {
        private const val TAG = "DetectionEventReceiver"
        const val ACTION_DETECTION_EVENT = "com.example.drivin_final.DETECTION_EVENT"
        
        // Event types
        const val EVENT_TYPE_DROWSINESS = "DROWSINESS"
        const val EVENT_TYPE_DISTRACTION = "DISTRACTION"
        
        // Severity levels
        const val SEVERITY_WARNING = "WARNING"
        const val SEVERITY_SERIOUS = "SERIOUS"
        const val SEVERITY_FATAL = "FATAL"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_DETECTION_EVENT) {
            val eventType = intent.getStringExtra("type") ?: return
            val severity = intent.getStringExtra("severity") ?: SEVERITY_WARNING
            
            Log.d(TAG, "Received detection event: $eventType with severity $severity")
            
            when (eventType) {
                EVENT_TYPE_DROWSINESS -> {
                    // Forward drowsiness event to the ViewModel
                    viewModel.reportDrowsiness(severity)
                }
                EVENT_TYPE_DISTRACTION -> {
                    // Forward distraction event to the ViewModel
                    viewModel.reportDistraction(severity)
                }
                else -> {
                    // Handle unknown event types
                    Log.w(TAG, "Unknown detection event type: $eventType")
                }
            }
        }
    }

    /**
     * Register this receiver with the given context
     */
    fun register(context: Context) {
        val filter = IntentFilter(ACTION_DETECTION_EVENT)
        context.registerReceiver(this, filter)
        Log.d(TAG, "Detection event receiver registered")
    }

    /**
     * Unregister this receiver from the given context
     */
    fun unregister(context: Context) {
        try {
            context.unregisterReceiver(this)
            Log.d(TAG, "Detection event receiver unregistered")
        } catch (e: IllegalArgumentException) {
            // Receiver was not registered
            Log.w(TAG, "Attempted to unregister detection receiver that wasn't registered")
        }
    }
} 