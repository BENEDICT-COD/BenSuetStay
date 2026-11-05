package com.mansuetdev.bensuetstay

enum class FundingType {
    NSFAS,
<<<<<<< HEAD
    SELF_OR_BURSARY;

    companion object {
        /** Older student records stored "SELF_FUNDED", rooms store the enum name. Accept both. */
        fun fromStorage(value: String?): FundingType = when (value) {
            "SELF_OR_BURSARY", "SELF_FUNDED" -> SELF_OR_BURSARY
            else -> NSFAS
        }
    }
}

/**
 * One card on the homepage. A listing that offers both NSFAS and self-funded rooms
 * becomes two cards, each showing only the rooms of its funding type.
 */
data class Accommodation(
    val id: String,
    val listingId: String,
    val name: String,
    val location: String,
    val campusesServed: String,
    val fundingType: FundingType,
    val spacesAvailable: Int,
    val minPrice: Int?,
    val roomTypes: List<String>,
    val photoUris: List<String> = emptyList()
)
=======
    SELF_OR_BURSARY
}

data class Accommodation(
    val id: String,
    val name: String,
    val location: String,
    val fundingType: FundingType,
    val spacesAvailable: Int,
    val priceLabel: String,
    val roomTypes: List<String>,
    val photoUris: List<String> = emptyList(),
    val description: String = "",
    val hasTransport: Boolean = false,
    val isFurnished: Boolean = false,
    val isPrepaidElectricity: Boolean = false,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)
>>>>>>> d231b4d53e6f750ae3c0daf4f61493ce26eaf809
