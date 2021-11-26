package me.fetsh.geekbrains.weather.ui.map

import android.Manifest
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import androidx.fragment.app.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import me.fetsh.geekbrains.weather.R
import me.fetsh.geekbrains.weather.databinding.FragmentMapsBinding
import java.io.IOException

class MapsFragment : Fragment() {

    private var _binding: FragmentMapsBinding? = null
    private val binding get() = _binding!!
    private lateinit var map: GoogleMap
    private val callback = OnMapReadyCallback { googleMap ->
        map = googleMap
        activateMyLocation(googleMap)
        val initialPlace = LatLng(52.52000659999999, 13.404953999999975)
        googleMap.addMarker(MarkerOptions().position(initialPlace).title(getString(R.string.marker_start)))
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(initialPlace, 15f))
        googleMap.setOnMapLongClickListener { latLng ->
            getAddressAsync(latLng)
            googleMap.clear()
            googleMap.addMarker(MarkerOptions().position(latLng))
        }

    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
        initSearchByAddress()
    }

    private fun getAddressAsync(location: LatLng) {
        context?.let { context ->
            val geoCoder = Geocoder(context)
            Thread {
                try {
                    val addresses =
                        geoCoder.getFromLocation(location.latitude, location.longitude, 1)
                    binding.textAddress.post { binding.textAddress.text = addresses[0].getAddressLine(0) }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }.start()
        }
    }

    private fun initSearchByAddress() {
        binding.buttonSearch.setOnClickListener { searchButton ->
            val geoCoder = Geocoder(searchButton.context)
            val searchText = binding.searchAddress.text.toString()
            Thread {
                try {
                    val addresses = geoCoder.getFromLocationName(searchText, 1)
                    if (addresses.size > 0) {
                        goToAddress(addresses, searchButton, searchText)
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }.start()
        }
    }

    private fun goToAddress(
        addresses: MutableList<Address>,
        view: View,
        searchText: String
    ) {
        val location = LatLng(addresses[0].latitude, addresses[0].longitude)
        view.post {
            map.clear()
            map.addMarker(MarkerOptions().position(location).title(searchText))
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(location,15f))
        }
    }

    private fun activateMyLocation(googleMap: GoogleMap) {
        context?.let {
            val isPermissionGranted =
                ContextCompat.checkSelfPermission(it, Manifest.permission.ACCESS_FINE_LOCATION) ==
                        PackageManager.PERMISSION_GRANTED
            googleMap.isMyLocationEnabled = isPermissionGranted
            googleMap.uiSettings.isMyLocationButtonEnabled = isPermissionGranted
        }
    }
}