package com.example.openweather.ui.weather

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.openweather.model.ForecastDay
import com.example.openweather.network.WeatherService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

data class WeatherUiState(
    val isLoading: Boolean = true,
    val currentTemp: Float? = null,
    val description: String? = null,
    val tip: String? = null,
    val forecastList: List<ForecastDay> = emptyList()
)

@HiltViewModel
class WeatherViewModel @Inject constructor(
    val weatherService: WeatherService
) : ViewModel() {

    var state by mutableStateOf(WeatherUiState())
        private set

    private val apiKey = "88625e3714eaa54d2f537b42f1429987"

    private val tips = mapOf(
        "Clear" to "Clear Sky: Wear sunglasses and keep the AC ready. Roads may be hot and slippery from dust.",
        "Few Clouds" to "Few Clouds: Good driving weather. Keep shades handy and be aware of glare.",
        "Scattered Clouds" to "Scattered Clouds: Good driving conditions. Keep an eye on changing weather patterns.",
        "Broken Clouds" to "Broken Clouds: Decent driving conditions. Be prepared for sudden changes in light.",
        "Overcast" to "Overcast: Turn on headlights. Watch for sudden rain or low visibility.",
        "Drizzle" to "Drizzle: Roads are slick! Drive slow and keep your distance.",
        "Rain" to "Rain: Use headlights. Avoid sudden braking and deep puddles.",
        "Shower Rain" to "Shower Rain: Keep your wipers sharp. Expect fast-changing visibility.",
        "Thunderstorm" to "Thunderstorm: Stay inside if possible. Avoid flooded roads and drive with extreme caution.",
        "Snow" to "Snow: Drive slowly, increase following distance, and avoid sudden movements.",
        "Mist" to "Mist: Use fog lights if available. Drive slow and increase following distance.",
        "Fog" to "Fog: Use fog lights, reduce speed, and increase following distance significantly."
    )

    fun loadWeather() {
        viewModelScope.launch {
            state = state.copy(isLoading = true)

            try {
                val weather = weatherService.getCurrentWeather("Hanoi", apiKey)
                val desc = weather.weather.firstOrNull()?.description?.replaceFirstChar { it.uppercase() } ?: "Clear"
                val forecast = generateNext7DaysForecast()

                state = state.copy(
                    isLoading = false,
                    currentTemp = weather.main.temp,
                    description = desc,
                    tip = getTipForCondition(desc),
                    forecastList = forecast
                )
            } catch (e: Exception) {
                state = state.copy(isLoading = false)
            }
        }
    }


    fun getTipForCondition(condition: String?): String {
        return tips[condition ?: ""] ?: "Use low beams instead of high beams. Keep a safe distance from the vehicle ahead. Ensure wipers and defoggers are on. Avoid sudden braking or sharp turns."
    }

    private fun generateNext7DaysForecast(): List<ForecastDay> {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("EEE", Locale.getDefault())
        val forecast = mutableListOf<ForecastDay>()

        val conditions = listOf("Clear", "Rain", "Overcast", "Thunderstorm", "Drizzle", "Few Clouds", "Scattered Clouds")
        val temperatures = listOf(28.0, 24.0, 26.0, 22.0, 25.0, 27.0, 29.0)

        for (i in 0 until 7) {
            val dayName = dateFormat.format(calendar.time)
            forecast.add(ForecastDay(dayName, temperatures[i], conditions[i]))
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        return forecast
    }
}
