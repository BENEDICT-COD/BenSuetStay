package com.mansuetdev.bensuetstay

import com.google.firebase.firestore.FirebaseFirestore

object LandlordRepository {
    private val db = FirebaseFirestore.getInstance()
    private val collection = db.collection("landlords")

    val landlords = mutableListOf<LandlordAccount>()
    var loggedInLandlord: LandlordAccount? = null

    init {
        collection.addSnapshotListener { snapshot, _ ->
            if (snapshot != null && !snapshot.isEmpty) {
                val newList = mutableListOf<LandlordAccount>()
                for (doc in snapshot.documents) {
                    val id = doc.id
                    val name = doc.getString("name") ?: ""
                    val email = doc.getString("email") ?: ""
                    val phone = doc.getString("phone") ?: ""
                    val verified = doc.getBoolean("verified") ?: false
                    val listingsCount = doc.getLong("listingsCount")?.toInt() ?: 0

                    val password = doc.getString("password") ?: "password123"
                    val mustChangePassword = doc.getBoolean("mustChangePassword") ?: true
                    val profileImageUri = doc.getString("profileImageUri")

                    newList.add(LandlordAccount(id, name, email, phone, verified, listingsCount, password, mustChangePassword, profileImageUri))
                }
                landlords.clear()
                landlords.addAll(newList)
            } else if (snapshot != null && snapshot.isEmpty) {
                landlords.clear()
            }
        }
    }

    private fun saveToFirestore(landlord: LandlordAccount) {
        val data = mapOf(
            "name" to landlord.name,
            "email" to landlord.email,
            "phone" to landlord.phone,
            "verified" to landlord.verified,
            "listingsCount" to landlord.listingsCount,
            "password" to landlord.password,
            "mustChangePassword" to landlord.mustChangePassword,
            "profileImageUri" to landlord.profileImageUri
        )
        collection.document(landlord.id).set(data)
    }

    fun findById(id: String): LandlordAccount? = landlords.find { it.id == id }

    fun login(emailOrPhone: String, password: String): LandlordAccount? {
        val landlord = landlords.find { 
            (it.email.equals(emailOrPhone, ignoreCase = true) || it.phone == emailOrPhone) && it.password == password 
        }
        loggedInLandlord = landlord
        return landlord
    }

    fun updateLandlord(landlord: LandlordAccount) {
        val index = landlords.indexOfFirst { it.id == landlord.id }
        if (index != -1) {
            landlords[index] = landlord
            saveToFirestore(landlord)
        }
    }

    fun addLandlord(landlord: LandlordAccount) {
        val index = landlords.indexOfFirst { it.id == landlord.id }
        if (index == -1) landlords.add(landlord) else landlords[index] = landlord
        saveToFirestore(landlord)
    }

    fun resetPassword(id: String) {
        val landlord = findById(id) ?: return
        val updated = landlord.copy(password = "Stay@2025", mustChangePassword = true)
        updateLandlord(updated)
    }

    fun deleteLandlord(id: String) {
        landlords.removeAll { it.id == id }
        collection.document(id).delete()
    }
}
