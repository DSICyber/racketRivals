package com.example.racketrivals
import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
//this is where users can click on any part of the map to set a potential court location
// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [PotentialCourtPicker.newInstance] factory method to
 * create an instance of this fragment.
 */
class PotentialCourtPicker : Fragment(), OnMapReadyCallback {
    private val sharedViewModel: SharedViewModel by activityViewModels()
    private var selectedMarker: Marker? = null
    private lateinit var mapView: MapView
    private lateinit var googleMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var confirmButton: Button
    private var locationSelectedListener: OnCourtLocationSelectedListener? = null
    private var selectedLatLng: LatLng? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Log.d("MapFragment", "onCreateView")
        return inflater.inflate(R.layout.fragment_potential_court_picker, container, false)
    }

    fun setLocationSelectedListener(listener: OnCourtLocationSelectedListener) {
        locationSelectedListener = listener
    }
    interface OnCourtLocationSelectedListener {
        fun onLocationSelected(latLng: LatLng)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("MapFragment", "onViewCreated")

        mapView = view.findViewById(R.id.mapView)
        confirmButton = view.findViewById(R.id.btnConfirmLocation)

        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
    }

    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap
        // ... existing code ...
        updateLocationUI(this.googleMap)
        googleMap.setOnMapClickListener { latLng ->
            selectedLatLng = latLng

            selectedMarker?.remove()
            selectedMarker = googleMap.addMarker(MarkerOptions().position(latLng))
            confirmButton.visibility = View.VISIBLE // Show the button
        }

        confirmButton?.setOnClickListener {
            selectedLatLng?.let { latLng ->
                Log.d("Potential", "Selected Location: Lat = ${latLng.latitude}, Lng = ${latLng.longitude}")
                sharedViewModel.selectedLocation.value = latLng
                locationSelectedListener?.onLocationSelected(latLng)
                requireActivity().supportFragmentManager.popBackStack()
            }
        }
    }

    private fun updateLocationUI(map: GoogleMap) {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d("MapFragment", "Location permission not granted")
            // Request missing location permissions
            return
        }
        googleMap.isMyLocationEnabled = true

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val hamiltonLatLng = LatLng(43.2557, -79.8711) // Latitude and longitude for Hamilton, Ontario
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(hamiltonLatLng, 10f))
            } else {
                Log.d("MapFragment", "Location is null")
            }
        }.addOnFailureListener {
            Log.e("MapFragment", "Error getting location", it)
        }
    }

    // Add similar log statements to other lifecycle methods
    override fun onStart() {
        super.onStart()
        mapView.onStart()
        Log.d("MapFragment", "onStart")
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
        Log.d("MapFragment", "onResume")
    }

    override fun onPause() {
        mapView.onPause()
        super.onPause()
        Log.d("MapFragment", "onPause")
    }

    override fun onStop() {
        mapView.onStop()
        super.onStop()
        Log.d("MapFragment", "onStop")
    }

    override fun onDestroyView() {
        mapView.onDestroy()
        super.onDestroyView()
        Log.d("MapFragment", "onDestroyView")
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
        Log.d("MapFragment", "onSaveInstanceState")
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
        Log.d("MapFragment", "onLowMemory")
    }
}
