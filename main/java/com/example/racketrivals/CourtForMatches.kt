package com.example.racketrivals

import Coordinates
import kotlinx.serialization.Serializable

//this was supposed to be for courts that were retrieved
@Serializable
data class CourtForMatches(
    val court_id:Long,
    val court_name:String,
    val admission_cost: Float,
    val court_location: Coordinates,
    val lights: Boolean
)
