package me.fetsh.geekbrains.weather.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import me.fetsh.geekbrains.weather.App.Companion.getHistoryDao
import me.fetsh.geekbrains.weather.RemoteData
import me.fetsh.geekbrains.weather.repository.LocalRepository
import me.fetsh.geekbrains.weather.repository.LocalRepositoryImpl
import me.fetsh.geekbrains.weather.room.HistoryEntity

class HistoryViewModel(
    private val _historyLiveData: MutableLiveData<RemoteData<List<HistoryEntity>, Throwable>> = MutableLiveData(RemoteData.NotAsked),
    private val historyRepository: LocalRepository = LocalRepositoryImpl(getHistoryDao())
) : ViewModel() {

    val historyLiveData : LiveData<RemoteData<List<HistoryEntity>, Throwable>>
        get() = _historyLiveData

    fun getAllHistory() {
        _historyLiveData.value = RemoteData.Loading
        Thread {
            try {
                _historyLiveData.postValue(RemoteData.Success(historyRepository.getAllHistory()))
            } catch (e: Exception) {
                _historyLiveData.postValue(RemoteData.Failure(e))
            }
        }.start()
    }
}