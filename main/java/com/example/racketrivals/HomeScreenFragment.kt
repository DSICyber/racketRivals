package com.example.racketrivals

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.racketrivals.SupabaseClientManager.client
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.createChannel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject

//this is the home screen that was supposed show an upcoming match
class HomeScreenFragment : Fragment() {
    lateinit var requestInfo: JSONObject
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home_screen, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)






        // Assuming your button's ID in the layout is "buttonId"
        val submitCourtButton = view.findViewById<Button>(R.id.submitCourtLocationButton)
        submitCourtButton.setOnClickListener {
            lifecycleScope.launch(Dispatchers.Main) {
                findNavController().navigate(R.id.fragment_court_submission)
            }

        }
        val voteButton = view.findViewById<Button>(R.id.voteCourtLocationButton)
        voteButton.setOnClickListener {
            lifecycleScope.launch(Dispatchers.Main) {
                findNavController().navigate(R.id.fragment_court_voting)
            }

        }

    }
}