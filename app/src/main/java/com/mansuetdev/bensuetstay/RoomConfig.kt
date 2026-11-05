package com.mansuetdev.bensuetstay

data class RoomConfig(
    val roomType: String = "",
    val fundingType: FundingType = FundingType.NSFAS,
<<<<<<< HEAD
    // Monthly rent in rand. 0 for NSFAS rooms (covered by the allowance).
    val priceAmount: Int = 0,
    val spacesAvailable: Int = 0,
    val imageUrl: String? = null
) {
    val priceLabel: String
        get() = if (fundingType == FundingType.NSFAS) "NSFAS Allowance" else ListingLogic.formatRent(priceAmount)
}
=======
    val price: String = "",
    val spacesAvailable: Int = 0,
    val imageUrl: String? = null
)
>>>>>>> d231b4d53e6f750ae3c0daf4f61493ce26eaf809
