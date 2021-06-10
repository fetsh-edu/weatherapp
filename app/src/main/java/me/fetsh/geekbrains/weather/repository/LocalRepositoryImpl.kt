package me.fetsh.geekbrains.weather.repository

import me.fetsh.geekbrains.weather.model.City
import me.fetsh.geekbrains.weather.model.Weather
import me.fetsh.geekbrains.weather.room.HistoryDao
import me.fetsh.geekbrains.weather.room.HistoryEntity

class LocalRepositoryImpl(private val localDataSource: HistoryDao) :
    LocalRepository {

    override fun getAllHistory(): List<HistoryEntity> {
        return localDataSource.all()
    }

    override fun saveEntity(
        city_name: String,
        temperature: Int,
        condition: String
    ) {
        localDataSource.insert(HistoryEntity(
            0,
            city_name,
            temperature,
            condition
        ))
    }
}

