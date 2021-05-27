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
import me.fetsh.geekbrains.weather.ui.details.DetailsFragment
import me.fetsh.geekbrains.weather.ui.utils.showSnackBar

class MainFragment : Fragment() {

    interface OnItemViewClickListener {
        fun onItemViewClick(weather: Weather)
    }

    private var _binding: MainFragmentBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MainViewModel by lazy {
        ViewModelProvider(this).get(MainViewModel::class.java)
    }
    private val adapter = MainFragmentAdapter(object : OnItemViewClickListener {
        override fun onItemViewClick(weather: Weather) {
            activity?.supportFragmentManager?.also { manager ->
                manager.beginTransaction()
                    .add(R.id.container, DetailsFragment.newInstance(Bundle().also { bundle ->
                        bundle.putParcelable(DetailsFragment.BUNDLE_EXTRA, weather)
                    }))
                    .addToBackStack("")
                    .commitAllowingStateLoss()
            }
        }
    })
    private var isDataSetRus: Boolean = true

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
        binding.mainFragmentFAB.setOnClickListener { changeWeatherDataSet() }
        viewModel.getLiveData().observe(viewLifecycleOwner, this::renderData)
        viewModel.getWeatherFromLocalSourceRus()
    }

    private fun changeWeatherDataSet() {
        if (isDataSetRus) {
            viewModel.getWeatherFromLocalSourceWorld()
            binding.mainFragmentFAB.setImageResource(R.drawable.ic_russia)
        } else {
            viewModel.getWeatherFromLocalSourceRus()
            binding.mainFragmentFAB.setImageResource(R.drawable.ic_earth)
        }
        isDataSetRus = !isDataSetRus
    }

    private fun renderData(data: RemoteData<List<Weather>, Throwable>) {
        when (data) {
            is RemoteData.Failure -> {
                binding.mainFragmentLoadingLayout.visibility = View.GONE
                binding.mainFragmentRootView.showSnackBar(
                    context = context!!,
                    text = R.string.error,
                    actionText = R.string.reload) {
                    viewModel.getWeatherFromLocalSourceRus()
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

