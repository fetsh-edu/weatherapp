package me.fetsh.geekbrains.weather.model

import java.lang.Exception

enum class CitiesSet {
    RUS, WORLD;

    companion object {
        fun fromString(string: String) : CitiesSet {
            return try {
                valueOf(string)
            } catch (ex: Exception) {
                RUS
            }
        }
    }
}
