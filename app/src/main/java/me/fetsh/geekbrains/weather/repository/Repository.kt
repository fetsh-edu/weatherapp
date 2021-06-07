package me.fetsh.geekbrains.weather.repository

import me.fetsh.geekbrains.weather.model.City
import me.fetsh.geekbrains.weather.model.Weather
import retrofit2.Callback

interface Repository {
    fun getWeatherFromServer(lat: Double, lon: Double, callback: Callback<Weather>)
    fun getCitiesFromLocalStorageRus(): List<City>
    fun getCitiesFromLocalStorageWorld(): List<City>
}