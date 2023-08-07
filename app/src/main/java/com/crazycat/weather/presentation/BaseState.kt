package com.crazycat.weather.presentation

import android.os.Parcelable
import com.crazycat.weather.model.WeatherData
import kotlinx.parcelize.Parcelize

@Parcelize
sealed class BaseState : Parcelable {
    object Working : BaseState()
    object Loading : BaseState()
    class Success(val weatherData: WeatherData) : BaseState()
    class Error(val message: String) : BaseState()
}
