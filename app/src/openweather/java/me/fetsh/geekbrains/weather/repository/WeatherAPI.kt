package me.fetsh.geekbrains.weather.repository

import me.fetsh.geekbrains.weather.model.Weather
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherAPI {
    @GET("data/2.5/weather")
    fun getWeather(
        @Query("appid") token: String,
        @Query("lat") lat: Double,
        @Query("lon") lon: Double
    ): Call<Weather>
}