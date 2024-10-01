package com.example.racketrivals

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import io.github.jan.supabase.gotrue.gotrue
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Returning
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

//this is where requests are inserted
class matchCreationFragment : Fragment(), FindCourtForMatchFragment.OnCourtSelectedListener {
    private lateinit var courtNameTextView: TextView

    private var courtIdPrivate=-1L
    private var requestedPlayTime="nothing in here"
    // Variables to store user-selected date and time
    private var selectedYear = 0
    private var selectedMonth = 0
    private var selectedDay = 0
    private var selectedHour = 0
    private var selectedMinute = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)




    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val matchCourtButton = view.findViewById<Button>(R.id.matchCourtId)
        setFragmentResultListener("courtSelected") { requestKey, bundle ->
            val courtId = bundle.getLong("court_id")
            val courtName = bundle.getString("court_name")

            // Now you have the courtId and courtName in matchCreationFragment
            // Update your UI or variables accordingly
            courtIdPrivate = courtId
            courtNameTextView.text = "Court Choice: $courtName"
        }
        matchCourtButton.setOnClickListener {
            findNavController().navigate(R.id.matchCourtId)  // Trigger the navigation action
        }

        val errorReported: TextView = view.findViewById(R.id.errorMessage)
        val dateTimePickerButton: Button = view.findViewById(R.id.dateTimePickerButton)
        dateTimePickerButton.setOnClickListener {
            try {
                showDateTimePicker()
            } catch (e: Exception) {
                Log.e("DateTimePickerError", "Error in showDateTimePicker: ${e.message}")
            }
        }
        val submitRequestButton: Button = view.findViewById(R.id.submitMatchRequest)
        submitRequestButton.setOnClickListener {
            Log.d("Hello", "A  $courtIdPrivate  $requestedPlayTime")
            if (courtIdPrivate!=-1L && requestedPlayTime!="nothing in here") {
                lifecycleScope.launch(Dispatchers.IO) {
                    val user = SupabaseClientManager.client.gotrue.retrieveUserForCurrentSession(updateSession = true)
                    val userId = user.id
                    try{
                    val insertRequest=InsertRequest(request_sender_user_id=userId,request_status="PENDING", play_time = requestedPlayTime,court_id=courtIdPrivate)
                        SupabaseClientManager.client.postgrest["Request"].insert(insertRequest, returning = Returning.MINIMAL)
                        lifecycleScope.launch(Dispatchers.Main) {
                            findNavController().navigate(R.id.home)}
                    }catch (e:Exception){
                        Log.e("ExceptionTag", "An error occurred: ${e.message}", e)

                    }
                }
            }else{
                errorReported.setVisibility(View.VISIBLE)
            }
        }

        val courtId = arguments?.getLong("court_id")
        val courtName = arguments?.getString("court_name")
        Log.d("UserId", courtName.toString())
        if (courtId != null) {
            courtIdPrivate=courtId
        }
        courtNameTextView = view.findViewById(R.id.courtChoiceID)
        courtNameTextView.setText("Court Choice: ${courtName}")
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_match_creation, container, false)

    }

    override fun onCourtSelected(court_id: Long, court_name: String) {
        lifecycleScope.launch {
            withContext(Dispatchers.Main) {
                courtNameTextView.text = "Court Choice: $court_name"
                courtIdPrivate = court_id

            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun showDateTimePicker() {
        val currentDateTime = Calendar.getInstance()
        val year = currentDateTime.get(Calendar.YEAR)
        val month = currentDateTime.get(Calendar.MONTH)
        val day = currentDateTime.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(requireContext(), { _, year, monthOfYear, dayOfMonth ->
            selectedYear = year
            selectedMonth = monthOfYear
            selectedDay = dayOfMonth

            TimePickerDialog(requireContext(), { _, hourOfDay, minute ->
                selectedHour = hourOfDay
                selectedMinute = minute
                val unixTimestamp = convertToUnixTimestamp(selectedYear, selectedMonth, selectedDay, selectedHour, selectedMinute)
                unixTimestampToFormattedString(unixTimestamp)

            }, currentDateTime.get(Calendar.HOUR_OF_DAY), currentDateTime.get(Calendar.MINUTE), false).show()
        }, year, month, day).show()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun unixTimestampToFormattedString(unixTimestamp: Long): String {
        val dateTime = LocalDateTime.ofInstant(
            Instant.ofEpochSecond(unixTimestamp),
            ZoneId.systemDefault()  // Adjust the ZoneId as needed
        )
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        requestedPlayTime=dateTime.format(formatter)
        Log.d("Play-Time", requestedPlayTime)
        return dateTime.format(formatter)
    }

    private fun convertToUnixTimestamp(year: Int, month: Int, day: Int, hour: Int, minute: Int): Long {
        try {
            val calendar = Calendar.getInstance()
            calendar.set(year, month, day, hour, minute)

            Log.d("playtimeCheck",requestedPlayTime)
            return calendar.timeInMillis / 1000
        } catch (e: Exception) {
            Log.e("TimestampConversionError", "Error converting to Unix timestamp: ${e.message}")
            return 0L
        }
    }


}