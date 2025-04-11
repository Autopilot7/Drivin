package com.example.openweather.ui.weather

import android.annotation.SuppressLint
import android.app.Dialog
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.openweather.R
import com.example.openweather.adapters.ForecastAdapter
import com.example.openweather.databinding.ActivityWeatherBinding
import com.example.openweather.model.ForecastDay
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

@Composable
fun WeatherScreen(
    viewModel: WeatherViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Create the binding
    val binding = remember {
        ActivityWeatherBinding.inflate(LayoutInflater.from(context))
    }

    // Setup the XML layout in Compose
    AndroidView(
        factory = { binding.root },
        modifier = Modifier.fillMaxSize(),
        update = { view ->
            // This block is called when the view needs to be updated
            val state = viewModel.state

            // Update UI when data changes
            if (!state.isLoading) {
                binding.progressBar.visibility = android.view.View.GONE

                state.currentTemp?.let { temp ->
                    state.description?.let { desc ->
                        binding.textViewCurrentWeather.text = "Temp: ${temp}°C, $desc"

                        // Set weather icon
                        val iconRes = getWeatherIconResource(desc)
                        binding.currentWeatherIcon.setImageResource(iconRes)

                        // Set weather tip
                        binding.textViewTips.text = viewModel.getTipForCondition(desc)
                    }
                }

                // Set weather alert (hardcoded)
                binding.textViewWeatherAlert.text = "Weather Alert: Thunderstorm expected later today."

                // Update forecast list
                val forecastAdapter = binding.recyclerViewForecast.adapter as? ForecastAdapter
                forecastAdapter?.submitList(state.forecastList)
            } else {
                binding.progressBar.visibility = android.view.View.VISIBLE
            }
        }
    )

    // Initial setup of RecyclerView and SwipeRefreshLayout
    LaunchedEffect(Unit) {
        // Setup RecyclerView
        val recyclerView = binding.recyclerViewForecast
        val forecastAdapter = ForecastAdapter { forecastDay ->
            showWeatherTipDialog(context, forecastDay, viewModel.getTipForCondition(forecastDay.condition))
        }

        recyclerView.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            adapter = forecastAdapter
        }

        // Setup SwipeRefreshLayout
        val swipeRefreshLayout = binding.swipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener {
            viewModel.loadWeather()
            swipeRefreshLayout.isRefreshing = false
        }

        // Load weather data
        viewModel.loadWeather()
    }

    // Handle lifecycle events
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.loadWeather()
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
}

@SuppressLint("SetTextI18n")
private fun showWeatherTipDialog(context: android.content.Context, forecastDay: ForecastDay, tip: String) {
    val dialog = Dialog(context)
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
    tipView.text = tip
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
