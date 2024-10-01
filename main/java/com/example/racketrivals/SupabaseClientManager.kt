package com.example.racketrivals

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.GoTrue
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.storage.Storage

//api to connect to supabase
object SupabaseClientManager {
    val client by lazy{ createSupabaseClient (
        supabaseUrl = "https://fgqlragaiswmyvookphi.supabase.co",
        supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImZncWxyYWdhaXN3bXl2b29rcGhpIiwicm9sZSI6ImFub24iLCJpYXQiOjE2ODIxMDE0NzIsImV4cCI6MTk5NzY3NzQ3Mn0.9113rnXHIQSKbwvFFtnx2KTvW_jPoxDMpg_Nk4ZyquE"
    ) {
        install(Storage)
        install(Postgrest)
        install(GoTrue)
        install(Realtime)

    }
}
}