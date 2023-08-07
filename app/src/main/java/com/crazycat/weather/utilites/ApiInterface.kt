package com.crazycat.weather.utilites

import com.crazycat.weather.model.WeatherData
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiInterface {

    @GET("weather")
    fun getCurrentWeatherData(
        @Query("lat") lat: String,
        @Query("lon") lon: String,
        @Query("APPID") appid: String
    ): Call<WeatherData>

    @GET("weather")
    fun getCityWeatherData(
        @Query("q") q: String,
        @Query("APPID") appid: String
    ): Call<WeatherData>
}