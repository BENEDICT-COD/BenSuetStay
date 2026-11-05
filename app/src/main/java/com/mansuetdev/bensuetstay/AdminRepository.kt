package com.mansuetdev.bensuetstay

<<<<<<< HEAD
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.AggregateQuerySnapshot
import com.google.firebase.firestore.AggregateSource
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

/**
 * admins/{uid}. Admin documents are created by hand in the Firebase console
 * (see README "First-time Firebase setup"); the app never creates admins.
 */
object AdminRepository {
    const val COLLECTION = "admins"

    private val db get() = FirebaseFirestore.getInstance()

    fun fromDoc(doc: DocumentSnapshot) = AdminAccount(
        id = doc.id,
        name = doc.getString("name") ?: "Admin",
        email = doc.getString("email") ?: "",
        profileImageUri = doc.getString("profileImageUri")?.takeIf { it.isNotBlank() }
    )

    fun updateProfileImage(uid: String, url: String?): Task<Void> =
        db.collection(COLLECTION).document(uid).update("profileImageUri", url)

    /** Platform totals using Firestore count() queries, so no documents are downloaded. */
    fun loadStats(): Task<AdminStats> {
        val queries = listOf(
            db.collection(ListingRepository.COLLECTION).count(),
            db.collection(LandlordRepository.COLLECTION).count(),
            db.collection(LandlordRepository.COLLECTION).whereEqualTo("verified", false).count(),
            db.collection(StudentRepository.COLLECTION).count(),
            db.collection(ApplicationRepository.COLLECTION).count(),
            db.collection(BookingRepository.COLLECTION).count()
        )
        val tasks = queries.map { it.get(AggregateSource.SERVER) }
        return Tasks.whenAllSuccess<AggregateQuerySnapshot>(tasks).continueWith { t ->
            if (!t.isSuccessful) throw t.exception ?: IllegalStateException("Couldn't load stats")
            val c = t.result.map { it.count.toInt() }
            AdminStats(
                totalListings = c[0],
                totalLandlords = c[1],
                pendingApprovals = c[2],
                totalStudents = c[3],
                totalApplications = c[4],
                totalBookings = c[5]
            )
=======
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
>>>>>>> d231b4d53e6f750ae3c0daf4f61493ce26eaf809
        }
    }
}
