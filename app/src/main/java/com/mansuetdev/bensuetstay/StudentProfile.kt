package com.mansuetdev.bensuetstay

data class StudentProfile(
    val uid: String = "",
    val fullNames: String = "",
    val address: String = "",
    val institution: String = "",
    val email: String = "",
    val fundingType: String = "", // stored as "NSFAS" or "SELF_FUNDED"
    val profileImageUri: String = ""
)