package com.example.racketrivals

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions


//this is the maps fragment that lets users look around for a court to play at
class MapsFragment : Fragment(), OnMapReadyCallback {
    private lateinit var mMap: GoogleMap

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_maps, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val buttonGoBack = view.findViewById<Button>(R.id.buttonGoBack)
        // Retrieve data from the bundle
        val courtName = arguments?.getString("court_name")
        val latitude = arguments?.getDouble("court_latitude", 0.0)
        val longitude = arguments?.getDouble("court_longitude", 0.0)
        val admissionCost = arguments?.getFloat("admission_cost")
        val lights = arguments?.getBoolean("lights")

        // Set up TextViews
        view.findViewById<TextView>(R.id.tvCourtName).text = courtName
        view.findViewById<TextView>(R.id.tvLights).text = "Lights: ${if (lights == true) "Yes" else "No"}"
        view.findViewById<TextView>(R.id.tvAdmissionCostLabel).text = "Admission Cost: ${admissionCost.toString()}"
        buttonGoBack.setOnClickListener {
            // Perform the fragment transaction to go back to CourtVotingFragment
            requireActivity().supportFragmentManager.popBackStack()
        }
        // Set up the map fragment
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Retrieve latitude and longitude from the arguments
        val latitude = arguments?.getDouble("court_latitude", 0.0) ?: 0.0
        val longitude = arguments?.getDouble("court_longitude", 0.0) ?: 0.0
        val location = LatLng(latitude, longitude)

        // Add a marker for the court location
        mMap.addMarker(MarkerOptions().position(location).title(arguments?.getString("court_name")))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))
    }
}
