package me.fetsh.geekbrains.weather.room

import androidx.room.*

@Dao
interface HistoryDao {

    @Query("SELECT * FROM HistoryEntity ORDER BY id DESC")
    fun all(): List<HistoryEntity>

    @Query("SELECT * FROM HistoryEntity WHERE city LIKE :city")
    fun getDataByWord(city: String): List<HistoryEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(entity: HistoryEntity)

    @Update
    fun update(entity: HistoryEntity)

    @Delete
    fun delete(entity: HistoryEntity)
}