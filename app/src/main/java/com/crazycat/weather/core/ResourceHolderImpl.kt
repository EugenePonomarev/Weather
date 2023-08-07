package com.crazycat.weather.core

import android.annotation.SuppressLint
import android.content.Context

class ResourceHolderImpl(private val context: Context) : ResourceHolder {

    override fun getString(stringRes: Int): String {
        return context.getString(stringRes)
    }

    override fun getString(stringRes: Int, vararg arg: Any): String {
        return context.getString(stringRes, *arg)
    }

    @SuppressLint("ResourceType")
    override fun getStringArray(stringRes: Int): Array<out String> {
        return context.resources.getStringArray(stringRes)
    }
}