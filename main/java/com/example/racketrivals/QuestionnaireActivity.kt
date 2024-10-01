package com.example.racketrivals
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import io.github.jan.supabase.gotrue.gotrue
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.net.URLEncoder
//questionaire to determine elo
class QuestionnaireActivity : AppCompatActivity() {
    fun uriToByteArray(context: Context, uri: Uri): ByteArray {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                inputStreamToByteArray(inputStream)
            } ?: ByteArray(0) // Return an empty ByteArray if the input stream is null
        } catch (e: Exception) {
            e.printStackTrace()
            ByteArray(0) // Return an empty ByteArray in case of an exception
        }
    }


    fun inputStreamToByteArray(inputStream: InputStream): ByteArray {
        ByteArrayOutputStream().use { buffer ->
            val data = ByteArray(1024)
            var length: Int
            while (inputStream.read(data, 0, data.size).also { length = it } != -1) {
                buffer.write(data, 0, length)
            }
            buffer.flush()
            return buffer.toByteArray()
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.questionaire_activity_layout)
        val client = SupabaseClientManager.client
        var loginSuccess = false

        val userEmail = intent.getStringExtra("EMAIL") ?: "Default Email"
        val userPassword = intent.getStringExtra("PASSWORD") ?: "Default Password"
        val fullName = intent.getStringExtra("FULL_NAME") ?: "Default Name"
        val profilePhoto = intent.getStringExtra("PROFILE_PHOTO") ?: "Default Name"
        val imageUri = profilePhoto.let { Uri.parse(it) }


        val createAccountButton = findViewById<Button>(R.id.createAccountButton)
        createAccountButton.setOnClickListener {
            if (areAllOptionsSelected()) {

                Log.d("QuestionnaireActivity", "Email: $userEmail")
                Log.d("QuestionnaireActivity", "Password: $userPassword")
                Log.d("QuestionnaireActivity", "Full Name: $fullName")
                Log.d("QuestionnaireActivity", "Profile Photo: $profilePhoto")
                Log.d("QuestionnaireActivity", "Image URI: $imageUri")

                val byteArray = uriToByteArray(this, imageUri)


                lifecycleScope.launch(Dispatchers.IO) {
                    val fileName = URLEncoder.encode(userEmail, "UTF-8") + ".png"
                    try {
                        val bucket = client.storage["avatars"]
                        bucket.upload(fileName, byteArray, upsert = false)

                    } catch (e: Exception) {

                        Log.e("UploadError", "Error during file upload: ${e.message}", e)


                    }
                    try {


                        val bucket = client.storage["avatars"]


                        val user = client.gotrue.signUpWith(Email) {
                            email = userEmail
                            password = userPassword
                            data = buildJsonObject {
                                put("avatar_url", fileName)
                                put("full_name", fullName)

                            }


                        }
                    } catch (e: Exception) {
                        // Handle the error here
                        Log.e("SignupError", "Error during signup or upload: ${e.message}", e)
                        // You might also want to show an error message to the user
                    }

                    try {

                        val eloScored = calculateEloScore()
                        Log.d("QuestionnaireActivity", "Elo: $eloScored")
                        client.postgrest["profiles"].update(
                            {
                                set("elo", eloScored)
                            }

                        ) {

                            eq("avatar_url", fileName)

                        }
                        val successMessage =
                            findViewById<TextView>(R.id.accountCreationSuccessMessage)
                        val failureMessage =
                            findViewById<TextView>(R.id.accountCreationFailedMessage)
                        withContext(Dispatchers.Main) {
                            successMessage.visibility = View.VISIBLE
                            Handler(Looper.getMainLooper()).postDelayed({
                                val intent = Intent(
                                    this@QuestionnaireActivity,
                                    LogInScreen::class.java
                                ) // Replace NextActivity with your target activity
                                startActivity(intent)
                                finish()
                            }, 10)
                            finish()
                        }
                    } catch (e: Exception) {
                        val failMessage = findViewById<TextView>(R.id.accountCreationFailedMessage)
                        failMessage.visibility = View.VISIBLE

                    }


                }


            } else {
                Toast.makeText(
                    this,
                    "Please select an option for each question",
                    Toast.LENGTH_SHORT
                ).show()
            }


            val backButton = findViewById<Button>(R.id.backButton)
            backButton.setOnClickListener {
                // Intent to start AccountCreationActivity
                val intent = Intent(this, LogInScreen::class.java)
                startActivity(intent)

            }
        }
    }
    private fun areAllOptionsSelected(): Boolean {
        val groups = listOf(
            findViewById<RadioGroup>(R.id.experienceLevelRadioGroup),
            findViewById<RadioGroup>(R.id.durationRadioGroup),
            findViewById<RadioGroup>(R.id.playFrequencyRadioGroup),
            findViewById<RadioGroup>(R.id.tennisLessonsRadioGroup),
            findViewById<RadioGroup>(R.id.tennisCompetitionsRadioGroup)
        )

        return groups.all { it.checkedRadioButtonId != -1 }
    }

    private fun calculateEloScore(): Int {
        var eloScore = 0
        val groups = listOf(
            findViewById<RadioGroup>(R.id.experienceLevelRadioGroup),
            findViewById<RadioGroup>(R.id.durationRadioGroup),
            findViewById<RadioGroup>(R.id.playFrequencyRadioGroup),
            findViewById<RadioGroup>(R.id.tennisLessonsRadioGroup),
            findViewById<RadioGroup>(R.id.tennisCompetitionsRadioGroup)
        )

        for (group in groups) {
            val selectedOptionId = group.checkedRadioButtonId
            if (selectedOptionId != -1) {
                val selectedRadioButton = findViewById<RadioButton>(selectedOptionId)
                val score = selectedRadioButton.tag.toString().toInt()
                eloScore += score
            }
        }

        return eloScore
    }
}

