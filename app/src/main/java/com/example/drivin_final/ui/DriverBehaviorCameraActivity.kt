package com.example.drivin_final.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.drivin_final.R
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import java.util.*

class DriverBehaviorCameraActivity : AppCompatActivity() {

    private lateinit var previewView: PreviewView
    private lateinit var statusTextView: TextView
    // Visual alert UI components
    private lateinit var behaviorAlertPanel: CardView
    private lateinit var alertTitleTextView: TextView
    private lateinit var alertMessageTextView: TextView
    private lateinit var dismissAlertButton: Button
    
    // Detection counters for determining behavior patterns
    private var consecutiveEyeClosedFrames = 0
    private var consecutiveFaceAwayFrames = 0
    
    // Thresholds for alert levels (in frames, approximately 3-5 frames per second)
    private val WARNING_THRESHOLD = 3  // ~1 second
    private val SERIOUS_THRESHOLD = 9  // ~3 seconds
    private val FATAL_THRESHOLD = 15   // ~5 seconds
    
    // Time between alerts
    private val ALERT_COOLDOWN_MS: Long = 10000  // 10 seconds
    private var lastAlertTime: Long = 0

    companion object {
        private const val CAMERA_PERMISSION_REQUEST_CODE = 1001
        private const val TAG = "DriverDetection"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_driver_behavior_camera)

        previewView = findViewById<PreviewView>(R.id.previewView)
        statusTextView = findViewById<TextView>(R.id.statusTextView)
        
        // Initialize alert panel views
        behaviorAlertPanel = findViewById<CardView>(R.id.behaviorAlertPanel)
        alertTitleTextView = findViewById<TextView>(R.id.alertTitleTextView)
        alertMessageTextView = findViewById<TextView>(R.id.alertMessageTextView)
        dismissAlertButton = findViewById<Button>(R.id.dismissAlertButton)
        
        // Set up dismiss button click listener
        dismissAlertButton.setOnClickListener {
            hideAlertPanel()
        }
        
        // Set up back button click listener
        findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.backButton).setOnClickListener {
            finish() // Close this activity and return to the previous one
        }

        // Initial status
        updateStatus("Initializing camera...")

        // Check for camera permissions.
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED
        ) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()

                // Create a preview use case.
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                // Set up the image analyzer which uses ML Kit's Face Detection.
                val imageAnalyzer = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build().also { analysis ->
                        analysis.setAnalyzer(ContextCompat.getMainExecutor(this)) { imageProxy ->
                            processImageProxy(imageProxy)
                        }
                    }

                // Try to use front camera first, fall back to back camera if front camera isn't available
                val cameraSelector = try {
                    // Check if device has a front camera
                    if (cameraProvider.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA)) {
                        CameraSelector.DEFAULT_FRONT_CAMERA
                    } else {
                        Log.w(TAG, "Front camera not available, falling back to back camera")
                        CameraSelector.DEFAULT_BACK_CAMERA
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Error checking camera availability: ${e.message}")
                    // Default to back camera as fallback
                    CameraSelector.DEFAULT_BACK_CAMERA
                }

                try {
                    // Unbind use cases before rebinding.
                    cameraProvider.unbindAll()
                    // Bind the use cases to the lifecycle.
                    cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalyzer)
                } catch (exc: Exception) {
                    // Show a more user-friendly message for emulator-specific issues
                    if (isEmulator()) {
                        Log.e(TAG, "Camera binding failed on emulator. This is expected in some emulators.", exc)
                        showEmulatorCameraMessage()
                    } else {
                        Log.e(TAG, "Use case binding failed", exc)
                        Toast.makeText(this, "Could not start camera: ${exc.message}", 
                            Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Camera provider error: ${e.message}", e)
                Toast.makeText(this, "Camera error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun processImageProxy(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val rotationDegrees = imageProxy.imageInfo.rotationDegrees
            val image = InputImage.fromMediaImage(mediaImage, rotationDegrees)

            // Use high-accuracy face detection with classification options
            val options = FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL) // Enable eye detection
                .build()
            val detector = FaceDetection.getClient(options)

            // Process the image using ML Kit.
            detector.process(image)
                .addOnSuccessListener { faces ->
                    if (faces.isEmpty()) {
                        Log.d(TAG, "No faces detected. Potential distraction or improper camera angle.")
                        updateStatus("No face detected")
                        // Increment face away counter
                        consecutiveFaceAwayFrames++
                        consecutiveEyeClosedFrames = 0 // Reset eye closed counter
                        
                        // Check if distraction threshold is met
                        checkForDistraction()
                    } else {
                        Log.d(TAG, "Detected ${faces.size} face(s).")
                        updateStatus("Detected ${faces.size} face(s)")
                        
                        // Reset face away counter since a face is detected
                        consecutiveFaceAwayFrames = 0
                        
                        // Check for drowsiness by analyzing the face
                        val face = faces[0] // Use the first face detected
                        
                        // If eye open probability is available, check for closed eyes
                        if (face.leftEyeOpenProbability != null && face.rightEyeOpenProbability != null) {
                            val leftEyeOpen = face.leftEyeOpenProbability ?: 1.0f
                            val rightEyeOpen = face.rightEyeOpenProbability ?: 1.0f
                            
                            // If both eyes are likely closed
                            if (leftEyeOpen < 0.3 && rightEyeOpen < 0.3) {
                                consecutiveEyeClosedFrames++
                                updateStatus("Eyes appear to be closed")
                                
                                // Check if drowsiness threshold is met
                                checkForDrowsiness()
                            } else {
                                // Eyes are open, reset counter
                                consecutiveEyeClosedFrames = 0
                            }
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Face detection error: ", e)
                    updateStatus("Face detection error: ${e.message}")
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        } else {
            imageProxy.close()
        }
    }

    private fun isEmulator(): Boolean {
        return (Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                || "google_sdk" == Build.PRODUCT)
    }

    private fun showEmulatorCameraMessage() {
        // Create a simple overlay to show instead of camera preview
        val overlay = TextView(this)
        overlay.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        overlay.gravity = Gravity.CENTER
        overlay.setBackgroundColor(Color.BLACK)
        overlay.setTextColor(Color.WHITE)
        overlay.textSize = 18f
        overlay.text = "Camera simulation active.\nOn a real device, the camera would be analyzing your driving behavior."
        
        // Replace the preview view with this message
        val parent = previewView.parent as ViewGroup
        val index = parent.indexOfChild(previewView)
        parent.removeView(previewView)
        parent.addView(overlay, index)
        
        // Make sure the status TextView is still visible
        statusTextView.bringToFront()
        
        // Simulate some face detection events for demo purposes
        startDemoDetectionSimulation()
    }
    
    private fun startDemoDetectionSimulation() {
        // This will simulate face detection events for demonstration purposes in the emulator
        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed(object : Runnable {
            override fun run() {
                // Randomly simulate different detection scenarios
                val random = Random()
                val scenario = random.nextInt(4)
                
                when (scenario) {
                    0 -> {
                        Log.d(TAG, "Simulation: Face detected, normal driving.")
                        updateStatus("Simulation: Face detected, normal driving")
                    }
                    1 -> {
                        Log.d(TAG, "Simulation: Eyes closed detected, potential drowsiness.")
                        updateStatus("Simulation: Eyes closed, potential drowsiness")
                        
                        // Show visual alert for drowsiness
                        if (random.nextBoolean()) {
                            // Randomly choose severity
                            val severity = when(random.nextInt(3)) {
                                0 -> "WARNING"
                                1 -> "SERIOUS"
                                else -> "FATAL"
                            }
                            
                            // Show appropriate alert
                            when (severity) {
                                "WARNING" -> showAlertPanel("CAUTION - DROWSINESS", 
                                    "Your eyes appear to be closing.", Color.YELLOW)
                                "SERIOUS" -> showAlertPanel("WARNING - DROWSINESS", 
                                    "Your eyes have been closed for too long.", Color.parseColor("#FFA500"))
                                else -> showAlertPanel("DANGER - DROWSINESS", 
                                    "Wake up! You appear to be falling asleep.", Color.RED)
                            }
                            
                            // Broadcast drowsiness event
                            sendDetectionBroadcast("DROWSINESS", severity)
                        }
                    }
                    2 -> {
                        Log.d(TAG, "Simulation: Face turned away, distracted driving.")
                        updateStatus("Simulation: Face turned away, distracted driving")
                        
                        // Show visual alert for distraction
                        if (random.nextBoolean()) {
                            // Randomly choose severity
                            val severity = when(random.nextInt(3)) {
                                0 -> "WARNING"
                                1 -> "SERIOUS"
                                else -> "FATAL"
                            }
                            
                            // Show appropriate alert
                            when (severity) {
                                "WARNING" -> showAlertPanel("CAUTION - DISTRACTION", 
                                    "Please keep your eyes on the road.", Color.YELLOW)
                                "SERIOUS" -> showAlertPanel("WARNING - DISTRACTION", 
                                    "You've been looking away for too long.", Color.parseColor("#FFA500"))
                                else -> showAlertPanel("DANGER - DISTRACTION", 
                                    "Eyes on the road! You're not looking at the road.", Color.RED)
                            }
                            
                            // Broadcast distraction event
                            sendDetectionBroadcast("DISTRACTION", severity)
                        }
                    }
                    3 -> {
                        Log.d(TAG, "Simulation: Multiple faces detected, passenger distraction.")
                        updateStatus("Simulation: Multiple faces detected, passenger distraction")
                    }
                }
                
                // Schedule the next simulation
                handler.postDelayed(this, 5000 + random.nextInt(10000).toLong())
            }
        }, 3000)
    }
    
    private fun checkForDrowsiness() {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastAlertTime < ALERT_COOLDOWN_MS) {
            return  // Don't show alerts too frequently
        }
        
        when {
            consecutiveEyeClosedFrames >= FATAL_THRESHOLD -> {
                // Critical drowsiness detected
                showAlertPanel("DANGER - DROWSINESS", "Wake up! You appear to be falling asleep.", Color.RED)
                // Broadcast severe drowsiness event
                sendDetectionBroadcast("DROWSINESS", "FATAL")
                lastAlertTime = currentTime
            }
            consecutiveEyeClosedFrames >= SERIOUS_THRESHOLD -> {
                // Serious drowsiness detected
                showAlertPanel("WARNING - DROWSINESS", "Your eyes have been closed for too long.", Color.parseColor("#FFA500")) // Orange
                // Broadcast serious drowsiness event
                sendDetectionBroadcast("DROWSINESS", "SERIOUS")
                lastAlertTime = currentTime
            }
            consecutiveEyeClosedFrames >= WARNING_THRESHOLD -> {
                // Mild drowsiness detected
                showAlertPanel("CAUTION - DROWSINESS", "Your eyes appear to be closing.", Color.YELLOW)
                // Broadcast mild drowsiness event
                sendDetectionBroadcast("DROWSINESS", "WARNING")
                lastAlertTime = currentTime
            }
        }
    }
    
    private fun checkForDistraction() {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastAlertTime < ALERT_COOLDOWN_MS) {
            return  // Don't show alerts too frequently
        }
        
        when {
            consecutiveFaceAwayFrames >= FATAL_THRESHOLD -> {
                // Critical distraction detected
                showAlertPanel("DANGER - DISTRACTION", "Eyes on the road! You're not looking at the road.", Color.RED)
                // Broadcast severe distraction event
                sendDetectionBroadcast("DISTRACTION", "FATAL")
                lastAlertTime = currentTime
            }
            consecutiveFaceAwayFrames >= SERIOUS_THRESHOLD -> {
                // Serious distraction detected
                showAlertPanel("WARNING - DISTRACTION", "You've been looking away for too long.", Color.parseColor("#FFA500")) // Orange
                // Broadcast serious distraction event
                sendDetectionBroadcast("DISTRACTION", "SERIOUS")
                lastAlertTime = currentTime
            }
            consecutiveFaceAwayFrames >= WARNING_THRESHOLD -> {
                // Mild distraction detected
                showAlertPanel("CAUTION - DISTRACTION", "Please keep your eyes on the road.", Color.YELLOW)
                // Broadcast mild distraction event
                sendDetectionBroadcast("DISTRACTION", "WARNING")
                lastAlertTime = currentTime
            }
        }
    }
    
    private fun showAlertPanel(title: String, message: String, backgroundColor: Int) {
        runOnUiThread {
            // Set alert content
            alertTitleTextView.text = title
            alertMessageTextView.text = message
            
            // Set background color based on severity
            behaviorAlertPanel.setCardBackgroundColor(backgroundColor)
            
            // Show the alert panel
            behaviorAlertPanel.visibility = android.view.View.VISIBLE
        }
    }
    
    private fun hideAlertPanel() {
        runOnUiThread {
            behaviorAlertPanel.visibility = android.view.View.GONE
        }
    }
    
    private fun sendDetectionBroadcast(type: String, severity: String) {
        // Send a broadcast to the DetectionEventReceiver
        val intent = Intent("com.example.drivin_final.DETECTION_EVENT")
        intent.putExtra("type", type)
        intent.putExtra("severity", severity)
        sendBroadcast(intent)
        Log.d(TAG, "Sent detection broadcast: $type with severity $severity")
    }
    
    private fun updateStatus(status: String) {
        runOnUiThread {
            statusTextView.text = "Status: $status"
        }
    }
}


