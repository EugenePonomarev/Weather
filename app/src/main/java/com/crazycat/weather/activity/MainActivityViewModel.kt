package com.crazycat.weather.activity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crazycat.weather.R
import com.crazycat.weather.activity.domain.ActivityRepository
import com.crazycat.weather.presentation.BaseState
import com.crazycat.weather.core.ResourceHolder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainActivityViewModel(
    private val activityRepository: ActivityRepository,
    private val resourceHolder: ResourceHolder
) : ViewModel() {

    private var _state = MutableStateFlow<BaseState>(BaseState.Working)
    val state = _state.asStateFlow()

    private fun updateState(onUpdate: (BaseState) -> BaseState) {
        _state.update { oldState -> onUpdate(oldState) }
    }

    fun getCurrentLocationWeather(latitude: String, longitude: String) {
        viewModelScope.launch {
            activityRepository.getCurrentLocationWeather(latitude, longitude)
                .onStart { updateState { BaseState.Loading } }
                .onCompletion { updateState { BaseState.Working } }
                .catch { updateState { BaseState.Error(resourceHolder.getString(R.string.not_get_weather)) } }
                .collect { data ->
                    updateState { BaseState.Success(data) }
                }
        }
    }

    fun getCityWeather(city: String) {
        viewModelScope.launch {
            activityRepository.getCityWeather(city)
                .onStart { updateState { BaseState.Loading } }
                .onCompletion { updateState { BaseState.Working } }
                .catch { updateState { BaseState.Error(resourceHolder.getString(R.string.not_get_weather)) } }
                .collect { data ->
                    updateState { BaseState.Success(data) }
                }
        }
    }
}