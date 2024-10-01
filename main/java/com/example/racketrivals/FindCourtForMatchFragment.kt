package com.example.racketrivals

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.racketrivals.SupabaseClientManager.client
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.SphericalUtil
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


//This is a choice google map that lets users choose what court they want to use
class FindCourtForMatchFragment : Fragment(), OnMapReadyCallback {


    interface OnCourtSelectedListener {
        fun onCourtSelected(courtId: Long, courtName: String)
    }
    private val allMarkers: MutableList<Marker> = mutableListOf()
    private var listener: OnCourtSelectedListener? = null
    private lateinit var mMap: GoogleMap

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as? OnCourtSelectedListener
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_find_court_for_match, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val courtsForMatches =
                    client.postgrest["Court"].select().decodeList<CourtForMatches>()
                withContext(Dispatchers.Main) {
                    for (court in courtsForMatches) {
                        val location =
                            LatLng(court.court_location.latitude, court.court_location.longitude)
                        val marker = mMap.addMarker(
                            MarkerOptions()
                                .position(location)
                                .title(court.court_name)
                                .snippet("Price: ${court.admission_cost} Lights: ${if (court.lights) "Available" else "Unavailable"}")

                        )
                        marker?.let { allMarkers.add(it) }
                        if (marker != null) {
                            marker.showInfoWindow()
                        }
                        mMap.setOnCameraIdleListener {
                            val center = googleMap.cameraPosition.target
                            val closestMarker = findClosestMarker(center, allMarkers)
                            closestMarker?.showInfoWindow()
                        }
                        marker?.tag = court.court_id
                    }
                    if (courtsForMatches.isNotEmpty()) {
                        val firstCourtLocation = LatLng(43.225351, -79.879156)
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(firstCourtLocation, 10f))
                    }
                }
            } catch (e: Exception) {
                Log.e("DatabaseError", "Error: ${e.message}")
            }
        }

        mMap.setOnMarkerClickListener { marker ->
            val courtId = marker.tag as? Long
            val courtName = marker.title
            if (courtId != null && courtName != null) {
                Log.d("Has Been Clicked", "This is a debug message")

                setFragmentResult("courtSelected", bundleOf("court_id" to courtId, "court_name" to courtName))

                findNavController().popBackStack()



            }
            false
        }
    }
    fun findClosestMarker(center: LatLng, markers: List<Marker>): Marker? {
        return markers.minByOrNull { marker ->
            val markerLocation = LatLng(marker.position.latitude, marker.position.longitude)
            SphericalUtil.computeDistanceBetween(center, markerLocation)
        }
    }

    }
