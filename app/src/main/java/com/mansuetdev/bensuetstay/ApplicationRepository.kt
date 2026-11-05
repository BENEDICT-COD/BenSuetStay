package com.mansuetdev.bensuetstay

import android.app.Activity
import com.google.android.gms.tasks.Task
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import java.util.Date

/**
 * Room applications (NSFAS confirmation or self-funded registration).
 * Queries are scoped to one student or one landlord; firestore.rules enforces the same.
 */
object ApplicationRepository {
    const val COLLECTION = "applications"
    // Transaction failure markers, turned into translated messages by AuthErrors
    const val ERR_ALREADY_APPROVED = "err_already_approved"
    const val ERR_ROOM_GONE = "err_room_gone"
    const val ERR_ROOM_FULL = "err_room_full"

    private val db get() = FirebaseFirestore.getInstance()
    private val collection get() = db.collection(COLLECTION)

    fun newId(): String = collection.document().id

    /**
     * Saves the application. Only the names of the student's documents are recorded; the files
     * themselves aren't uploaded (that needs Firebase Storage, i.e. the Blaze plan).
     */
    fun submit(application: StudentApplication, documentNames: List<String>): Task<Void> {
        val documents = documentNames.map { AppDocument(it, url = null) }
        val batch = db.batch()
        batch.set(collection.document(application.id), toMap(application.copy(documents = documents)))
        batch.set(
            NotificationCenter.newRef(),
            NotificationCenter.alert(
                application.landlordId, AlertType.APPLICATION_SUBMITTED,
                application.studentName, application.roomType, application.accommodationName
            )
        )
        return batch.commit()
    }

    /** True if the student already has a non-declined application for this listing. */
    fun hasActiveApplication(studentId: String, listingId: String): Task<Boolean> =
        collection.whereEqualTo("studentId", studentId).whereEqualTo("listingId", listingId).get()
            .continueWith { t ->
                if (!t.isSuccessful) throw t.exception ?: IllegalStateException("Couldn't check applications")
                t.result.documents.map { fromDoc(it) }.any { it.isActive }
            }

    fun listenForStudent(activity: Activity, studentId: String, onChange: (List<StudentApplication>) -> Unit, onError: (Exception) -> Unit) =
        listen(activity, collection.whereEqualTo("studentId", studentId), onChange, onError)

    fun listenForLandlord(activity: Activity, landlordId: String, onChange: (List<StudentApplication>) -> Unit, onError: (Exception) -> Unit) =
        listen(activity, collection.whereEqualTo("landlordId", landlordId), onChange, onError)

    private fun listen(
        activity: Activity,
        query: Query,
        onChange: (List<StudentApplication>) -> Unit,
        onError: (Exception) -> Unit
    ): ListenerRegistration = query.addSnapshotListener(activity) { snapshot, error ->
        if (error != null) { onError(error); return@addSnapshotListener }
        if (snapshot != null) onChange(snapshot.documents.map { fromDoc(it) }.sortedByDescending { it.createdAtMillis })
    }

    /** Live single application, removed automatically when [activity] stops. */
    fun listenToOne(
        activity: Activity,
        id: String,
        onChange: (StudentApplication?) -> Unit,
        onError: (Exception) -> Unit
    ): ListenerRegistration = collection.document(id).addSnapshotListener(activity) { doc, error ->
        if (error != null) { onError(error); return@addSnapshotListener }
        onChange(if (doc != null && doc.exists()) fromDoc(doc) else null)
    }

    fun markViewed(id: String): Task<Void> = collection.document(id).update("status", ApplicationStatus.VIEWED.name)

    /**
     * Landlord decision. Approving also takes one space off the matching room type, inside a
     * transaction so two approvals can't both take the last bed.
     */
    fun decide(application: StudentApplication, status: ApplicationStatus, feedback: String): Task<*> {
        val appRef = collection.document(application.id)
        if (status != ApplicationStatus.APPROVED) {
            val batch = db.batch()
            batch.update(appRef, mapOf("status" to status.name, "feedback" to feedback))
            batch.set(
                NotificationCenter.newRef(),
                NotificationCenter.alert(application.studentId, AlertType.APPLICATION_DECLINED, application.accommodationName)
            )
            return batch.commit()
        }
        val listingRef = db.collection(ListingRepository.COLLECTION).document(application.listingId)
        return db.runTransaction<Any?> { tx ->
            val current = tx.get(appRef)
            if (current.getString("status") == ApplicationStatus.APPROVED.name) {
                throw FirebaseFirestoreException(ERR_ALREADY_APPROVED, FirebaseFirestoreException.Code.ABORTED)
            }
            val listing = ListingRepository.fromDoc(tx.get(listingRef))
            val index = listing.rooms.indexOfFirst {
                it.roomType == application.roomType && it.fundingType == application.roomFundingType
            }
            if (index == -1) {
                throw FirebaseFirestoreException(
                    ERR_ROOM_GONE, FirebaseFirestoreException.Code.FAILED_PRECONDITION
                )
            }
            val room = listing.rooms[index]
            if (room.spacesAvailable <= 0) {
                throw FirebaseFirestoreException(
                    ERR_ROOM_FULL, FirebaseFirestoreException.Code.FAILED_PRECONDITION
                )
            }
            val rooms = listing.rooms.toMutableList()
            rooms[index] = room.copy(spacesAvailable = room.spacesAvailable - 1)
            tx.update(listingRef, "rooms", rooms.map { ListingRepository.roomToMap(it) })
            tx.update(appRef, mapOf("status" to status.name, "feedback" to feedback))
            tx.set(
                NotificationCenter.newRef(),
                NotificationCenter.alert(application.studentId, AlertType.APPLICATION_APPROVED, application.accommodationName)
            )
            null
        }
    }

    fun fromDoc(doc: DocumentSnapshot): StudentApplication {
        val documents = (doc.get("documents") as? List<*>).orEmpty().mapNotNull { item ->
            when (item) {
                is Map<*, *> -> AppDocument(item["name"] as? String ?: "Document", item["url"] as? String)
                is String -> AppDocument(item, null) // records saved before real uploads existed
                else -> null
            }
        }
        val createdAt = doc.getTimestamp("createdAt")?.toDate()?.time ?: 0L
        return StudentApplication(
            id = doc.id,
            studentId = doc.getString("studentId") ?: "",
            studentName = doc.getString("studentName") ?: "",
            studentEmail = doc.getString("studentEmail") ?: "",
            listingId = doc.getString("listingId") ?: "",
            accommodationName = doc.getString("accommodationName") ?: "",
            landlordId = doc.getString("landlordId") ?: "",
            roomType = doc.getString("roomType") ?: doc.getString("roomSelected") ?: "",
            applicationType = if (doc.getString("applicationType") == ApplicationType.SELF_FUNDED_REGISTRATION.name)
                ApplicationType.SELF_FUNDED_REGISTRATION else ApplicationType.NSFAS_CONFIRMATION,
            dateApplied = doc.getString("dateApplied") ?: "",
            createdAtMillis = createdAt,
            status = ApplicationStatus.entries.firstOrNull { it.name == doc.getString("status") }
                ?: ApplicationStatus.SUBMITTED,
            idNumber = doc.getString("idNumber"),
            studentNumber = doc.getString("studentNumber"),
            campus = doc.getString("campus"),
            nextOfKinName = doc.getString("nextOfKinName"),
            nextOfKinPhone = doc.getString("nextOfKinPhone"),
            documents = documents,
            feedback = doc.getString("feedback")
        )
    }

    private fun toMap(a: StudentApplication): Map<String, Any?> = mapOf(
        "studentId" to a.studentId,
        "studentName" to a.studentName,
        "studentEmail" to a.studentEmail,
        "listingId" to a.listingId,
        "accommodationName" to a.accommodationName,
        "landlordId" to a.landlordId,
        "roomType" to a.roomType,
        "applicationType" to a.applicationType.name,
        "dateApplied" to a.dateApplied,
        "createdAt" to Timestamp(Date(a.createdAtMillis)),
        "status" to a.status.name,
        "idNumber" to a.idNumber,
        "studentNumber" to a.studentNumber,
        "campus" to a.campus,
        "nextOfKinName" to a.nextOfKinName,
        "nextOfKinPhone" to a.nextOfKinPhone,
        "documents" to a.documents.map { mapOf("name" to it.name, "url" to it.url) },
        "feedback" to a.feedback
    )
}
