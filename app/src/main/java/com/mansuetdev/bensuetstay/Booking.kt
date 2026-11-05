package com.mansuetdev.bensuetstay

enum class BookingStatus { PENDING, CONFIRMED, DECLINED }

data class Booking(
    val id: String,
<<<<<<< HEAD
    val studentId: String,
    val studentName: String,
    val listingId: String,
    val accommodationName: String,
    val landlordId: String,
    val requestedDateTime: String,
    val requestedAtMillis: Long,
    val status: BookingStatus,
    val rejectionReason: String? = null,
    val suggestedTime: String? = null,
    // Saved on this phone while offline, not sent yet
    val pendingSync: Boolean = false
)
=======
    val studentName: String,
    val accommodationName: String,
    val requestedDateTime: String,
    var status: BookingStatus,
    var rejectionReason: String? = null,
    var suggestedTime: String? = null
)
>>>>>>> d231b4d53e6f750ae3c0daf4f61493ce26eaf809
