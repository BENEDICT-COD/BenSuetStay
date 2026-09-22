package com.mansuetdev.bensuetstay

data class AdminListing(
    val id: String = "",
    val name: String = "",
    val location: String = "",
    val campusesServed: String = "",
    val amenities: List<String> = emptyList(),
    val assignedLandlordId: String? = null,
    val assignedLandlordName: String? = null,
    val rooms: List<RoomConfig> = emptyList(),
    val photoCount: Int = 0,
    val videoCount: Int = 0,
    val description: String = "",
    val hasTransport: Boolean = false,
    val isFurnished: Boolean = false,
    val isPrepaidElectricity: Boolean = false,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val photoUris: List<String> = emptyList(),
    val videoUri: String = ""
)