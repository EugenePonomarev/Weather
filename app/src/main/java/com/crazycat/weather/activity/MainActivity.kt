package com.crazycat.weather.activity

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.widget.SearchView.OnQueryTextListener
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.crazycat.weather.R
import com.crazycat.weather.activity.domain.ActivityRepositoryImpl
import com.crazycat.weather.core.ResourceHolderImpl
import com.crazycat.weather.databinding.ActivityMainBinding
import com.crazycat.weather.model.WeatherData
import com.crazycat.weather.presentation.BaseState
import com.crazycat.weather.ui.Constants.DATE_PATTERN
import com.crazycat.weather.ui.Constants.PERMISSION_REQUEST_CODE
import com.crazycat.weather.ui.Constants.TIME_PATTERN
import com.crazycat.weather.ui.delegate.viewModelCreator
import com.crazycat.weather.ui.utils.gone
import com.crazycat.weather.ui.utils.show
import com.crazycat.weather.utilites.ApiUtilities
import com.crazycat.weather.utilites.TrackingUtility
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity(), EasyPermissions.PermissionCallbacks {

    private lateinit var binding: ActivityMainBinding

    private val viewModel by viewModelCreator {
        MainActivityViewModel(ActivityRepositoryImpl(ApiUtilities), ResourceHolderImpl(this))
    }

    private lateinit var currentLocation: Location
    private lateinit var fusedLocationProvider: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fusedLocationProvider = LocationServices.getFusedLocationProviderClient(this)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initListeners()
        checkCurrentLocation()
        stateSubscribe()
    }

    private fun initListeners() {
        with(binding) {
            currentLocation.setOnClickListener { checkCurrentLocation() }

            searchView.setOnQueryTextListener(object : OnQueryTextListener,
                SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(p0: String?): Boolean {
                    if (p0 != null) viewModel.getCityWeather(p0)
                    return true
                }

                override fun onQueryTextChange(p0: String?): Boolean {
                    return true
                }
            })
        }
    }

    private fun stateSubscribe() {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collectLatest { state ->
                    when (state) {
                        is BaseState.Error -> showError(state.message)
                        is BaseState.Loading -> showLoading()
                        is BaseState.Working -> showWorking()
                        is BaseState.Success -> successResult(state.weatherData)
                    }
                }
            }
        }
    }

    private fun initViews(weatherData: WeatherData) {
        val currentDate = SimpleDateFormat(DATE_PATTERN, Locale.getDefault()).format(Date())
        with(binding) {
            dateTime.text = currentDate.toString()
            maxTemp.text = getString(R.string.max, convertKelvinToCelsius(weatherData.main.temp_max))
            minTemp.text = getString(R.string.min, convertKelvinToCelsius(weatherData.main.temp_min))
            temp.text = getString(R.string.d, convertKelvinToCelsiusNumber(weatherData.main.temp))
            feelsLike.text = getString(R.string.d, convertKelvinToCelsius(weatherData.main.feels_like))
            weatherTitle.text = weatherData.weather[0].main
            sunriseValue.text = sunTime(weatherData.sys.sunrise.toLong())
            sunsetValue.text = sunTime(weatherData.sys.sunset.toLong())
            pressureValue.text = weatherData.main.pressure.toString()
            humidityValue.text = getString(R.string.percent, weatherData.main.humidity.toString())
            tempFValue.text = getString(R.string.d, ((convertKelvinToCelsiusDouble(weatherData.main.temp) * 1.8) + 32).toInt().toString())
            windValue.text = getString(R.string.m_s, weatherData.wind.speed.toString())
            groundValue.text = weatherData.main.grnd_level.toString()
            seaValue.text = weatherData.main.sea_level.toString()
            countryValue.text = weatherData.sys.country
            cityName.text = weatherData.name
        }
        setBackgroundAndIcons(weatherData.weather[0].id)
    }

    private fun setBackgroundAndIcons(id: Int) {
        with(binding) {
            when (id) {
                //Thunderstorm
                in 200..232 -> {
                    weatherImg.setImageResource(R.drawable.ic_storm_weather)
                    root.background = ContextCompat
                        .getDrawable(this@MainActivity, R.drawable.thunderstrom_bg)
                    optionsLayout.background = ContextCompat
                        .getDrawable(this@MainActivity, R.drawable.thunderstrom_bg)
                }
                //Drizzle
                in 300..321 -> {
                    weatherImg.setImageResource(R.drawable.ic_few_clouds)
                    root.background = ContextCompat
                        .getDrawable(this@MainActivity, R.drawable.drizzle_bg)
                    optionsLayout.background = ContextCompat
                        .getDrawable(this@MainActivity, R.drawable.drizzle_bg)
                }
                //Rain
                in 500..531 -> {
                    weatherImg.setImageResource(R.drawable.ic_rainy_weather)
                    root.background = ContextCompat
                        .getDrawable(this@MainActivity, R.drawable.rain_bg)
                    optionsLayout.background = ContextCompat
                        .getDrawable(this@MainActivity, R.drawable.rain_bg)
                }
                //Snow
                in 600..622 -> {
                    weatherImg.setImageResource(R.drawable.ic_snow_weather)
                    root.background = ContextCompat
                        .getDrawable(this@MainActivity, R.drawable.snow_bg)
                    optionsLayout.background = ContextCompat
                        .getDrawable(this@MainActivity, R.drawable.snow_bg)
                }
                //Atmosphere
                in 701..781 -> {
                    weatherImg.setImageResource(R.drawable.ic_broken_clouds)
                    root.background = ContextCompat
                        .getDrawable(this@MainActivity, R.drawable.atmosphere_bg)
                    optionsLayout.background = ContextCompat
                        .getDrawable(this@MainActivity, R.drawable.atmosphere_bg)
                }
                //Clear
                800 -> {
                    weatherImg.setImageResource(R.drawable.ic_clear_day)
                    root.background = ContextCompat
                        .getDrawable(this@MainActivity, R.drawable.clear_bg)
                    optionsLayout.background = ContextCompat
                        .getDrawable(this@MainActivity, R.drawable.clear_bg)
                }

                //Clouds
                in 801..804 -> {
                    weatherImg.setImageResource(R.drawable.ic_cloudy_weather)
                    root.background = ContextCompat
                        .getDrawable(this@MainActivity, R.drawable.clouds_bg)
                    optionsLayout.background = ContextCompat
                        .getDrawable(this@MainActivity, R.drawable.clouds_bg)
                }

                //unknown
                else -> {
                    weatherImg.setImageResource(R.drawable.ic_unknown)
                    root.background = ContextCompat
                        .getDrawable(this@MainActivity, R.drawable.unknown_bg)
                    optionsLayout.background = ContextCompat
                        .getDrawable(this@MainActivity, R.drawable.unknown_bg)
                }
            }
        }
    }

    private fun showWorking() {
        binding.progressBar.gone()
    }

    private fun showLoading() {
        binding.progressBar.show()
    }

    private fun showError(message: String) {
        binding.progressBar.gone()
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun successResult(weatherData: WeatherData) {
        binding.progressBar.gone()
        initViews(weatherData)
    }

    private fun convertKelvinToCelsius(temp: Double): String {
        var intTemp = temp
        intTemp = intTemp.minus(273)
        return intTemp.toBigDecimal().setScale(1, RoundingMode.UP).toDouble().toString()
    }

    private fun convertKelvinToCelsiusDouble(temp: Double): Double {
        var intTemp = temp
        intTemp = intTemp.minus(273)
        return intTemp.toBigDecimal().setScale(1, RoundingMode.UP).toDouble()
    }

    private fun convertKelvinToCelsiusNumber(temp: Double): String {
        var intTemp = temp
        intTemp = intTemp.minus(273)
        return intTemp.toBigDecimal().setScale(1, RoundingMode.UP).toDouble().toInt().toString()
    }

    private fun sunTime(ts: Long): String {
        val unix: Long = ts * 1000
        val dateFormat = SimpleDateFormat(TIME_PATTERN, Locale.getDefault())
        val dt = Date(unix)
        return dateFormat.format(dt)
    }

    private fun checkCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions()
            return
        }
        fusedLocationProvider.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                currentLocation = location
                viewModel.getCurrentLocationWeather(
                    location.latitude.toString(),
                    location.longitude.toString()
                )
            }
        }
    }

    private fun requestPermissions() {
        if (TrackingUtility.hasLocationPermissions(this)) {
            checkCurrentLocation()
            return
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            EasyPermissions.requestPermissions(
                this,
                getString(R.string.you_need_accept_lock_permissions),
                PERMISSION_REQUEST_CODE,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        } else {
            EasyPermissions.requestPermissions(
                this,
                getString(R.string.you_need_accept_lock_permissions),
                PERMISSION_REQUEST_CODE,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
        }
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            AppSettingsDialog.Builder(this).build().show()
        } else {
            requestPermissions()
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {}

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }
}
