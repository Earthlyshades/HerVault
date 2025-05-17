package com.earthlyapps.hervault.viewmodels.menViewmodels

import androidx.lifecycle.ViewModel
import com.earthlyapps.hervault.models.CycleData
import com.google.firebase.Firebase
import com.google.firebase.database.database
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow



class MenDashboardViewModel : ViewModel() {
    private val _partnerCycleData = MutableStateFlow<CycleData?>(null)
    val partnerCycleData: StateFlow<CycleData?> = _partnerCycleData
    private val database = Firebase.database

    private val _tips = MutableStateFlow<List<Tip>>(emptyList())
    val tips: StateFlow<List<Tip>> = _tips

    fun loadPartnerData(partnerId: String) {
        database.getReference("cycles/$partnerId").get()
            .addOnSuccessListener { snapshot ->
                _partnerCycleData.value = snapshot.children.lastOrNull()
                    ?.getValue(CycleData::class.java)
            }
    }

    fun loadTips() {
        _tips.value = listOf(
            Tip("Comfort Tips", "Offer warm compresses for cramps"),
            Tip("Emotional Support", "Be patient with mood swings"),
            Tip("Nutrition", "Suggest magnesium-rich foods")
        )
    }
}

data class Tip(val title: String, val content: String)