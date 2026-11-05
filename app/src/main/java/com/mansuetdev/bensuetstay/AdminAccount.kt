package com.mansuetdev.bensuetstay

// id is the admin's Firebase Auth uid. Admin documents are created in the Firebase console.
data class AdminAccount(
    val id: String,
    val name: String,
    val email: String,
    val profileImageUri: String? = null
)
