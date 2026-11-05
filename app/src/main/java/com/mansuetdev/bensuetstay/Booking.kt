package com.mansuetdev.bensuetstay

enum class BookingStatus { PENDING, CONFIRMED, DECLINED }

data class Booking(
    val id: String,
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
