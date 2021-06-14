package me.fetsh.geekbrains.weather.ui.main

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
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
import java.io.IOException

private const val CITIES_SET = "LIST_OF_TOWNS_KEY"
private const val REFRESH_PERIOD = 0L
private const val MINIMAL_DISTANCE = 0f

class MainFragment : Fragment() {

    interface OnItemViewClickListener {
        fun onItemViewClick(city: City)
    }

    private var _binding: MainFragmentBinding? = null
    private val binding get() = _binding!!
    private val locationManager : LocationManager? by lazy {
        activity?.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
    }

    private val viewModel: WeatherViewModel by lazy {
        ViewModelProvider(this).get(WeatherViewModel::class.java)
    }
    private val adapter = MainFragmentAdapter(object : OnItemViewClickListener {
        override fun onItemViewClick(city: City) = openDetailsFragment(city)
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
        binding.mainFragmentFABLocation.setOnClickListener { checkLocationPermission() }
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

    private fun checkLocationPermission() {
        activity?.let {
            when {
                ContextCompat.checkSelfPermission(it, Manifest.permission.ACCESS_FINE_LOCATION) ==
                        PackageManager.PERMISSION_GRANTED -> {
                    getLocation()
                }
                shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                    showRationaleDialog()
                }
                else -> {
                    requestPermission()
                }
            }
        }
    }

    private val activityResultLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                getLocation()
            } else {
                showDialog(
                    getString(R.string.dialog_title_no_gps),
                    getString(R.string.dialog_message_no_gps)
                )
            }
        }

    private fun requestPermission() {
        activityResultLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    private fun showRationaleDialog() {
        activity?.let { activity ->
            AlertDialog.Builder(activity)
                .setTitle(getString(R.string.dialog_rationale_title))
                .setMessage(getString(R.string.dialog_rationale_meaasge))
                .setPositiveButton(getString(R.string.dialog_rationale_give_access)) { _, _ ->
                    requestPermission()
                }
                .setNegativeButton(getString(R.string.dialog_rationale_decline)) { dialog, _ -> dialog.dismiss() }
                .create()
                .show()
        }
    }

    private fun showDialog(title: String, message: String) {
        activity?.let { activity ->
            AlertDialog.Builder(activity)
                .setTitle(title)
                .setMessage(message)
                .setNegativeButton(getString(R.string.dialog_button_close)) { dialog, _ -> dialog.dismiss() }
                .create()
                .show()
        }
    }

    private val onLocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            locationManager?.removeUpdates(this)
            context?.let {
                getAddressAsync(it, location)
            }
        }
        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {
        }
        override fun onProviderEnabled(provider: String) {
        }
        override fun onProviderDisabled(provider: String) {
        }
    }

    private fun getLocation() {
        activity?.let { activity ->
            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
                showRationaleDialog()
            } else {
                locationManager?.let { locationManager ->
                    if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                        locationManager.getProvider(LocationManager.GPS_PROVIDER)?.let { provider ->
                            locationManager.requestLocationUpdates(
                                LocationManager.GPS_PROVIDER,
                                REFRESH_PERIOD,
                                MINIMAL_DISTANCE,
                                onLocationListener
                            )
                        }
                    } else {
                        val location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                        if (location == null) {
                            showDialog(
                                getString(R.string.dialog_title_gps_turned_off),
                                getString(R.string.dialog_message_last_location_unknown)
                            )
                        } else {
                            getAddressAsync(activity, location)
                            showDialog(
                                getString(R.string.dialog_title_gps_turned_off),
                                getString(R.string.dialog_message_last_known_location)
                            )
                        }
                    }
                }
            }
        }
    }

    private fun getAddressAsync(context: Context, location: Location) {
        val geoCoder = Geocoder(context)
        Thread {
            try {
                val addresses = geoCoder.getFromLocation(
                    location.latitude,
                    location.longitude,
                    1
                )
                binding.mainFragmentFAB.post {
                    showAddressDialog(addresses[0].getAddressLine(0), location)
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }.start()
    }

    private fun showAddressDialog(address: String, location: Location) {
        activity?.let {
            AlertDialog.Builder(it)
                .setTitle(getString(R.string.dialog_address_title))
                .setMessage(address)
                .setPositiveButton(getString(R.string.dialog_address_get_weather)) { _, _ ->
                    openDetailsFragment(City(address, location.latitude, location.longitude))
                }
                .setNegativeButton(getString(R.string.dialog_button_close)) { dialog, _ -> dialog.dismiss() }
                .create()
                .show()
        }
    }

    private fun openDetailsFragment(city: City) {
        activity?.supportFragmentManager?.also { manager ->
            manager.beginTransaction()
                .replace(R.id.container, DetailsFragment.newInstance(Bundle().also { bundle ->
                    bundle.putParcelable(DetailsFragment.BUNDLE_EXTRA, city)
                }))
                .addToBackStack("")
                .commit()
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

