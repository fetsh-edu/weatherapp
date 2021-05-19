package me.fetsh.geekbrains.weather.ui.main


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import me.fetsh.geekbrains.weather.R
import me.fetsh.geekbrains.weather.RemoteData
import me.fetsh.geekbrains.weather.databinding.MainFragmentBinding
import me.fetsh.geekbrains.weather.model.Weather

class MainFragment : Fragment() {

    companion object {
        fun newInstance() = MainFragment()
    }

    private var _binding: MainFragmentBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: MainViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = MainFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        viewModel.getLiveData().observe(viewLifecycleOwner, this::renderData)
        viewModel.getWeather()
    }
    private fun renderData(data: RemoteData<Weather, Throwable>) {
        when (data) {
            is RemoteData.Success -> {
                val weatherData = data.value
                binding.loadingLayout.visibility = View.GONE
                setData(weatherData)
                Snackbar.make(binding.mainView, "Success", Snackbar.LENGTH_LONG).show()
            }
            is RemoteData.Loading -> {
                binding.loadingLayout.visibility = View.VISIBLE
            }
            is RemoteData.Failure -> {
                binding.loadingLayout.visibility = View.GONE
                Snackbar
                    .make(binding.mainView, "Error", Snackbar.LENGTH_INDEFINITE)
                    .setAction("Reload") { viewModel.getWeather() }
                    .show()
            }
            RemoteData.NotAsked -> {
                binding.loadingLayout.visibility = View.GONE
                binding.cityName.text = "Initial"
            }
        }

    }
    private fun setData(weatherData: Weather) {
        binding.cityName.text = weatherData.city.city
        binding.cityCoordinates.text = String.format(
            getString(R.string.city_coordinates),
            weatherData.city.lat.toString(),
            weatherData.city.lon.toString()
        )
        binding.temperatureValue.text = weatherData.temperature.toString()
        binding.feelsLikeValue.text = weatherData.feelsLike.toString()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}