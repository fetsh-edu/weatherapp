package me.fetsh.geekbrains.weather.ui.details

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.github.twocoffeesoneteam.glidetovectoryou.GlideToVectorYou
import com.squareup.picasso.Picasso
import me.fetsh.geekbrains.weather.R
import me.fetsh.geekbrains.weather.RemoteData
import me.fetsh.geekbrains.weather.databinding.DetailsFragmentBinding
import me.fetsh.geekbrains.weather.model.City
import me.fetsh.geekbrains.weather.model.Weather
import me.fetsh.geekbrains.weather.model.WeatherViewModel


class DetailsFragment : Fragment() {

    private var _binding: DetailsFragmentBinding? = null
    private val binding get() = _binding!!
    private lateinit var cityBundle: City

    private val viewModel: WeatherViewModel by lazy {
        ViewModelProvider(this).get(WeatherViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DetailsFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        cityBundle = arguments?.getParcelable(BUNDLE_EXTRA) ?: City()
        viewModel.remoteWeather.observe(viewLifecycleOwner, this::displayWeather)
        viewModel.getWeatherFromRemoteSource(cityBundle.lat, cityBundle.lon)
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
                    fact.icon?.let { icon ->
                        GlideToVectorYou.justLoadImage(
                            activity,
                            Uri.parse("https://yastatic.net/weather/i/icons/blueye/color/svg/${icon}.svg"),
                            binding.weatherIcon
                        )
                    }
                }
                Picasso
                    .get()
                    .load("https://freepngimg.com/thumb/city/36284-8-city-transparent-image.png")
                    .into(binding.headerIcon)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val BUNDLE_EXTRA = "weather"
        fun newInstance(bundle: Bundle): DetailsFragment {
            val fragment = DetailsFragment()
            fragment.arguments = bundle
            return fragment
        }
    }
}