package com.mansuetdev.bensuetstay

import com.google.firebase.firestore.FirebaseFirestore

object AdminRepository {
    private val db = FirebaseFirestore.getInstance()
    private val collection = db.collection("admins")

    val admins = mutableListOf<AdminAccount>()
    var loggedInAdmin: AdminAccount? = null

    init {
        collection.addSnapshotListener { snapshot, _ ->
            if (snapshot != null && !snapshot.isEmpty) {
                val newList = mutableListOf<AdminAccount>()
                for (doc in snapshot.documents) {
                    val id = doc.id
                    val name = doc.getString("name") ?: ""
                    val email = doc.getString("email") ?: ""
                    val password = doc.getString("password") ?: ""
                    val profileImageUri = doc.getString("profileImageUri")

                    newList.add(AdminAccount(id, name, email, password, profileImageUri))
                }
                admins.clear()
                admins.addAll(newList)
            } else if (snapshot != null && snapshot.isEmpty) {
                admins.clear()
            }
        }
    }

    private fun saveToFirestore(admin: AdminAccount) {
        val data = mapOf(
            "name" to admin.name,
            "email" to admin.email,
            "password" to admin.password,
            "profileImageUri" to admin.profileImageUri
        )
        collection.document(admin.id).set(data)
    }

    fun login(email: String, password: String): AdminAccount? {
        val admin = admins.find { 
            it.email.equals(email, ignoreCase = true) && it.password == password 
        }
        loggedInAdmin = admin
        return admin
    }

    fun updateAdmin(admin: AdminAccount) {
        val index = admins.indexOfFirst { it.id == admin.id }
        if (index != -1) {
            admins[index] = admin
            saveToFirestore(admin)
        }
    }
}
