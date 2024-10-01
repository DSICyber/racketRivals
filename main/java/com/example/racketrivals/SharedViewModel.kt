package com.example.racketrivals

import PotentialCourts
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.example.racketrivals.SupabaseClientManager.client
import com.google.android.gms.maps.model.LatLng
import io.github.jan.supabase.gotrue.gotrue
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.RealtimeChannel
import io.github.jan.supabase.realtime.createChannel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

// Allows for coordinates to be shared between potentialcourtpicker and courtsubmission fragment
class SharedViewModel : ViewModel() {

    init {
        startListeningToChanges();

    }
    // Backing field (private)
    private val _potentialCourtData = MutableLiveData<List<RetrievedPotentialCourt>>()

    // Exposed LiveData (public)
    val potentialCourtData: LiveData<List<RetrievedPotentialCourt>> get() = _potentialCourtData

    // MutableLiveData to store selected location
    val selectedLocation: MutableLiveData<LatLng> = MutableLiveData()

    // Function to start listening to changes in the "Possible Court Location" table
    fun startListeningToChanges() {
        val channel = client.realtime.createChannel("channelId")
        val changeFlow = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
            table = "Possible Court Location"
        }
        var  potentialCourts: List<RetrievedPotentialCourt>

        // Launch a coroutine to collect changes
        viewModelScope.launch {
            connectToWebSocket();
            changeFlow.collect { action ->
                when (action) {
                    is PostgresAction.Delete -> {
                        // Handle delete action
                    }
                    is PostgresAction.Insert -> {
                        //check if user has voted on any courts
                        val userId =client.gotrue.retrieveUserForCurrentSession(updateSession = true).id
                        val votedCourtsQuery = client.postgrest["Vote"]
                            .select(Columns.list("possible_court_location_id","vote_type","user_id")) {
                                eq("user_id",userId)
                            }.decodeList<Vote>()

                        val votedCourtIds = mutableListOf<Long>()

                        for (vote in votedCourtsQuery) {
                            votedCourtIds.add(vote.possible_court_location_id)
                            Log.d("CourtVotingFragment", "Voted Court ID: ${vote.possible_court_location_id}")
                        }
                     potentialCourts = client.postgrest["Possible Court Location"]
                        .select(Columns.list("possible_court_location_id,lights,admission_cost,status,court_name,court_coordinates,status")) {
                            if (votedCourtsQuery.isNotEmpty()) {
                                and {
                                    for (vote in votedCourtsQuery) {
                                        votedCourtIds.add(vote.possible_court_location_id)
                                        neq(
                                            "possible_court_location_id",
                                            vote.possible_court_location_id
                                        )
                                    }
                                }
                            }
                        }.decodeList<RetrievedPotentialCourt>()

                    Log.e("SharedViewModelChange", "Status ${potentialCourts} ")

                    // Update the LiveData with the new data
                    _potentialCourtData.value = potentialCourts
                }


                    is PostgresAction.Select -> TODO()
                    is PostgresAction.Update -> TODO()

                    }


                }

            channel.join();
            }


        }


    }


    suspend fun connectToWebSocket() {
    try {
        var isConnected=false;
        if(!isConnected) {
            Log.e("RealTimeStatus", "Status ${SupabaseClientManager.client.realtime.status.value} ")
            SupabaseClientManager.client.realtime.connect()
            Log.e(
                "RealTimeStatus",
                "Status ${SupabaseClientManager.client.realtime.status.value} "
            )
            isConnected=true;
        }

    } catch (e: Exception) {
        Log.e("ConnectionError", "Error in connection ${e.message},  ")
    }}

