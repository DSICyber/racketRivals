package com.example.racketrivals

import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.racketrivals.SupabaseClientManager.client
import io.github.jan.supabase.gotrue.gotrue
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Count
import io.github.jan.supabase.postgrest.query.Returning
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

//this is for each court in the court voting section
class CourtViewHolder(itemView: View,private val scope: CoroutineScope) : RecyclerView.ViewHolder(itemView) {

    private val buttonValid: Button = itemView.findViewById(R.id.buttonValid)
    private val buttonInvalid: Button = itemView.findViewById(R.id.buttonInvalid)
    private val buttonDetails: Button = itemView.findViewById(R.id.buttonDetails)
    private val courtName: EditText = itemView.findViewById(R.id.editTextCourtName)
    fun bind(court: RetrievedPotentialCourt,clickListener: OnCourtClickListener) {

        val thankYouTextView = itemView.findViewById<TextView>(R.id.textViewThankYou)
        val tag = court.possible_court_location_id
        courtName.setText(court.court_name)
        buttonValid.tag = tag
        buttonInvalid.tag = tag
        buttonDetails.tag = tag

        //this will add one vote that says yes to a certain court
        buttonValid.setOnClickListener {
            buttonValid.isClickable = false
            buttonInvalid.isClickable = false
            buttonDetails.isClickable = false
            thankYouTextView.visibility = View.VISIBLE
            clickEvent("yes",court)

        }
        //this will say no to a certain court
        buttonInvalid.setOnClickListener {
            buttonValid.isClickable = false
            buttonInvalid.isClickable = false
            buttonDetails.isClickable = false
            thankYouTextView.visibility = View.VISIBLE
            clickEvent("no",court)
        }
        //this gives the details to the tennis court
        buttonDetails.setOnClickListener {
            val mapsFragment = MapsFragment().apply {
                clickListener.onCourtDetailsClicked(court)
            }
        }
    }

    fun clickEvent(yesOrNo: String,court:RetrievedPotentialCourt) {


    }

}


