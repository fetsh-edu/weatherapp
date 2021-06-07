package me.fetsh.geekbrains.weather.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import me.fetsh.geekbrains.weather.RemoteData
import me.fetsh.geekbrains.weather.repository.RemoteDataSource
import me.fetsh.geekbrains.weather.repository.Repository
import me.fetsh.geekbrains.weather.repository.RepositoryImpl
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Thread.sleep

class WeatherViewModel(
    private val _remoteCities: MutableLiveData<RemoteData<List<City>, Throwable>> = MutableLiveData(RemoteData.NotAsked),
    private val _remoteWeather: MutableLiveData<RemoteData<Weather, Throwable>> = MutableLiveData(RemoteData.NotAsked),
    private val repositoryImpl: Repository = RepositoryImpl(RemoteDataSource())
) :
    ViewModel() {

    val remoteCities: LiveData<RemoteData<List<City>, Throwable>>
        get() = _remoteCities

    val remoteWeather: LiveData<RemoteData<Weather, Throwable>>
        get() = _remoteWeather

    fun getWeatherFromRemoteSource(lat: Double, lon: Double) {
        _remoteWeather.value = RemoteData.Loading
        repositoryImpl.getWeatherFromServer(lat, lon, callBack)
    }

    private val callBack = object :
        Callback<Weather> {

        override fun onResponse(call: Call<Weather>, response: Response<Weather>) {
            val serverResponse: Weather? = response.body()
            _remoteWeather.postValue(
                if (response.isSuccessful && serverResponse != null) {
                    checkResponse(serverResponse)
                } else {
                    RemoteData.Failure(Throwable("Server error"))
                }
            )
        }

        override fun onFailure(call: Call<Weather>, t: Throwable) {
            _remoteWeather.postValue(RemoteData.Failure(Throwable(t.message ?: "Server request error")))
        }

        private fun checkResponse(serverResponse: Weather): RemoteData<Weather, Throwable> {
            val fact = serverResponse.fact
            return if (fact == null || fact.temp == null || fact.feels_like == null || fact.condition.isNullOrEmpty()) {
                RemoteData.Failure(Throwable("Corrupted data"))
            } else {
                RemoteData.Success(serverResponse)
            }
        }
    }

    fun getCitiesFromLocalSourceRus() = getCitiesFromLocalSource(isRussian = true)
    fun getCitiesFromLocalSourceWorld() = getCitiesFromLocalSource(isRussian = false)
    private fun getCitiesFromLocalSource(isRussian: Boolean) {
        _remoteCities.value = RemoteData.Loading
        Thread {
            sleep(100)
            _remoteCities.postValue(
                RemoteData.Success(
                    if (isRussian) {
                        repositoryImpl.getCitiesFromLocalStorageRus()
                    } else {
                        repositoryImpl.getCitiesFromLocalStorageWorld()
                    }
                )
            )
        }.start()
    }
}
