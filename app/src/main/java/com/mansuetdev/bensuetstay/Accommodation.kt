package com.mansuetdev.bensuetstay

enum class FundingType {
    NSFAS,
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