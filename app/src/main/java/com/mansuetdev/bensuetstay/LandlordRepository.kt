package com.mansuetdev.bensuetstay

<<<<<<< HEAD
import android.app.Activity
import android.content.Context
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

/** landlords/{uid}. Created and deleted by admins only (see firestore.rules). */
object LandlordRepository {
    const val COLLECTION = "landlords"
    private const val HELPER_APP = "landlord-account-helper"

    private val collection get() = FirebaseFirestore.getInstance().collection(COLLECTION)

    /** Admin only: live list of every landlord, removed automatically when [activity] stops. */
    fun listenAll(
        activity: Activity,
        onChange: (List<LandlordAccount>) -> Unit,
        onError: (Exception) -> Unit
    ): ListenerRegistration = collection.addSnapshotListener(activity) { snapshot, error ->
        if (error != null) { onError(error); return@addSnapshotListener }
        if (snapshot != null) onChange(snapshot.documents.map { fromDoc(it) }.sortedBy { it.name.lowercase() })
    }

    /** Admin only: one-off read used by the listing form's landlord picker. */
    fun getAll(): Task<List<LandlordAccount>> = collection.get().continueWith { t ->
        if (!t.isSuccessful) throw t.exception ?: IllegalStateException("Couldn't load landlords")
        t.result.documents.map { fromDoc(it) }.sortedBy { it.name.lowercase() }
    }

    /**
     * Creates the landlord's Firebase Auth account and profile.
     *
     * createUserWithEmailAndPassword() signs the new user in, which would log the admin
     * out. So the account is created through a second FirebaseApp instance, leaving the
     * admin's session (and Firestore permissions) untouched.
     */
    fun createAccount(
        context: Context,
        name: String,
        email: String,
        phone: String,
        temporaryPassword: String
    ): Task<LandlordAccount> {
        val helperAuth = helperAuth(context)
        return helperAuth.createUserWithEmailAndPassword(email, temporaryPassword)
            .continueWithTask { created ->
                if (!created.isSuccessful) throw created.exception ?: IllegalStateException("Account creation failed")
                val user = created.result.user ?: throw IllegalStateException("Account creation failed")
                val landlord = LandlordAccount(
                    id = user.uid,
                    name = name,
                    email = email,
                    phone = phone,
                    verified = true,
                    mustChangePassword = true
                )
                collection.document(landlord.id).set(toMap(landlord)).continueWithTask { saved ->
                    if (saved.isSuccessful) {
                        helperAuth.signOut()
                        Tasks.forResult(landlord)
                    } else {
                        // Don't leave an Auth account behind with no profile
                        user.delete().continueWith<LandlordAccount> {
                            helperAuth.signOut()
                            throw saved.exception ?: IllegalStateException("Couldn't save landlord profile")
                        }
                    }
                }
            }
    }

    private fun helperAuth(context: Context): FirebaseAuth {
        val app = FirebaseApp.getApps(context).firstOrNull { it.name == HELPER_APP }
            ?: FirebaseApp.initializeApp(context, FirebaseApp.getInstance().options, HELPER_APP)
        return FirebaseAuth.getInstance(app)
    }

    /**
     * Admin only. Removes the profile (which blocks login) and unassigns the landlord's
     * listings in one batch. The Auth account itself can only be deleted from the
     * Firebase console or a Cloud Function.
     */
    fun delete(landlordId: String): Task<Void> {
        val db = FirebaseFirestore.getInstance()
        val batch = db.batch()
        ListingRepository.listingsForLandlord(landlordId).forEach { listing ->
            batch.update(
                db.collection(ListingRepository.COLLECTION).document(listing.id),
                mapOf(
                    "assignedLandlordId" to null,
                    "assignedLandlordName" to null,
                    "assignedLandlordPhone" to null
                )
            )
        }
        batch.delete(collection.document(landlordId))
        return batch.commit()
    }

    fun sendPasswordReset(email: String): Task<Void> =
        FirebaseAuth.getInstance().sendPasswordResetEmail(email)

    fun markPasswordChanged(uid: String): Task<Void> =
        collection.document(uid).update("mustChangePassword", false)

    fun updateProfileImage(uid: String, url: String?): Task<Void> =
        collection.document(uid).update("profileImageUri", url)

    fun fromDoc(doc: DocumentSnapshot) = LandlordAccount(
        id = doc.id,
        name = doc.getString("name") ?: "",
        email = doc.getString("email") ?: "",
        phone = doc.getString("phone") ?: "",
        verified = doc.getBoolean("verified") ?: false,
        mustChangePassword = doc.getBoolean("mustChangePassword") ?: false,
        profileImageUri = doc.getString("profileImageUri")?.takeIf { it.isNotBlank() }
    )

    private fun toMap(l: LandlordAccount): Map<String, Any?> = mapOf(
        "name" to l.name,
        "email" to l.email,
        "phone" to l.phone,
        "verified" to l.verified,
        "mustChangePassword" to l.mustChangePassword,
        "profileImageUri" to l.profileImageUri
    )
=======
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
>>>>>>> d231b4d53e6f750ae3c0daf4f61493ce26eaf809
}
