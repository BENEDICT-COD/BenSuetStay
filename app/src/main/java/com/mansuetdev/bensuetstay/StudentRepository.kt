package com.mansuetdev.bensuetstay

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

/** students/{uid}. Each student can only read and edit their own document (see firestore.rules). */
object StudentRepository {
    const val COLLECTION = "students"

    private val collection get() = FirebaseFirestore.getInstance().collection(COLLECTION)

    fun createProfile(student: Student): Task<Void> = collection.document(student.id).set(
        mapOf(
            "uid" to student.id,
            "fullNames" to student.fullName,
            "email" to student.email,
            "address" to student.address,
            "institution" to student.institution,
            "fundingType" to student.fundingType.name,
            "profileImageUri" to student.profileImageUri
        )
    )

    fun updateProfile(uid: String, fullName: String, address: String, institution: String): Task<Void> =
        collection.document(uid).update(
            mapOf("fullNames" to fullName, "address" to address, "institution" to institution)
        )

    /** Account deletion (Settings → Privacy). Allowed for the student themself by firestore.rules. */
    fun deleteProfile(uid: String): Task<Void> = collection.document(uid).delete()

    fun updateProfileImage(uid: String, url: String?): Task<Void> =
        collection.document(uid).update("profileImageUri", url)

    fun fromDoc(doc: DocumentSnapshot) = Student(
        id = doc.id,
        fullName = doc.getString("fullNames") ?: doc.getString("fullName") ?: "",
        email = doc.getString("email") ?: "",
        address = doc.getString("address") ?: "",
        fundingType = FundingType.fromStorage(doc.getString("fundingType")),
        institution = doc.getString("institution") ?: "",
        profileImageUri = doc.getString("profileImageUri")?.takeIf { it.isNotBlank() }
    )
}
