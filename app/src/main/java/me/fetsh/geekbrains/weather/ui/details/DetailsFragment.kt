package me.fetsh.geekbrains.weather.ui.details


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import me.fetsh.geekbrains.weather.R
import me.fetsh.geekbrains.weather.RemoteData
import me.fetsh.geekbrains.weather.databinding.DetailsFragmentBinding
import me.fetsh.geekbrains.weather.model.City
import me.fetsh.geekbrains.weather.model.Repository
import me.fetsh.geekbrains.weather.model.RepositoryImpl
import me.fetsh.geekbrains.weather.model.Weather

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
        repositoryImpl.loadWeather(cityBundle.lat, cityBundle.lon, this::displayWeather)
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