package com.mansuetdev.bensuetstay

data class AdminListing(
    val id: String = "",
    val name: String = "",
    val location: String = "",
    val campusesServed: String = "",
    val amenities: List<String> = emptyList(),
    val assignedLandlordId: String? = null,
    val assignedLandlordName: String? = null,
<<<<<<< HEAD
    // Copied onto the listing so students can call without reading the landlords collection
    val assignedLandlordPhone: String? = null,
    val rooms: List<RoomConfig> = emptyList(),
=======
    val rooms: List<RoomConfig> = emptyList(),
    val photoCount: Int = 0,
    val videoCount: Int = 0,
>>>>>>> d231b4d53e6f750ae3c0daf4f61493ce26eaf809
    val description: String = "",
    val hasTransport: Boolean = false,
    val isFurnished: Boolean = false,
    val isPrepaidElectricity: Boolean = false,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
<<<<<<< HEAD
    // Links to the picked files on the admin's phone (content://...). No Firebase Storage needed.
    val photoUris: List<String> = emptyList(),
    val videoUri: String = ""
) {
    val totalSpaces: Int get() = rooms.sumOf { it.spacesAvailable }
}
=======
    val photoUris: List<String> = emptyList(),
    val videoUri: String = ""
)
>>>>>>> d231b4d53e6f750ae3c0daf4f61493ce26eaf809
