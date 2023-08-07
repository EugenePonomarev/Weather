package com.crazycat.weather.core

import androidx.annotation.StringRes

interface ResourceHolder {
    fun getString(@StringRes stringRes: Int): String

    fun getString(@StringRes stringRes: Int, vararg arg: Any): String

    fun getStringArray(@StringRes stringRes: Int): Array<out String>
}