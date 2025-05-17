package com.earthlyapps.hervault.models

data class Symptom(
    val userId: String = "",
    val date: String = "",
    val type: SymptomType = SymptomType.CRAMPS,
    val intensity: Int = 1,
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis()
) {
    constructor() : this("", "", SymptomType.CRAMPS, 1, "", 0)
}