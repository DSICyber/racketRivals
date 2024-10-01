package com.example.racketrivals


import kotlinx.serialization.Serializable

//class for each vote 
@Serializable
data class Vote(
    val possible_court_location_id: Long,
    val vote_type: String,
    val user_id: String
)

