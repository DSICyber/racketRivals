package com.example.racketrivals
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.core.content.ContextCompat
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.lifecycleScope
import io.github.jan.supabase.annotations.SupabaseInternal
import io.github.jan.supabase.gotrue.gotrue
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.gotrue.user.UserSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
class Global {
    companion object {
         var sessionKey: UserSession? = null
    }
}

class LogInScreen : ComponentActivity() {
    private fun showLoginError() {
        Toast.makeText(this, "These are not valid credentials", Toast.LENGTH_LONG).show()
    }

    private fun attemptLogin() {
        val emailEditText = findViewById<EditText>(R.id.editTextEmail)
        val passwordEditText = findViewById<EditText>(R.id.editTextPassword)
        val userEmail = emailEditText.text.toString()
        val userPassword = passwordEditText.text.toString()

        if (userEmail.isBlank() || userPassword.isBlank()) {
            showLoginError()
            return
        }


    }
    @OptIn(SupabaseInternal::class, DelicateCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_screen)

        val client=SupabaseClientManager.client


        val loginButton = findViewById<Button>(R.id.buttonSignIn)
        val createAccountButton = findViewById<Button>(R.id.buttonCreateAccount)
        createAccountButton.setOnClickListener {
            val intent = Intent(this@LogInScreen, AccountCreationActivity::class.java)
            startActivity(intent)
        }

        loginButton.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    attemptLogin()
                    val emailEditText = findViewById<EditText>(R.id.editTextEmail)
                    val passwordEditText = findViewById<EditText>(R.id.editTextPassword)
                    val userEmail = emailEditText.text.toString()
                    val userPassword = passwordEditText.text.toString()
                    val response =client.gotrue.loginWith(Email) {
                        email=userEmail
                        password = userPassword
                         Global.sessionKey = client.gotrue.currentSessionOrNull()
                    }
                    GlobalScope.launch {
                        startActivity(Intent(this@LogInScreen, UserSignInActivity::class.java))
                        finish()
                    }
                    withContext(Dispatchers.Main) {
                        // Process the response on the main thread
                        Log.e("DatabaseResponse",response.toString())

                    }

                } catch (e: Exception) {
                    try {
                        CoroutineScope(Dispatchers.Main).launch {
                        val invisibleTextView = findViewById<TextView>(R.id.invisibleTextView)
                        invisibleTextView.visibility = TextView.VISIBLE
                        invisibleTextView.text = "Invalid Credentials, Try Again"
                        invisibleTextView.setTextColor(android.graphics.Color.RED)}
                    } catch (e: Exception) {
                        // Handle the exception here
                        Log.e("MainActivity", "Error updating TextView: ${e.message}")
                        // You might also want to display an error message to the user or take other corrective actions
                    }


                        // Handle exception on the main thread
                        Log.e("DatabaseError", "Error: ${e.message}")
                    }
                }

            }



        }}







