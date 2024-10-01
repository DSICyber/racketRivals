package com.example.racketrivals

import kotlinx.serialization.Serializable

//puts in a request that users put in when they want to play a certain day
@Serializable
data class InsertRequest(
    val request_sender_user_id: String,
    val request_status: String,
    val play_time: String,
    val court_id: Long
)