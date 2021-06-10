package me.fetsh.geekbrains.weather.repository

import me.fetsh.geekbrains.weather.room.HistoryEntity

interface LocalRepository {
    fun getAllHistory(): List<HistoryEntity>
    fun saveEntity(city_name: String, temperature: Int, condition: String)
}