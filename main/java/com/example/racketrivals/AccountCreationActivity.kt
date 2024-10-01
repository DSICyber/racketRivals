package com.example.racketrivals

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter

class AccountCreationActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.account_creation_screen)

        // Find the ComposeView from the layout
        val composeView = findViewById<ComposeView>(R.id.compose_view)

        //  holds the selected image
        var selectedImageUri by mutableStateOf<Uri?>(null)


        composeView.setContent {
            val imagePickerLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.GetContent()
            ) { uri: Uri? ->
                selectedImageUri = uri // Update the state with the selected image URI
            }

            //allows user to pick image for profile
            ProfileImagePicker(selectedImageUri) {
                // When the Composable is clicked, launch the image picker
                imagePickerLauncher.launch("image/*")
            }
        }

        //show questionaire to measure elo
        val goToQuestionnaireTextView = findViewById<TextView>(R.id.textViewGoToQuestionnaire)

        // Set an OnClickListener
        goToQuestionnaireTextView.setOnClickListener {
            // Create an Intent to start QuestionnaireActivity
            val intent = Intent(this, QuestionnaireActivity::class.java)

            //this will be sent to the questionnaire page
            val email = findViewById<EditText>(R.id.editTextEmail).text.toString()
            val password = findViewById<EditText>(R.id.editTextPassword).text.toString()
            val fullName = findViewById<EditText>(R.id.editTextFullName).text.toString()
            val imageUriString = selectedImageUri.toString()
            if (isValidInput(email, password, fullName,imageUriString)) {
            // Put the collected data as extras in the Intent
            intent.putExtra("EMAIL", email)
            intent.putExtra("PASSWORD", password)
            intent.putExtra("FULL_NAME", fullName)
            intent.putExtra("PROFILE_PHOTO", imageUriString)
            // Start the QuestionnaireActivity
            startActivity(intent)
        }}

        //lets you go back a page
        val cancelButton = findViewById<Button>(R.id.buttonCancel)

        // Set OnClickListener to the Cancel Button
        cancelButton.setOnClickListener {
            // Intent to start LoginScreen
            val intent = Intent(this, LogInScreen::class.java)
            startActivity(intent)
            finish() // Optionally, close the current activity
        }
    }

    @Composable
    fun ProfileImagePicker(
        selectedImageUri: Uri?,
        onImageClick: () -> Unit
    ) {
        if (selectedImageUri == null) {
            // Default image if no photo is selected
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = "Profile Image",
                modifier = Modifier
                    .size(150.dp, 150.dp)
                    .clickable(onClick = onImageClick),
                contentScale = ContentScale.Crop
            )
        } else {
            // Show the selected image
            Image(
                painter = rememberAsyncImagePainter(model = selectedImageUri),
                contentDescription = "Selected Profile Image",
                modifier = Modifier
                    .size(150.dp, 150.dp)
                    .clickable(onClick = onImageClick),
                contentScale = ContentScale.Crop
            )
        }
    }

    //some validation
    private fun isValidInput(email: String, password: String, fullName: String, selectedImageUri: String): Boolean {
        // Check if the email is not empty
        if (email.isEmpty()) {
            Toast.makeText(this, "Email cannot be empty", Toast.LENGTH_SHORT).show()
            return false
        }

        // Check if the full name is not empty
        if (fullName.isEmpty()) {
            Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show()
            return false
        }

        // Check if the password is at least 8 characters long
        if (password.length < 8) {
            Toast.makeText(this, "Password must be at least 8 characters", Toast.LENGTH_SHORT).show()
            return false
        }

        // Check if an image has been selected
        if (selectedImageUri == null) {
            Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }
}
