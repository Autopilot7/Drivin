package com.example.drivin_final.util

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import android.util.Log
import com.example.drivin_final.R

/**
 * Utility class for generating and playing alert sounds
 */
class SoundGenerator(private val context: Context) {
    private val TAG = "SoundGenerator"
    private var soundPool: SoundPool? = null
    
    // Sound IDs
    private var infoSoundId: Int = -1
    private var warningSoundId: Int = -1
    private var dangerSoundId: Int = -1
    
    init {
        initSoundPool()
        loadSounds()
    }
    
    private fun initSoundPool() {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ALARM)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
            
        soundPool = SoundPool.Builder()
            .setMaxStreams(3)
            .setAudioAttributes(audioAttributes)
            .build()
    }
    
    private fun loadSounds() {
        try {
            // Load sounds from resources
            infoSoundId = soundPool?.load(context, R.raw.info_sound, 1) ?: -1
            warningSoundId = soundPool?.load(context, R.raw.warning_sound, 1) ?: -1
            dangerSoundId = soundPool?.load(context, R.raw.danger_sound, 1) ?: -1
            
            Log.d(TAG, "Sounds loaded: info=$infoSoundId, warning=$warningSoundId, danger=$dangerSoundId")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load sounds", e)
        }
    }
    
    /**
     * Play mild info alert sound
     */
    fun playInfoAlert() {
        if (infoSoundId != -1) {
            playSoundWithPriority(infoSoundId, 0.7f)
        }
    }
    
    /**
     * Play medium warning alert sound
     */
    fun playWarningAlert() {
        if (warningSoundId != -1) {
            playSoundWithPriority(warningSoundId, 0.8f)
        }
    }
    
    /**
     * Play high priority danger alert sound
     */
    fun playDangerAlert() {
        if (dangerSoundId != -1) {
            playSoundWithPriority(dangerSoundId, 1.0f)
        }
    }
    
    private fun playSoundWithPriority(soundId: Int, volume: Float) {
        try {
            // Get current volume from audio manager to scale our sound accordingly
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
            val currentVolume = audioManager?.getStreamVolume(AudioManager.STREAM_ALARM)?.toFloat() ?: 10f
            val maxVolume = audioManager?.getStreamMaxVolume(AudioManager.STREAM_ALARM)?.toFloat() ?: 15f
            
            // Scale to 0.0-1.0 range
            val normalizedVolume = if (maxVolume > 0) currentVolume / maxVolume else 0.7f
            val finalVolume = normalizedVolume * volume
            
            // Play sound with chosen volume
            soundPool?.play(soundId, finalVolume, finalVolume, 1, 0, 1.0f)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to play sound $soundId", e)
        }
    }
    
    /**
     * Release resources when no longer needed
     */
    fun release() {
        soundPool?.release()
        soundPool = null
    }
} 