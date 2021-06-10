package me.fetsh.geekbrains.weather.ui.main

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import me.fetsh.geekbrains.weather.R
import me.fetsh.geekbrains.weather.RemoteData
import me.fetsh.geekbrains.weather.databinding.MainFragmentBinding
import me.fetsh.geekbrains.weather.model.CitiesSet
import me.fetsh.geekbrains.weather.model.City
import me.fetsh.geekbrains.weather.model.WeatherViewModel
import me.fetsh.geekbrains.weather.ui.details.DetailsFragment
import me.fetsh.geekbrains.weather.ui.utils.showSnackBar

private const val CITIES_SET = "LIST_OF_TOWNS_KEY"

class MainFragment : Fragment() {

    interface OnItemViewClickListener {
        fun onItemViewClick(city: City)
    }

    private var _binding: MainFragmentBinding? = null
    private val binding get() = _binding!!

    private val viewModel: WeatherViewModel by lazy {
        ViewModelProvider(this).get(WeatherViewModel::class.java)
    }
    private val adapter = MainFragmentAdapter(object : OnItemViewClickListener {
        override fun onItemViewClick(city: City) {
            activity?.supportFragmentManager?.also { manager ->
                manager.beginTransaction()
                    .replace(R.id.container, DetailsFragment.newInstance(Bundle().also { bundle ->
                        bundle.putParcelable(DetailsFragment.BUNDLE_EXTRA, city)
                    }))
                    .addToBackStack("")
                    .commit()
            }
        }
    })
    private val defaultCitiesSet : CitiesSet = CitiesSet.RUS
    private var citiesSet: CitiesSet = defaultCitiesSet

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = MainFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.mainFragmentRecyclerView.adapter = adapter
        binding.mainFragmentFAB.setOnClickListener { changeCitiesDataSet() }
        viewModel.remoteCities.observe(viewLifecycleOwner, this::renderData)
        activity?.let { activity ->
            val savedCitiesSet = CitiesSet.fromString(
                activity
                    .getPreferences(Context.MODE_PRIVATE)
                    .getString(CITIES_SET, defaultCitiesSet.toString())!!
            )
            if (defaultCitiesSet != savedCitiesSet) {
                changeCitiesDataSet()
            } else {
                viewModel.getCitiesFromLocalSourceRus()
            }
        }
    }

    private fun changeCitiesDataSet() {
        citiesSet = when(citiesSet) {
            CitiesSet.RUS -> {
                viewModel.getCitiesFromLocalSourceWorld()
                binding.mainFragmentFAB.setImageResource(R.drawable.ic_russia)
                CitiesSet.WORLD
            }
            CitiesSet.WORLD -> {
                viewModel.getCitiesFromLocalSourceRus()
                binding.mainFragmentFAB.setImageResource(R.drawable.ic_earth)
                CitiesSet.RUS
            }
        }
        activity
            ?.getPreferences(Context.MODE_PRIVATE)
            ?.edit()
            ?.putString(CITIES_SET, citiesSet.toString())
            ?.apply()
    }

    private fun renderData(data: RemoteData<List<City>, Throwable>) {
        when (data) {
            is RemoteData.Failure -> {
                binding.mainFragmentLoadingLayout.visibility = View.GONE
                binding.mainFragmentRootView.showSnackBar(
                    context = requireContext(),
                    text = R.string.error,
                    actionText = R.string.reload) {
                    viewModel.getCitiesFromLocalSourceRus()
                }
            }
            RemoteData.Loading -> {
                binding.mainFragmentLoadingLayout.visibility = View.VISIBLE
            }
            RemoteData.NotAsked -> {
                binding.mainFragmentLoadingLayout.visibility = View.VISIBLE
            }
            is RemoteData.Success -> {
                binding.mainFragmentLoadingLayout.visibility = View.GONE
                adapter.setWeather(data.value)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        adapter.removeListener()
    }

    companion object {
        fun newInstance() =
            MainFragment()
    }
}

