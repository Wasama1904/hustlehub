package com.wasama.hustlehub.data.model

data class UserProfile(
    val userId: String = "",
    val email: String = "",
    val hourlyRate: Double = 0.0,
    val currency: String = "ZAR",
    val xp: Int = 0,
    val streak: Int = 0,
    val freezeTokens: Int = 1,
    val badges: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis()
)
