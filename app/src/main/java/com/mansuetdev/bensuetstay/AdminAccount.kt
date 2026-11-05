package com.mansuetdev.bensuetstay

<<<<<<< HEAD
// id is the admin's Firebase Auth uid. Admin documents are created in the Firebase console.
=======
>>>>>>> d231b4d53e6f750ae3c0daf4f61493ce26eaf809
data class AdminAccount(
    val id: String,
    val name: String,
    val email: String,
<<<<<<< HEAD
    val profileImageUri: String? = null
)
=======
    val password: String,
    val profileImageUri: String? = null
)
>>>>>>> d231b4d53e6f750ae3c0daf4f61493ce26eaf809
