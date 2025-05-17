package com.earthlyapps.hervault

import android.app.Application
import android.util.Log
import com.earthlyapps.hervault.models.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.Logger
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.BuildConfig
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class HerVaultApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        val database = Firebase.database("https://hervault-620d7-default-rtdb.europe-west1.firebasedatabase.app/").apply {
            setPersistenceEnabled(true)

            if (BuildConfig.DEBUG) {
                try {
                    setLogLevel(Logger.Level.DEBUG)
                } catch (e: Exception) {
                    Log.w("Firebase", "Detailed logging unavailable: ${e.message}")
                }

                reference.child(".info/connected")
                    .addValueEventListener(connectionListener)
            }
        }

        database.reference.child("Users").get()
            .addOnSuccessListener { snapshot ->
                Log.d("DB_DUMP", "All users:")
                snapshot.children.forEach {
                    Log.d("DB_DUMP", "${it.key}: ${it.getValue(User::class.java)}")
                }
            }
    }

    private val connectionListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            Log.d("FirebaseDB", "Connected: ${snapshot.getValue(Boolean::class.java)}")
        }

        override fun onCancelled(error: DatabaseError) {
            Log.e("FirebaseDB", "Connection error: ${error.message}")
        }
    }
}