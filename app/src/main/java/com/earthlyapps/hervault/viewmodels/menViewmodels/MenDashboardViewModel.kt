package com.earthlyapps.hervault.viewmodels.menViewmodels

import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.earthlyapps.hervault.models.CycleData
import com.earthlyapps.hervault.models.LadiesNeeds
import com.earthlyapps.hervault.models.Symptom
import com.earthlyapps.hervault.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class MenDashboardViewModel : ViewModel() {
    // Firebase KTX instances
    private val auth: FirebaseAuth = Firebase.auth
    private val database = Firebase.database("https://hervault-620d7-default-rtdb.europe-west1.firebasedatabase.app/")

    // State flows
    private val _partnerSymptoms = MutableStateFlow<List<Symptom>>(emptyList())
    val partnerSymptoms: StateFlow<List<Symptom>> = _partnerSymptoms.asStateFlow()

    private val _partnerCycles = MutableStateFlow<List<CycleData>>(emptyList())
    val partnerCycles: StateFlow<List<CycleData>> = _partnerCycles.asStateFlow()

    private val _partnerInfo = MutableStateFlow<User?>(null)
    val partnerInfo: StateFlow<User?> = _partnerInfo.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _ladiesNeeds = MutableStateFlow<LadiesNeeds?>(null)
    val ladiesNeeds: StateFlow<LadiesNeeds?> = _ladiesNeeds.asStateFlow()

    init {
        loadPartnerData()
        loadLadiesNeeds(userId = auth.currentUser?.uid ?: "")
    }

    internal fun loadPartnerData() {
        viewModelScope.launch {
            try {
                _loading.value = true
                _error.value = null

                val myUserId = auth.currentUser?.uid ?: run {
                    _partnerInfo.value = null
                    return@launch
                }

                // Get partner ID
                val partnerId = database.getReference("Users/$myUserId/linkedPartnerId")
                    .get().await()
                    .getValue<String>() ?: run {
                    _partnerInfo.value = null
                    return@launch
                }

                // Get partner details
                val partner = database.getReference("Users/$partnerId")
                    .get().await()
                    .getValue<User>()
                _partnerInfo.value = partner

                // Load shared data if enabled
                partner?.let {
                    if (it.shareSymptoms) loadSymptoms(partnerId, myUserId)
                    if (it.shareCycleData) loadCycles(partnerId, myUserId)
                }
            } catch (e: Exception) {
                _error.value = "Failed to load partner data: ${e.message}"
                Log.e("MenViewModel", "Error loading partner data", e)
            } finally {
                _loading.value = false
            }
        }
    }

    private fun loadSymptoms(partnerId: String, myUserId: String) {
        database.getReference("partnerSymptoms/$partnerId")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    _partnerSymptoms.value = snapshot.children.mapNotNull {
                        it.getValue<Symptom>()
                    }.sortedByDescending { it.timestamp }
                }

                override fun onCancelled(error: DatabaseError) {
                    _error.value = "Failed to load symptoms: ${error.message}"
                    Log.e("MenViewModel", "Symptoms listener cancelled", error.toException())
                }
            })
    }

    private fun loadCycles(partnerId: String, myUserId: String) {
        database.getReference("partnerCycles/$partnerId")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    _partnerCycles.value = snapshot.children.mapNotNull {
                        it.getValue<CycleData>()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    _error.value = "Failed to load cycles: ${error.message}"
                    Log.e("MenViewModel", "Cycles listener cancelled", error.toException())
                }
            })
    }

    private fun loadLadiesNeeds(userId: String){
        database.getReference("LadiesNeeds/${userId}")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    _ladiesNeeds.value = snapshot.getValue<LadiesNeeds>()
                }
                override fun onCancelled(error: DatabaseError) {
                    _error.value = "Failed to load ladies needs: ${error.message}"
                    Log.e("MenViewModel", "Ladies needs listener cancelled", error.toException())
                }
            })
    }

    fun refreshData() {
        loadPartnerData()
    }
}