package com.example.openweather

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.openweather.adapters.ForecastAdapter
import com.example.openweather.databinding.ActivityWeatherBinding
import com.example.openweather.model.ForecastDay
import com.example.openweather.network.WeatherService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class WeatherActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWeatherBinding
    private lateinit var forecastAdapter: ForecastAdapter
    private val apiKey = "88625e3714eaa54d2f537b42f1429987"

    @Inject
    lateinit var weatherService: WeatherService

    // Get the current day of the week
    private val currentDayOfWeek = SimpleDateFormat("EEEE", Locale.getDefault()).format(Date())

    // Generate the next 7 days (including today)
    private val forecastList = generateNext7DaysForecast()

    private val weatherTips = mapOf(
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWeatherBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        getWeatherToday()

        // Set up SwipeRefreshLayout
        binding.swipeRefreshLayout.setOnRefreshListener {
            getWeatherToday()
            binding.swipeRefreshLayout.isRefreshing = false
        }
    }

    private fun setupRecyclerView() {
        forecastAdapter = ForecastAdapter { forecastDay ->
            // When a forecast day is clicked, show the tip dialog
            showWeatherTipDialog(forecastDay)
        }
        binding.recyclerViewForecast.apply {
            layoutManager = LinearLayoutManager(this@WeatherActivity, LinearLayoutManager.VERTICAL, false)
            adapter = forecastAdapter
        }
        forecastAdapter.submitList(forecastList)
    }

    private fun getWeatherToday() {
        binding.progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                val weather = withContext(Dispatchers.IO) {
                    weatherService.getCurrentWeather("Hanoi", apiKey)
                }

                binding.progressBar.visibility = View.GONE
                val temp = weather.main.temp
                val desc = weather.weather.firstOrNull()?.description?.replaceFirstChar { it.uppercase() }

                // Set current weather
                binding.textViewCurrentWeather.text = "Temp: $temp°C, $desc"

                // Set weather icon dynamically based on condition
                val iconRes = getWeatherIconResource(desc)
                binding.currentWeatherIcon.setImageResource(iconRes)

                // Set weather tip based on current condition
                val tip = weatherTips[desc] ?: "No specific driving tips for this weather condition."
                binding.textViewTips.text = tip

                // Set a weather alert (for this example, it's hardcoded)
                binding.textViewWeatherAlert.text = "Weather Alert: Thunderstorm expected later today."

            } catch (e: Exception) {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this@WeatherActivity, "Failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showWeatherTipDialog(forecastDay: ForecastDay) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.layout_weather_tip_dialog)

        // Set dialog views
        val titleView = dialog.findViewById<TextView>(R.id.dialogTitle)
        val dayView = dialog.findViewById<TextView>(R.id.dialogDay)
        val conditionView = dialog.findViewById<TextView>(R.id.dialogCondition)
        val tipView = dialog.findViewById<TextView>(R.id.dialogTip)
        val iconView = dialog.findViewById<ImageView>(R.id.dialogWeatherIcon)
        val closeButton = dialog.findViewById<Button>(R.id.dialogCloseButton)

        // Set dialog content
        titleView.text = "Driving Tips for ${forecastDay.day}"
        dayView.text = forecastDay.day
        conditionView.text = "${forecastDay.condition}, ${forecastDay.temp}°C"
        tipView.text = weatherTips[forecastDay.condition] ?: "No specific driving tips for this weather condition."
        iconView.setImageResource(getWeatherIconResource(forecastDay.condition))

        // Set close button action
        closeButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun getWeatherIconResource(condition: String?): Int {
        return when (condition?.lowercase()) {
            "clear", "clear sky" -> R.drawable.sunny
            "few clouds" -> R.drawable.cloudy
            "scattered clouds", "broken clouds", "overcast", "overcast clouds" -> R.drawable.cloudy
            "shower rain", "rain", "moderate rain", "light rain" -> R.drawable.rain
            "thunderstorm" -> R.drawable.thunderstorm
            "snow" -> R.drawable.snow
            "mist", "fog" -> R.drawable.mist
            "drizzle" -> R.drawable.rain
            else -> R.drawable.sunny // Default icon
        }
    }

    private fun generateNext7DaysForecast(): List<ForecastDay> {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("EEE", Locale.getDefault())
        val forecast = mutableListOf<ForecastDay>()

        // Weather conditions for the next 7 days (including today)
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
