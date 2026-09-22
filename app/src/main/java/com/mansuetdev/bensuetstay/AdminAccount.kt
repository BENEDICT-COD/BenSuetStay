package com.mansuetdev.bensuetstay

data class AdminAccount(
    val id: String,
    val name: String,
    val email: String,
    val password: String,
    val profileImageUri: String? = null
)