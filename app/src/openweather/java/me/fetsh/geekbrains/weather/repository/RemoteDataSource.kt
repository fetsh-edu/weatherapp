package me.fetsh.geekbrains.weather.repository

import com.google.gson.GsonBuilder
import me.fetsh.geekbrains.weather.model.Fact
import me.fetsh.geekbrains.weather.model.Weather
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private const val API_URL = "https://api.openweathermap.org/"

class RemoteDataSource {

    fun getWeatherDetails(lat: Double, lon: Double, callback: Callback<Weather>) {
        val fakeWeather = Weather(Fact(33, 43, "cloudy", "ovc"))
        val fakeCall = null as? Call<Weather>
        callback.onResponse(fakeCall, Response.success(fakeWeather))
    }
}