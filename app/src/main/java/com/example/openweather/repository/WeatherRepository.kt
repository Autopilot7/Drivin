// WeatherRepository.kt
package com.example.openweather.repositories

import com.example.openweather.model.WeatherResponse
import com.example.openweather.network.WeatherService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WeatherRepository @Inject constructor(
    private val apiService: WeatherService
) {
    private val apiKey = "88625e3714eaa54d2f537b42f1429987"

    suspend fun getWeatherData(city: String): WeatherResponse {
        return withContext(Dispatchers.IO) {
            apiService.getCurrentWeather(city, apiKey)
        }
    }
}
