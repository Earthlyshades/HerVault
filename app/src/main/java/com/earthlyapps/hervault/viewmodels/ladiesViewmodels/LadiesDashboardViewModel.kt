package com.earthlyapps.hervault.viewmodels.ladiesViewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import com.earthlyapps.hervault.models.Symptom
import com.earthlyapps.hervault.models.SymptomType
import com.earthlyapps.hervault.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class LadiesDashboardViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val database = Firebase.database("https://hervault-620d7-default-rtdb.europe-west1.firebasedatabase.app/")

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    private val _symptoms = MutableStateFlow<List<Symptom>>(emptyList())
    val symptoms: StateFlow<List<Symptom>> = _symptoms

    private val _menData = MutableStateFlow<User?>(null) // Renamed from _partnerData
    val menData: StateFlow<User?> = _menData

    init {
        loadUserData()
        loadSymptoms()
    }

    private fun loadUserData() {
        auth.currentUser?.uid?.let { userId ->
            database.getReference("Users/$userId").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    _currentUser.value = snapshot.getValue(User::class.java)
                    loadMenData()
                }
                override fun onCancelled(error: DatabaseError) {
                    Log.e("DB_ERROR", "Failed to load user", error.toException())
                }
            })
        }
    }

    private fun loadMenData() { // Renamed from loadPartnerData
        currentUser.value?.linkedPartnerId?.let { menId ->
            database.getReference("Users/$menId").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    _menData.value = snapshot.getValue(User::class.java)
                }
                override fun onCancelled(error: DatabaseError) {
                    Log.e("DB_ERROR", "Failed to load men data", error.toException())
                }
            })
        }
    }

    fun logSymptom(date: String, type: SymptomType, intensity: Int) {
        val userId = auth.currentUser?.uid ?: run {
            Log.e("LOG_SYMPTOM", "No authenticated user")
            return
        }

        // Get current user data first
        database.reference.child("Users").child(userId).get()
            .addOnSuccessListener { userSnapshot ->
                val user = userSnapshot.getValue(User::class.java) ?: run {
                    Log.e("LOG_SYMPTOM", "User data not found")
                    return@addOnSuccessListener
                }

                val symptom = Symptom(
                    userId = userId,
                    date = date,
                    type = type,
                    intensity = intensity,
                    timestamp = System.currentTimeMillis()
                )

                // Write to owner's symptoms
                database.reference.child("symptoms").child(userId).push()
                    .setValue(symptom)
                    .addOnSuccessListener {
                        _symptoms.value = _symptoms.value + symptom

                        // Share with partner if enabled
                        if (user.shareSymptoms && user.linkedPartnerId != null) {
                            database.reference.child("partnerSymptoms")
                                .child(user.linkedPartnerId)
                                .child(userId)
                                .push()
                                .setValue(symptom)
                                .addOnFailureListener { e ->
                                    Log.e("SHARE_SYMPTOM", "Failed to share with partner", e)
                                }
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e("LOG_SYMPTOM", "Failed to log symptom", e)
                    }
            }
            .addOnFailureListener { e ->
                Log.e("LOG_SYMPTOM", "Failed to fetch user data", e)
            }
    }



    fun loadSymptoms() {
        val userId = auth.currentUser?.uid ?: return

        database.reference.child("symptoms").child(userId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val loadedSymptoms = snapshot.children.mapNotNull {
                        it.getValue(Symptom::class.java)
                    }
                    _symptoms.value = loadedSymptoms
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("LOAD_SYMPTOMS", "Failed to load symptoms", error.toException())
                }
            })
    }

    fun updateSharingPreferences(shareCycle: Boolean, shareSymptoms: Boolean) {
        auth.currentUser?.uid?.let { userId ->
            val updates = mapOf(
                "Users/$userId/shareCycleData" to shareCycle,
                "Users/$userId/shareSymptoms" to shareSymptoms
            )
            database.reference.updateChildren(updates)
        }
    }
}