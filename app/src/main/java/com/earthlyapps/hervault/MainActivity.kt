package com.earthlyapps.hervault

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import com.earthlyapps.hervault.navigation.AppNavHost
import com.earthlyapps.hervault.ui.theme.HerVaultTheme
import com.facebook.stetho.Stetho
import com.google.firebase.database.ktx.BuildConfig
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (BuildConfig.DEBUG) {
            Stetho.initializeWithDefaults(this)
            enableFirebaseDebugging()
        }
        enableEdgeToEdge()
        setContent {
            HerVaultTheme {
                AppNavHost()
            }
        }
    }

    private fun enableFirebaseDebugging() {
        try {
            Firebase.database.setLogLevel(com.google.firebase.database.Logger.Level.DEBUG)
            Log.d("FirebaseDebug", "Database logging enabled")
        } catch (e: Exception) {
            Log.w("FirebaseDebug", "Couldn't enable detailed logging", e)
        }
    }
}