package com.mansuetdev.bensuetstay

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
}
