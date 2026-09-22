package com.mansuetdev.bensuetstay

data class LandlordAccount(
    val id: String,
    val name: String,
    val email: String,
    val phone: String,
    val verified: Boolean,
    val listingsCount: Int,
    val password: String = "password123",
    val mustChangePassword: Boolean = true,
    val profileImageUri: String? = null
)