package me.fetsh.geekbrains.weather.ui.details

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import me.fetsh.geekbrains.weather.R
import me.fetsh.geekbrains.weather.RemoteData
import me.fetsh.geekbrains.weather.databinding.DetailsFragmentBinding
import me.fetsh.geekbrains.weather.model.*
import java.lang.Exception
import java.lang.IllegalStateException

const val DETAILS_INTENT_FILTER = "DETAILS INTENT FILTER"
const val DETAILS_LOAD_RESULT_EXTRA = "LOAD RESULT"
const val DETAILS_RESPONSE_SUCCESS_EXTRA = "RESPONSE SUCCESS"
const val DETAILS_RESPONSE_FAILURE_EXTRA = "RESPONSE FAILURE"
const val DETAILS_FAILURE_MESSAGE = "FAILURE MESSAGE"
const val DETAILS_TEMP_EXTRA = "TEMPERATURE"
const val DETAILS_FEELS_LIKE_EXTRA = "FEELS LIKE"
const val DETAILS_CONDITION_EXTRA = "CONDITION"
private const val TEMP_INVALID = -100
private const val FEELS_LIKE_INVALID = -100

class DetailsFragment : Fragment() {

    companion object {
        const val BUNDLE_EXTRA = "weather"
        fun newInstance(bundle: Bundle): DetailsFragment {
            val fragment = DetailsFragment()
            fragment.arguments = bundle
            return fragment
        }
    }

    private var _binding: DetailsFragmentBinding? = null
    private val binding get() = _binding!!
    private lateinit var cityBundle: City
    private lateinit var repositoryImpl: Repository

    private val loadResultsReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val weatherRemoteData = when (intent.getStringExtra(DETAILS_LOAD_RESULT_EXTRA)) {
                DETAILS_RESPONSE_FAILURE_EXTRA -> RemoteData.Failure(
                    Exception(intent.getStringExtra(DETAILS_FAILURE_MESSAGE) ?: "Illegal state")
                )
                DETAILS_RESPONSE_SUCCESS_EXTRA -> {
                    val temp = intent.getIntExtra(DETAILS_TEMP_EXTRA, TEMP_INVALID)
                    val feelsLike = intent.getIntExtra(DETAILS_FEELS_LIKE_EXTRA, FEELS_LIKE_INVALID)
                    val condition = intent.getStringExtra(DETAILS_CONDITION_EXTRA)
                    if (temp == TEMP_INVALID || feelsLike == FEELS_LIKE_INVALID || condition == null) {
                        RemoteData.Failure(IllegalStateException("Illegal state"))
                    } else {
                        RemoteData.Success(Weather(Fact(temp, feelsLike, condition)))
                    }
                }
                else -> RemoteData.Failure(IllegalStateException("Illegal state"))
            }
            displayWeather(weatherRemoteData)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        context?.let {
            LocalBroadcastManager.getInstance(it)
                .registerReceiver(loadResultsReceiver, IntentFilter(DETAILS_INTENT_FILTER))
        }
    }

    override fun onDestroy() {
        context?.let {
            LocalBroadcastManager.getInstance(it).unregisterReceiver(loadResultsReceiver)
        }
        super.onDestroy()
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DetailsFragmentBinding.inflate(inflater, container, false)
         repositoryImpl = RepositoryImpl()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        cityBundle = arguments?.getParcelable(BUNDLE_EXTRA) ?: City()
        displayWeather(RemoteData.Loading)
        getWeather()
//        repositoryImpl.loadWeather(cityBundle.lat, cityBundle.lon, this::displayWeather)
    }

    private fun getWeather() {
        context?.let { _context ->
            val intent = Intent(_context, DetailsService::class.java)
            intent.putExtra(LATITUDE_EXTRA, cityBundle.lat)
            intent.putExtra(LONGITUDE_EXTRA, cityBundle.lon)
            _context.startService(intent)
        }
    }

    private fun displayWeather(weather: RemoteData<Weather, Throwable>) {
        binding.cityName.text = cityBundle.city
        binding.cityCoordinates.text = String.format(
            getString(R.string.city_coordinates),
            cityBundle.lat.toString(),
            cityBundle.lon.toString()
        )
        when (weather) {
            is RemoteData.Failure -> {
                binding.mainView.visibility = View.VISIBLE
                binding.loadingLayout.visibility = View.GONE
                binding.cityName.text = weather.error.message
                binding.cityCoordinates.text = ""
            }
            is RemoteData.Loading -> {
                binding.mainView.visibility = View.GONE
                binding.loadingLayout.visibility = View.VISIBLE
            }
            is RemoteData.NotAsked -> {
                binding.mainView.visibility = View.VISIBLE
                binding.loadingLayout.visibility = View.GONE
                binding.cityName.text = getString(R.string.not_asked)
                binding.cityCoordinates.text = ""
            }
            is RemoteData.Success -> {
                binding.mainView.visibility = View.VISIBLE
                binding.loadingLayout.visibility = View.GONE
                weather.value.fact?.let { fact ->
                    binding.weatherCondition.text = fact.condition
                    binding.temperatureValue.text = fact.temp.toString()
                    binding.feelsLikeValue.text = fact.feels_like.toString()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}