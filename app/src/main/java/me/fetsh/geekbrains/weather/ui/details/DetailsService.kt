package me.fetsh.geekbrains.weather.ui.details

import android.app.IntentService
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.gson.Gson
import me.fetsh.geekbrains.weather.model.Weather
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.IllegalStateException
import java.net.MalformedURLException
import java.net.URL
import java.util.stream.Collectors
import javax.net.ssl.HttpsURLConnection
import kotlin.random.Random

const val DETAILS_INTENT_EMPTY_EXTRA = "INTENT IS EMPTY"
const val DETAILS_DATA_EMPTY_EXTRA = "DATA IS EMPTY"
const val DETAILS_RESPONSE_EMPTY_EXTRA = "RESPONSE IS EMPTY"
const val DETAILS_URL_MALFORMED_EXTRA = "URL MALFORMED"


const val LATITUDE_EXTRA = "Latitude"
const val LONGITUDE_EXTRA = "Longitude"
private const val API_KEY = "754d7bda-d82f-4100-a5a3-db799ab29a2e"
private const val API_URL = "https://api.weather.yandex.ru/v2/forecast"
private const val REQUEST_GET = "GET"
private const val REQUEST_TIMEOUT = 10000
private const val REQUEST_API_KEY = "X-Yandex-API-Key"

class DetailsService(name: String = "DetailService") : IntentService(name) {

    private val broadcastIntent = Intent(DETAILS_INTENT_FILTER)

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onHandleIntent(intent: Intent?) {
        if (intent == null) {
            onFailure(DETAILS_INTENT_EMPTY_EXTRA)
        } else {
            val lat = intent.getDoubleExtra(LATITUDE_EXTRA, 0.0)
            val lon = intent.getDoubleExtra(LONGITUDE_EXTRA, 0.0)
            if (lat == 0.0 && lon == 0.0) {
                onFailure(DETAILS_DATA_EMPTY_EXTRA)
            } else {
                loadWeather(lat.toString(), lon.toString())
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun loadWeather(lat: String, lon: String) {
        try {
            val uri = URL("$API_URL?lat=${lat}&lon=${lon}")
            lateinit var urlConnection: HttpsURLConnection
            try {

                urlConnection = uri.openConnection() as HttpsURLConnection
                urlConnection.requestMethod = REQUEST_GET
                urlConnection.addRequestProperty(REQUEST_API_KEY, API_KEY)
                urlConnection.readTimeout = REQUEST_TIMEOUT
                if (Random.nextDouble() <= 0.3) throw IllegalStateException("Random error!")
                val weather = Gson().fromJson(
                    getLines(BufferedReader(InputStreamReader(urlConnection.inputStream))),
                    Weather::class.java
                )
                onResponse(weather)
            } catch (e: Exception) {
                onFailure(e.message ?: "Empty error")
            } finally {
                urlConnection.disconnect()
            }
        } catch (e: MalformedURLException) {
            onFailure(DETAILS_URL_MALFORMED_EXTRA)
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun getLines(reader: BufferedReader): String {
        return reader.lines().collect(Collectors.joining("\n"))
    }

    private fun onResponse(weather: Weather) {
        val fact = weather.fact
        if (fact == null) {
            onFailure(DETAILS_RESPONSE_EMPTY_EXTRA)
        } else {
            onSuccessResponse(fact.temp, fact.feels_like, fact.condition)
        }
    }

    private fun onSuccessResponse(temp: Int?, feelsLike: Int?, condition: String?) {
        putLoadResult(DETAILS_RESPONSE_SUCCESS_EXTRA)
        broadcastIntent.putExtra(DETAILS_TEMP_EXTRA, temp)
        broadcastIntent.putExtra(DETAILS_FEELS_LIKE_EXTRA, feelsLike)
        broadcastIntent.putExtra(DETAILS_CONDITION_EXTRA, condition)
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent)
    }

    private fun onFailure(failureMessage: String) {
        putFailureResult(failureMessage)
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent)
    }

    private fun putFailureResult(failureMessage: String) {
        putLoadResult(DETAILS_RESPONSE_FAILURE_EXTRA)
        broadcastIntent.putExtra(DETAILS_FAILURE_MESSAGE, failureMessage)
    }

    private fun putLoadResult(result: String) {
        broadcastIntent.putExtra(DETAILS_LOAD_RESULT_EXTRA, result)
    }

}