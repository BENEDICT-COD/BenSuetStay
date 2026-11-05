package com.mansuetdev.bensuetstay

<<<<<<< HEAD
// Passwords live only in Firebase Authentication, never in Firestore.
=======
>>>>>>> d231b4d53e6f750ae3c0daf4f61493ce26eaf809
data class Student(
    val id: String,
    val fullName: String,
    val email: String,
<<<<<<< HEAD
    val address: String,
    val fundingType: FundingType,
    val institution: String,
    val profileImageUri: String? = null
)
=======
    val phone: String,
    val address: String,
    val password: String,
    val fundingType: FundingType,
    val institution: String,
    val profileImageUri: String? = null
)
>>>>>>> d231b4d53e6f750ae3c0daf4f61493ce26eaf809
