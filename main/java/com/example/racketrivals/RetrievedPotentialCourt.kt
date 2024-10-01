package com.example.racketrivals
import Coordinates
import kotlinx.serialization.Serializable

//incoming potential courts that are going to be voted
@Serializable
data class RetrievedPotentialCourt (
        val possible_court_location_id: Int,
        val court_name: String,
        val court_coordinates: Coordinates,
        val admission_cost: Float,
        val lights: Boolean,
        val status:String,

)
