package me.fetsh.geekbrains.weather.ui.main

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import me.fetsh.geekbrains.weather.RemoteData
import me.fetsh.geekbrains.weather.model.Repository
import me.fetsh.geekbrains.weather.model.RepositoryImpl
import me.fetsh.geekbrains.weather.model.Weather
import java.lang.Thread.sleep

class MainViewModel(
    private val liveDataToObserve: MutableLiveData<RemoteData<List<Weather>, Throwable>> = MutableLiveData(RemoteData.NotAsked),
    private val repositoryImpl: Repository = RepositoryImpl()
) :
    ViewModel() {

    fun getLiveData() = liveDataToObserve

    fun getWeatherFromLocalSourceRus() = getDataFromLocalSource(isRussian = true)

    fun getWeatherFromLocalSourceWorld() = getDataFromLocalSource(isRussian = false)

    fun getWeatherFromRemoteSource() = getDataFromLocalSource(isRussian = true)

    private fun getDataFromLocalSource(isRussian: Boolean) {
        liveDataToObserve.value = RemoteData.Loading
        Thread {
            sleep(1000)
            liveDataToObserve.postValue(
                RemoteData.Success(
                    if (isRussian) {
                        repositoryImpl.getWeatherFromLocalStorageRus()
                    } else {
                        repositoryImpl.getWeatherFromLocalStorageWorld()
                    }
                )
            )
//            liveDataToObserve.postValue(
//                if (Random.nextBoolean()) {
//                    RemoteData.Success(repositoryImpl.getWeatherFromLocalStorage())
//                } else {
//                    RemoteData.Failure(NetworkErrorException("Network error"))
//                }
//            )
        }.start()
    }
}
