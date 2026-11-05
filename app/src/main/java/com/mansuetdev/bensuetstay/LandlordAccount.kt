package com.mansuetdev.bensuetstay

<<<<<<< HEAD
// id is the landlord's Firebase Auth uid. Passwords live only in Firebase Authentication.
=======
>>>>>>> d231b4d53e6f750ae3c0daf4f61493ce26eaf809
data class LandlordAccount(
    val id: String,
    val name: String,
    val email: String,
    val phone: String,
    val verified: Boolean,
<<<<<<< HEAD
    val mustChangePassword: Boolean = true,
    val profileImageUri: String? = null
)
=======
    val listingsCount: Int,
    val password: String = "password123",
    val mustChangePassword: Boolean = true,
    val profileImageUri: String? = null
)
>>>>>>> d231b4d53e6f750ae3c0daf4f61493ce26eaf809
