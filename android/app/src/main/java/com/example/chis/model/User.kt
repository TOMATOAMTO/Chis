package com.example.chis.model

data class User(
    val id: String = "user_001",
    val username: String = "Plant Lover",
    val email: String = "user@example.com",
    val avatarUrl: String = "",
    val deviceCount: Int = 1,
    val selectedCity: String = "Beijing"
)
