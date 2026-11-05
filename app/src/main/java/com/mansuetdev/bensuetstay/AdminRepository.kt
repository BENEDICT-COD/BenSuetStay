package com.mansuetdev.bensuetstay

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
        }
    }
}
