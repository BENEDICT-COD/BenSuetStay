package com.mansuetdev.bensuetstay

data class AccommodationDetail(
    val name: String,
    val campusAcceptedLabel: String,
    val location: String,
    val distanceFromCampus: String,
    val amenities: List<String>,
    val landlordPhone: String,
    val totalSpacesOpen: Int,
    val imageCount: Int,
    val rooms: List<RoomOption>,
    val description: String,
    val hasTransport: Boolean,
    val isFurnished: Boolean,
    val isPrepaidElectricity: Boolean
)