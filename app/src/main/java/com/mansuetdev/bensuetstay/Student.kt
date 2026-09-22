package com.mansuetdev.bensuetstay

data class Student(
    val id: String,
    val fullName: String,
    val email: String,
    val phone: String,
    val address: String,
    val password: String,
    val fundingType: FundingType,
    val institution: String,
    val profileImageUri: String? = null
)