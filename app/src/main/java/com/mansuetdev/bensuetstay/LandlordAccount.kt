package com.mansuetdev.bensuetstay

// id is the landlord's Firebase Auth uid. Passwords live only in Firebase Authentication.
data class LandlordAccount(
    val id: String,
    val name: String,
    val email: String,
    val phone: String,
    val verified: Boolean,
    val mustChangePassword: Boolean = true,
    val profileImageUri: String? = null
)
