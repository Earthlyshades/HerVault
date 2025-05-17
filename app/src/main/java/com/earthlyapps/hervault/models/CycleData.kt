package com.earthlyapps.hervault.models

data class CycleData(
    val userId: String = "",
    val startDate: String = "",
    val cycleLength: Int = 28,
    val periodDays: List<String> = emptyList(),
    val symptoms: Map<String, Int> = emptyMap(),
    val timestamp: Long = System.currentTimeMillis()
)