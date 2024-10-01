package com.example.racketrivals

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.compose.ui.graphics.colorspace.connect
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.racketrivals.SupabaseClientManager.client
import io.github.jan.supabase.gotrue.gotrue
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.createChannel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


//this is the fragment that holds all of the possible courts that could become an official court
interface OnCourtClickListener {
    fun onCourtDetailsClicked(court: RetrievedPotentialCourt)
}


class CourtVotingFragment : Fragment(), OnCourtClickListener {
    private var isConnected = false
    override fun onCourtDetailsClicked(court: RetrievedPotentialCourt) {
        val mapsFragment = MapsFragment().apply {
            arguments = Bundle().apply {
                putString("court_name", court.court_name)
                putDouble("court_latitude", court.court_coordinates.latitude)
                putDouble("court_longitude", court.court_coordinates.longitude)
                putFloat("admission_cost", court.admission_cost)
                putBoolean("lights", court.lights)
            }
        }

        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.nav_host_fragment, mapsFragment)
            .addToBackStack(null)
            .commit()
    }


    private lateinit var adapter: CourtListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        savedInstanceState?.let {

        }
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_court_voting, container, false)


    }

     override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tvEmptyMessage: TextView = view.findViewById(R.id.tvEmptyMessage)

         lateinit var recyclerView: RecyclerView
         lateinit var adapter: CourtListAdapter


         try {
             val cancelButton = view.findViewById<Button>(R.id.btnCancel)
             cancelButton.setOnClickListener {
                 findNavController().navigate(R.id.home)
             }
         } catch (e: Exception) {

             Log.e("Cancel Button", "Error occurred: ${e.message}", e)
         }

         val channel = client.realtime.createChannel("updatedPotentialCourts") {
             //optional config
         }
         val changeFlow = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
             table = "Possible Court Location"
         }

         //executes co routines that update the list based off asynchronous calls
        lifecycleScope.launch(Dispatchers.IO) {
            try{

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
                //deletes any courts that user has already voted on

                var potentialCourts = client.postgrest["Possible Court Location"]
                    .select(Columns.list("possible_court_location_id,lights,admission_cost,status,court_name,court_coordinates,status"))
                    {

                        if(votedCourtsQuery.isNotEmpty()){
                        and {

                            for (vote in votedCourtsQuery) {
                            votedCourtIds.add(vote.possible_court_location_id)
                            neq("possible_court_location_id", vote.possible_court_location_id)
}
                        } }


                    }.decodeList<RetrievedPotentialCourt>()




                //this runs when every time a response is received from supabase or when a user input is made to the database
                withContext(Dispatchers.Main) {

                    recyclerView = view.findViewById(R.id.recyclerViewCourts)
                    //looks for changes in the potential courts table and updates the list accordingly
                    adapter = CourtListAdapter(this@CourtVotingFragment,lifecycleScope)
                    //sets up the adapter and the recycler view for displaying changes the list of potential courts
                    recyclerView.adapter = adapter
                    recyclerView.layoutManager = LinearLayoutManager(requireContext())
                    Log.e("RealTimeStatusCr", "Status ${potentialCourts} ")
                    adapter.submitList(potentialCourts)

                }



            }catch (e:Exception){
                Log.e("ExceptionTag", "An error occurred: ${e.message}", e)

            }
        }



    }




    }



