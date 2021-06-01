package me.fetsh.geekbrains.weather.model

import me.fetsh.geekbrains.weather.RemoteData

interface Repository {
    fun getWeatherFromServer(lat: Double, lon: Double): RemoteData<Weather, Throwable>
    fun getCitiesFromLocalStorageRus(): List<City>
    fun getCitiesFromLocalStorageWorld(): List<City>
    fun loadWeather(lat: Double, lon: Double, listener: (RemoteData<Weather, Throwable>) -> Unit)
}