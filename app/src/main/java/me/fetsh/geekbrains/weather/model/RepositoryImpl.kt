package me.fetsh.geekbrains.weather.model

import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresApi
import com.google.gson.Gson
import me.fetsh.geekbrains.weather.RemoteData
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.IllegalArgumentException
import java.lang.IllegalStateException
import java.net.MalformedURLException
import java.net.URL
import java.util.stream.Collectors
import javax.net.ssl.HttpsURLConnection
import kotlin.random.Random

private const val API_KEY = "754d7bda-d82f-4100-a5a3-db799ab29a2e"
private const val API_URL = "https://api.weather.yandex.ru/v2/forecast"

class RepositoryImpl : Repository {

    override fun getWeatherFromServer(lat: Double, lon: Double) = RemoteData.NotAsked

    override fun getCitiesFromLocalStorageRus(): List<City> = getRussianCities()

    override fun getCitiesFromLocalStorageWorld(): List<City> = getWorldCities()

    @RequiresApi(Build.VERSION_CODES.N)
    override fun loadWeather(lat: Double, lon: Double, listener: (RemoteData<Weather, Throwable>) -> Unit) {
        val handler = Handler(Looper.getMainLooper())
        try {
            val uri = URL("${API_URL}?lat=${lat}&lon=${lon}")
            Thread (Runnable {
                lateinit var urlConnection: HttpsURLConnection
                try {
                    urlConnection = uri.openConnection() as HttpsURLConnection
                    urlConnection.requestMethod = "GET"
                    urlConnection.addRequestProperty(
                        "X-Yandex-API-Key",
                        API_KEY
                    )
                    urlConnection.readTimeout = 10000
                    if (Random.nextDouble() <= 0.3) throw IllegalStateException("Random error!")
                    val weather = Gson().fromJson(
                        getLines(BufferedReader(InputStreamReader(urlConnection.inputStream))),
                        Weather::class.java
                    )
                    handler.post { listener(RemoteData.Success(weather)) }
                } catch (e: Exception) {
                    Log.e("", "Fail connection", e)
                    e.printStackTrace()
                    handler.post { listener(RemoteData.Failure(e)) }
                } finally {
                    urlConnection.disconnect()
                }
            }).start()
        } catch (e: MalformedURLException) {
            Log.e("", "Fail URI", e)
            e.printStackTrace()
            handler.post { listener(RemoteData.Failure(e)) }
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun getLines(reader: BufferedReader): String {
        return reader.lines().collect(Collectors.joining("\n"))
    }

}

fun getWorldCities(): List<City> = listOf(
    City("Лондон", 51.5085300, -0.1257400),
    City("Токио", 35.6895000, 139.6917100),
    City("Париж", 48.8534100, 2.3488000),
    City("Берлин", 52.52000659999999, 13.404953999999975),
    City("Рим", 41.9027835, 12.496365500000024),
    City("Минск", 53.90453979999999, 27.561524400000053),
    City("Стамбул", 41.0082376, 28.97835889999999),
    City("Вашингтон", 38.9071923, -77.03687070000001),
    City("Киев", 50.4501, 30.523400000000038),
    City("Пекин", 39.90419989999999, 116.40739630000007)
)
fun getRussianCities(): List<City> = listOf(
    City("Москва", 55.755826, 37.617299900000035),
    City("Санкт-Петербург", 59.9342802, 30.335098600000038),
    City("Новосибирск", 55.00835259999999, 82.93573270000002),
    City("Екатеринбург", 56.83892609999999, 60.60570250000001),
    City("Нижний Новгород", 56.2965039, 43.936059),
    City("Казань", 55.8304307, 49.06608060000008),
    City("Челябинск", 55.1644419, 61.4368432),
    City("Омск", 54.9884804, 73.32423610000001),
    City("Ростов-на-Дону", 47.2357137, 39.701505),
    City("Уфа", 54.7387621, 55.972055400000045)
)