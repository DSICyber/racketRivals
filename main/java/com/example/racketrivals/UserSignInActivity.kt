package com.example.racketrivals

import User
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentContainerView
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import io.github.jan.supabase.gotrue.gotrue
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.realtime.realtime
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URLEncoder

// UserSignInActivity.kt
class UserSignInActivity : AppCompatActivity() {

    private lateinit var sharedViewModel: SharedViewModel;
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.user_sign_in_main_screen)

        sharedViewModel = ViewModelProvider(this).get(SharedViewModel::class.java)

        val bottomNavView: BottomNavigationView = findViewById(R.id.bottomNavigationView)



        val client =SupabaseClientManager.client
        lifecycleScope.launch(Dispatchers.IO) {
            try{
                val user = client.gotrue.retrieveUserForCurrentSession(updateSession = true)





                val fileName = URLEncoder.encode(user.email, "UTF-8") + ".png"
                val bucket = client.storage["avatars"]
                val bytes = bucket.downloadAuthenticated(fileName)
                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                val userDetails= client.postgrest["profiles"].select(columns= Columns.list("avatar_url,full_name,elo")){
                   eq("avatar_url",fileName)
                }.decodeSingle<User>()
                withContext(Dispatchers.Main) {
                    val eloLabelTextView: TextView = findViewById(R.id.eloLabelTextView)
                    val usernameTextView: TextView = findViewById(R.id.usernameTextView)
                val imageView: ImageView = findViewById(R.id.profileImageView)
                    eloLabelTextView.text = userDetails.elo.toString()
                    usernameTextView.text = userDetails.full_name
                imageView.setImageBitmap(bitmap)}


            }catch (e:Exception){
                Log.e("ExceptionTag", "An error occurred: ${e.message}", e)

            }

            try {



                findViewById<FragmentContainerView>(R.id.nav_host_fragment).post {
                    val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
                    val navController = navHostFragment.navController

                    val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)

                    bottomNavigationView.setupWithNavController(navController)
                }

            } catch (e: Exception) {
                // Handle the exception
                Log.e("YourTag", "Error occurred: ${e.message}", e) // Log the error for debugging

            }
        }


    }




}




