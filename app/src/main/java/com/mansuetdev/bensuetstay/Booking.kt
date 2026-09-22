package com.mansuetdev.bensuetstay

enum class BookingStatus { PENDING, CONFIRMED, DECLINED }

data class Booking(
    val id: String,
    val studentName: String,
    val accommodationName: String,
    val requestedDateTime: String,
    var status: BookingStatus,
    var rejectionReason: String? = null,
    var suggestedTime: String? = null
)