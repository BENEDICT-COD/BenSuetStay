package com.mansuetdev.bensuetstay

<<<<<<< HEAD
import android.app.Activity
import com.google.android.gms.tasks.Task
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import java.util.Date

/**
 * Viewing bookings. Every query is scoped to one student or one landlord so a phone only
 * ever downloads bookings that belong to its user; firestore.rules enforces the same thing.
 */
object BookingRepository {
    const val COLLECTION = "bookings"

    private val collection get() = FirebaseFirestore.getInstance().collection(COLLECTION)

    fun newId(): String = collection.document().id

    /** Saves the booking and alerts the landlord, atomically. */
    fun create(booking: Booking): Task<Void> {
        val batch = FirebaseFirestore.getInstance().batch()
        batch.set(collection.document(booking.id), toMap(booking))
        batch.set(
            NotificationCenter.newRef(),
            NotificationCenter.alert(
                booking.landlordId, AlertType.BOOKING_REQUESTED,
                booking.studentName, booking.accommodationName, booking.requestedDateTime
            )
        )
        return batch.commit()
    }

    /** Live bookings for a student; the listener is removed automatically when [activity] stops. */
    fun listenForStudent(activity: Activity, studentId: String, onChange: (List<Booking>) -> Unit, onError: (Exception) -> Unit) =
        listen(activity, collection.whereEqualTo("studentId", studentId), onChange, onError)

    fun listenForLandlord(activity: Activity, landlordId: String, onChange: (List<Booking>) -> Unit, onError: (Exception) -> Unit) =
        listen(activity, collection.whereEqualTo("landlordId", landlordId), onChange, onError)

    private fun listen(
        activity: Activity,
        query: Query,
        onChange: (List<Booking>) -> Unit,
        onError: (Exception) -> Unit
    ): ListenerRegistration = query.addSnapshotListener(activity) { snapshot, error ->
        if (error != null) { onError(error); return@addSnapshotListener }
        if (snapshot != null) onChange(snapshot.documents.map { fromDoc(it) }.sortedBy { it.requestedAtMillis })
    }

    /** Landlord confirms or declines; the student is alerted in the same atomic write. */
    fun respond(booking: Booking, status: BookingStatus, rejectionReason: String? = null, suggestedTime: String? = null): Task<Void> {
        val batch = FirebaseFirestore.getInstance().batch()
        batch.update(
            collection.document(booking.id),
            mapOf("status" to status.name, "rejectionReason" to rejectionReason, "suggestedTime" to suggestedTime)
        )
        val alert = if (status == BookingStatus.CONFIRMED) {
            NotificationCenter.alert(booking.studentId, AlertType.BOOKING_CONFIRMED, booking.accommodationName, booking.requestedDateTime)
        } else {
            NotificationCenter.alert(booking.studentId, AlertType.BOOKING_DECLINED, booking.accommodationName)
        }
        batch.set(NotificationCenter.newRef(), alert)
        return batch.commit()
    }

    fun fromDoc(doc: DocumentSnapshot) = Booking(
        id = doc.id,
        studentId = doc.getString("studentId") ?: "",
        studentName = doc.getString("studentName") ?: "",
        listingId = doc.getString("listingId") ?: "",
        accommodationName = doc.getString("accommodationName") ?: "",
        landlordId = doc.getString("landlordId") ?: "",
        requestedDateTime = doc.getString("requestedDateTime") ?: "",
        requestedAtMillis = doc.getTimestamp("requestedAt")?.toDate()?.time ?: 0L,
        status = BookingStatus.entries.firstOrNull { it.name == doc.getString("status") } ?: BookingStatus.PENDING,
        rejectionReason = doc.getString("rejectionReason"),
        suggestedTime = doc.getString("suggestedTime")
    )

    private fun toMap(b: Booking): Map<String, Any?> = mapOf(
        "studentId" to b.studentId,
        "studentName" to b.studentName,
        "listingId" to b.listingId,
        "accommodationName" to b.accommodationName,
        "landlordId" to b.landlordId,
        "requestedDateTime" to b.requestedDateTime,
        "requestedAt" to Timestamp(Date(b.requestedAtMillis)),
        "status" to b.status.name,
        "rejectionReason" to b.rejectionReason,
        "suggestedTime" to b.suggestedTime,
        "createdAt" to Timestamp.now()
    )
=======
import com.google.firebase.firestore.FirebaseFirestore

object BookingRepository {
    private val db = FirebaseFirestore.getInstance()
    private val collection = db.collection("bookings")

    val bookings = mutableListOf<Booking>()

    init {
        collection.addSnapshotListener { snapshot, _ ->
            if (snapshot != null && !snapshot.isEmpty) {
                val newList = mutableListOf<Booking>()
                for (doc in snapshot.documents) {
                    val id = doc.id
                    val studentName = doc.getString("studentName") ?: ""
                    val accommodationName = doc.getString("accommodationName") ?: ""
                    val requestedDateTime = doc.getString("requestedDateTime") ?: ""
                    val statusStr = doc.getString("status") ?: "PENDING"
                    val status = try { BookingStatus.valueOf(statusStr) } catch(e: Exception) { BookingStatus.PENDING }
                    val rejectionReason = doc.getString("rejectionReason")
                    val suggestedTime = doc.getString("suggestedTime")

                    newList.add(Booking(id, studentName, accommodationName, requestedDateTime, status, rejectionReason, suggestedTime))
                }
                bookings.clear()
                bookings.addAll(newList)
                notifyListeners()
            } else if (snapshot != null && snapshot.isEmpty) {
                bookings.clear()
                notifyListeners()
            }
        }
    }

    private val listeners = mutableListOf<() -> Unit>()
    fun addBookingListener(l: () -> Unit) { listeners.add(l) }
    fun removeBookingListener(l: () -> Unit) { listeners.remove(l) }
    private fun notifyListeners() { listeners.forEach { it() } }

    private fun saveToFirestore(booking: Booking) {
        val data = mapOf(
            "studentName" to booking.studentName,
            "accommodationName" to booking.accommodationName,
            "requestedDateTime" to booking.requestedDateTime,
            "status" to booking.status.name,
            "rejectionReason" to booking.rejectionReason,
            "suggestedTime" to booking.suggestedTime
        )
        collection.document(booking.id).set(data)
    }

    fun getBookingsForStudent(studentName: String): List<Booking> {
        return bookings.filter { it.studentName == studentName }
    }

    fun addBooking(booking: Booking) {
        val index = bookings.indexOfFirst { it.id == booking.id }
        if (index == -1) bookings.add(booking) else bookings[index] = booking
        saveToFirestore(booking)
    }

    fun updateStatus(id: String, status: BookingStatus, rejectionReason: String? = null, suggestedTime: String? = null) {
        val index = bookings.indexOfFirst { it.id == id }
        if (index != -1) {
            bookings[index].status = status
            bookings[index].rejectionReason = rejectionReason
            bookings[index].suggestedTime = suggestedTime
            saveToFirestore(bookings[index])
        }
    }
>>>>>>> d231b4d53e6f750ae3c0daf4f61493ce26eaf809
}
