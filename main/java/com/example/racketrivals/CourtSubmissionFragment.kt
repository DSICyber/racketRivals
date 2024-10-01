package com.example.racketrivals

import Coordinates
import PotentialCourts
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.racketrivals.SupabaseClientManager.client
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Returning
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CourtSubmissionFragment : Fragment(){

    var isLocationSelected = false
    var lat= 0.0
    var long=0.0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_court_submission, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val priceEditText: EditText =
            view.findViewById(R.id.etPrice)
        val radioGroup: RadioGroup = view.findViewById(R.id.rgLights)
        val courtNameEditText: EditText =
            view.findViewById(R.id.etCourtName)





        val sharedViewModel: SharedViewModel by activityViewModels()

        //this switches to the google map that lets users click a coordinate to claim as a court
        val selectCourtButton = view.findViewById<Button>(R.id.selectCourtButton)

        selectCourtButton.setOnClickListener {

            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.replace(R.id.nav_host_fragment, PotentialCourtPicker())
            transaction.addToBackStack(null)
            transaction.commit()
        }

        //set the latitude and logitude from the google maps activity
        sharedViewModel.selectedLocation.observe(viewLifecycleOwner) { latLng ->
            Log.d(
                "CourtSubmissionFragment",
                "Selected Location: Lat = ${latLng.latitude}, Lng = ${latLng.longitude}"
            )
            lat= latLng.latitude.toDouble()
            long=latLng.longitude.toDouble()


            isLocationSelected = true
        }

        val cancelButton = view.findViewById<Button>(R.id.cancel_button)

        cancelButton.setOnClickListener {
            findNavController().navigateUp() // Navigate up in the navigation graph
        }
        //check if any form validation is wrong
        val submitButton = view.findViewById<Button>(R.id.court_submit)
        submitButton.setOnClickListener {

            val selectedRadioButtonId = radioGroup.checkedRadioButtonId
            val selectedRadioButton: RadioButton? = view.findViewById(selectedRadioButtonId)
            val lightsOn = selectedRadioButton?.tag.toString().toBoolean()
            // Validate Price of Admission
            val isPriceValid = priceEditText.text.toString().toFloatOrNull()!= null
            val priceOfAdmission = priceEditText.text.toString().toFloat()

            // Validate Lights
            val isLightsSelected = radioGroup.checkedRadioButtonId != -1

            // Validate Court Name
            val courtName = courtNameEditText.text.toString()
            val isCourtNameValid = courtName.isNotEmpty()

            if (isLocationSelected && isPriceValid && isLightsSelected && isCourtNameValid) {
                lifecycleScope.launch(Dispatchers.IO) {

                    val cords=Coordinates(
                        latitude= lat,
                        longitude= long
                    )
                    val newCourt=PotentialCourts(

                        court_name =courtName,
                        court_coordinates =cords,
                        admission_cost =priceOfAdmission,
                        lights =lightsOn,
                        status="UNDECIDED"
                    )
                    //enters this into the possible court location
                    try{
                    val response=client.postgrest["Possible Court Location"].insert(newCourt, returning = Returning.MINIMAL)


                        lifecycleScope.launch(Dispatchers.Main) {
                            val successMessageTextView: TextView = view.findViewById(R.id.tvSuccessMessage)
                            successMessageTextView.visibility = View.VISIBLE

                            // Delay the fragment transaction to give time for the message to be seen
                            Handler(Looper.getMainLooper()).postDelayed({
                                requireActivity().supportFragmentManager.popBackStack()
                            }, 2000) // Delay for 2 seconds
                        }
                    } catch (e: Exception) {
                        Log.e("SupabaseException", "Exception during insert: ${e.message}", e)
                        val failedMessageTextView: TextView = view.findViewById(R.id.failedMessage)
                        failedMessageTextView.visibility = View.VISIBLE
                }
                }


                }
            }
        }


    }



