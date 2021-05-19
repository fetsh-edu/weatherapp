package me.fetsh.geekbrains.weather.ui.main

import android.accounts.NetworkErrorException
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import me.fetsh.geekbrains.weather.RemoteData
import me.fetsh.geekbrains.weather.model.Repository
import me.fetsh.geekbrains.weather.model.RepositoryImpl
import me.fetsh.geekbrains.weather.model.Weather
import java.lang.Thread.sleep
import kotlin.random.Random

class MainViewModel(
    private val liveDataToObserve: MutableLiveData<RemoteData<Weather, Throwable>> = MutableLiveData(RemoteData.NotAsked),
    private val repositoryImpl: Repository = RepositoryImpl()
) :
    ViewModel() {

    fun getLiveData() = liveDataToObserve

    fun getWeather() = getDataFromLocalSource()

    private fun getDataFromLocalSource() {
        Thread {
            sleep(1000)
            liveDataToObserve.postValue(
                if (Random.nextBoolean()) {
                    RemoteData.Success(repositoryImpl.getWeatherFromLocalStorage())
                } else {
                    RemoteData.Failure(NetworkErrorException("Network error"))
                }
            )
        }.start()
    }
}
