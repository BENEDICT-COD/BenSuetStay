package com.mansuetdev.bensuetstay

data class AdminListing(
    val id: String = "",
    val name: String = "",
    val location: String = "",
    val campusesServed: String = "",
    val amenities: List<String> = emptyList(),
    val assignedLandlordId: String? = null,
    val assignedLandlordName: String? = null,
    // Copied onto the listing so students can call without reading the landlords collection
    val assignedLandlordPhone: String? = null,
    val rooms: List<RoomConfig> = emptyList(),
    val description: String = "",
    val hasTransport: Boolean = false,
    val isFurnished: Boolean = false,
    val isPrepaidElectricity: Boolean = false,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    // Links to the picked files on the admin's phone (content://...). No Firebase Storage needed.
    val photoUris: List<String> = emptyList(),
    val videoUri: String = ""
) {
    val totalSpaces: Int get() = rooms.sumOf { it.spacesAvailable }
}
