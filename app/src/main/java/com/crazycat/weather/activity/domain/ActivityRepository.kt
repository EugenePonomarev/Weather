package com.crazycat.weather.activity.domain

import com.crazycat.weather.model.WeatherData
import kotlinx.coroutines.flow.Flow

interface ActivityRepository {

    suspend fun getCurrentLocationWeather(latitude: String, longitude: String) : Flow<WeatherData>

    suspend fun getCityWeather(city: String) : Flow<WeatherData>
}