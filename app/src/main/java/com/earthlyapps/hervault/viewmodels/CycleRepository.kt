package com.earthlyapps.hervault.viewmodels

import com.earthlyapps.hervault.models.CycleData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class CycleRepository {
    private val auth = FirebaseAuth.getInstance()
    private val database: FirebaseDatabase = Firebase.database
    private val cycleRef = database.getReference("cycles")

    fun logCycleData(
        startDate: String,
        cycleLength: Int,
        periodDays: List<String>,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val userId = auth.currentUser?.uid ?: run {
            onFailure(Exception("User not authenticated"))
            return
        }

        val cycleData = hashMapOf(
            "startDate" to startDate,
            "cycleLength" to cycleLength,
            "periodDays" to periodDays,
            "timestamp" to System.currentTimeMillis()
        )

        cycleRef.child(userId).push().setValue(cycleData)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onFailure(e) }
    }

    fun getCycleData(
        onSuccess: (List<CycleData>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val userId = auth.currentUser?.uid ?: run {
            onFailure(Exception("User not authenticated"))
            return
        }

        cycleRef.child(userId).get()
            .addOnSuccessListener { snapshot ->
                val cycles = snapshot.children.mapNotNull { child ->
                    child.getValue(CycleData::class.java)
                }
                onSuccess(cycles)
            }
            .addOnFailureListener { e -> onFailure(e) }
    }
}