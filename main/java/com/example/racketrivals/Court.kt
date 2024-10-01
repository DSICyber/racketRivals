package com.example.racketrivals

import Coordinates
import kotlinx.serialization.Serializable

// Custom serializer for LocalDateTime

/**
 * courts that have been verified
 */
@Serializable
data class Court(
    val court_name:String,
    val admission_cost: Float,
    val court_location: Coordinates,
    val lights: Boolean
)
