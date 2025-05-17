package com.earthlyapps.hervault.models

data class User(
    var uid: String = "",
    var name: String = "",
    var email: String = "",
    var role: String = "",
    var partnerCode: String = "",
    var linkedPartnerId: String = "",
    val shareCycleData: Boolean = true,
    val shareSymptoms: Boolean = true,
    val lastUpdated: Long = System.currentTimeMillis()
)
