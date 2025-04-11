// NetworkModule.kt
package com.example.openweather.di

import com.example.openweather.network.DirectionsApiService
import com.example.openweather.network.WeatherService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val WEATHER_BASE_URL = "https://api.openweathermap.org/"
    private const val DIRECTIONS_BASE_URL = "https://maps.googleapis.com/"

    @Provides
    @Singleton
    @Named("WeatherRetrofit")  // Use Named qualifiers
    fun provideWeatherRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(WEATHER_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideWeatherService(@Named("WeatherRetrofit") retrofit: Retrofit): WeatherService {
        return retrofit.create(WeatherService::class.java)
    }

    @Provides
    @Singleton
    @Named("DirectionsRetrofit") // Use Named qualifiers
    fun provideDirectionsRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(DIRECTIONS_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideDirectionsApiService(@Named("DirectionsRetrofit") retrofit: Retrofit): DirectionsApiService {
        return retrofit.create(DirectionsApiService::class.java)
    }
}