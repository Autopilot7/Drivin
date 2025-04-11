package com.example.openweather.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.openweather.R
import com.example.openweather.model.ForecastDay

class ForecastAdapter(private val onItemClick: (ForecastDay) -> Unit) :
    ListAdapter<ForecastDay, ForecastAdapter.ForecastViewHolder>(ForecastDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ForecastViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_forecast, parent, false)
        return ForecastViewHolder(view)
    }

    override fun onBindViewHolder(holder: ForecastViewHolder, position: Int) {
        val forecastDay = getItem(position)
        holder.bind(forecastDay)
        holder.itemView.setOnClickListener {
            onItemClick(forecastDay)
        }
    }

    class ForecastViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val dayOfWeek: TextView = itemView.findViewById(R.id.dayOfWeek)
        private val weatherIcon: ImageView = itemView.findViewById(R.id.weatherIcon)
        private val condition: TextView = itemView.findViewById(R.id.condition)
        private val temp: TextView = itemView.findViewById(R.id.temp)

        fun bind(forecastDay: ForecastDay) {
            dayOfWeek.text = forecastDay.day
            condition.text = forecastDay.condition
            temp.text = "${forecastDay.temp.toInt()}°C"

            // Set weather icon based on condition
            val iconRes = when (forecastDay.condition.lowercase()) {
                "clear" -> R.drawable.sunny
                "few clouds" -> R.drawable.cloudy
                "scattered clouds", "broken clouds", "overcast" -> R.drawable.cloudy
                "shower rain", "rain" -> R.drawable.rain
                "thunderstorm" -> R.drawable.thunderstorm
                "snow" -> R.drawable.snow
                "mist", "fog" -> R.drawable.mist
                "drizzle" -> R.drawable.cloudy
                else -> R.drawable.sunny // Default icon
            }
            weatherIcon.setImageResource(iconRes)
        }
    }

    class ForecastDiffCallback : DiffUtil.ItemCallback<ForecastDay>() {
        override fun areItemsTheSame(oldItem: ForecastDay, newItem: ForecastDay): Boolean {
            return oldItem.day == newItem.day
        }

        override fun areContentsTheSame(oldItem: ForecastDay, newItem: ForecastDay): Boolean {
            return oldItem == newItem
        }
    }
}
