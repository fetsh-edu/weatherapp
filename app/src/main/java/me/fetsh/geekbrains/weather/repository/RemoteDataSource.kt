package me.fetsh.geekbrains.weather.repository

import com.google.gson.GsonBuilder
import me.fetsh.geekbrains.weather.model.Weather
import retrofit2.Callback
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

private const val API_KEY = "754d7bda-d82f-4100-a5a3-db799ab29a2e"
private const val API_URL = "https://api.weather.yandex.ru/"

class RemoteDataSource {

    private val weatherApi = Retrofit.Builder()
        .baseUrl(API_URL)
        .addConverterFactory(
            GsonConverterFactory.create(
                GsonBuilder().setLenient().create()
            )
        )
        .build().create(WeatherAPI::class.java)

    fun getWeatherDetails(lat: Double, lon: Double, callback: Callback<Weather>) {
        weatherApi.getWeather(API_KEY, lat, lon).enqueue(callback)
    }
}