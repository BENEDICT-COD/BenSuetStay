package com.mansuetdev.bensuetstay

data class RoomConfig(
    val roomType: String = "",
    val fundingType: FundingType = FundingType.NSFAS,
    val price: String = "",
    val spacesAvailable: Int = 0,
    val imageUrl: String? = null
)