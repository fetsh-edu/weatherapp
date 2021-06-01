package me.fetsh.geekbrains.weather.model

data class Weather(
    val fact: Fact?
)
data class Fact(
    val temp: Int?,
    val feels_like: Int?,
    val condition: String?
)