package com.mansuetdev.bensuetstay

enum class FundingType {
    NSFAS,
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
