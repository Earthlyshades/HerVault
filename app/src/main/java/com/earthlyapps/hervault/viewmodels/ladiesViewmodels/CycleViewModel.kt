package com.earthlyapps.hervault.viewmodels.ladiesViewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import com.earthlyapps.hervault.models.CycleData
import com.earthlyapps.hervault.models.User
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class CycleViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val database = Firebase.database("https://hervault-620d7-default-rtdb.europe-west1.firebasedatabase.app/")

    private val _cycleData = MutableStateFlow<List<CycleData>>(emptyList())
    val cycleData: StateFlow<List<CycleData>> = _cycleData

    fun logCycle(startDate: String, cycleLength: Int, periodDays: List<String>) {
        val userId = auth.currentUser?.uid ?: return

        database.getReference("Users/$userId").get().addOnSuccessListener { snapshot ->
            val user = snapshot.getValue(User::class.java) ?: return@addOnSuccessListener

            val cycleData = CycleData(
                userId = userId,
                startDate = startDate,
                cycleLength = cycleLength,
                periodDays = periodDays
            )

            // 1. Save to woman's own cycles
            database.getReference("cycles/$userId").push()
                .setValue(cycleData)
                .addOnSuccessListener {
                    // 2. Share with partner if enabled
                    if (user.shareCycleData && user.linkedPartnerId != null) {
                        val partnerPath = "partnerCycles/${user.linkedPartnerId}/$userId"
                        database.getReference(partnerPath).push()
                            .setValue(cycleData)
                            .addOnSuccessListener {
                                Log.d("SHARING", "Cycle shared with partner")
                            }
                    }
                }
        }
    }

    fun loadCycleData() {
        auth.currentUser?.uid?.let { userId ->
            database.getReference("cycles/$userId").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    _cycleData.value = snapshot.children.mapNotNull {
                        it.getValue(CycleData::class.java)
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    Log.e("DB_ERROR", "Failed to load cycle data", error.toException())
                }
            })
        }
    }
}