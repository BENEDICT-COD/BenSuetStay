package com.mansuetdev.bensuetstay

// Passwords live only in Firebase Authentication, never in Firestore.
data class Student(
    val id: String,
    val fullName: String,
    val email: String,
    val address: String,
    val fundingType: FundingType,
    val institution: String,
    val profileImageUri: String? = null
)
