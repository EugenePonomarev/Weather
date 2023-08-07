package com.crazycat.weather.activity.domain

import com.crazycat.weather.model.WeatherData
import com.crazycat.weather.ui.Constants.API_KEY
import com.crazycat.weather.utilites.ApiUtilities
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class ActivityRepositoryImpl(private val apiUtilities: ApiUtilities) : ActivityRepository {

    override suspend fun getCurrentLocationWeather(latitude: String, longitude: String): Flow<WeatherData> = flow {

        emit(suspendCoroutine { continuation ->
            apiUtilities.getApiInterface()?.getCurrentWeatherData(latitude, longitude, API_KEY)
                ?.execute()?.run {
                if (isSuccessful)
                    body()?.let { continuation.resume(it) }
                else
                    throw Throwable("${this.code()}: ${this.message()}")
            }
        })

    }.flowOn(Dispatchers.IO)

    override suspend fun getCityWeather(city: String): Flow<WeatherData> = flow {

        emit(suspendCoroutine { continuation ->
            apiUtilities.getApiInterface()?.getCityWeatherData(city, API_KEY)?.execute()?.run {
                if (isSuccessful) {
                    body()?.let { continuation.resume(it) }
                } else
                    throw Throwable("${this.code()}: ${this.message()}")
            }
        })

    }.flowOn(Dispatchers.IO)
}
