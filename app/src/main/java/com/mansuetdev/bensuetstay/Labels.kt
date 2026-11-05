package com.mansuetdev.bensuetstay

import android.content.Context

/**
 * Display text for values that are stored in Firestore in English (amenities, room types,
 * funding) and for prices. The stored value never changes with the app language, so
 * filtering and approvals keep matching; only what the user sees is translated.
 */
object Labels {
    /** Stored value -> string resource. Order is the order shown in the listing form. */
    val AMENITIES = listOf(
        "WiFi" to R.string.amenity_wifi,
        "Security" to R.string.amenity_security,
        "Meals" to R.string.amenity_meals,
        "Water incl." to R.string.amenity_water
    )
    val ROOM_TYPES = listOf(
        "Single" to R.string.room_single,
        "2-Sharing" to R.string.room_2_sharing,
        "3-Sharing" to R.string.room_3_sharing,
        "4-Sharing" to R.string.room_4_sharing
    )

    fun amenity(context: Context, stored: String): String =
        AMENITIES.firstOrNull { it.first == stored }?.let { context.getString(it.second) } ?: stored

    fun roomType(context: Context, stored: String): String =
        ROOM_TYPES.firstOrNull { it.first == stored }?.let { context.getString(it.second) } ?: stored

    fun funding(context: Context, type: FundingType): String =
        context.getString(if (type == FundingType.NSFAS) R.string.nsfas else R.string.self_funded)

    /** "NSFAS Allowance" or "R2 500/mo" in the current language. */
    fun roomPrice(context: Context, room: RoomConfig): String =
        if (room.fundingType == FundingType.NSFAS) context.getString(R.string.nsfas_allowance)
        else context.getString(R.string.rent_per_month, ListingLogic.formatRand(room.priceAmount))

    /** Price line on a homepage card. */
    fun cardPrice(context: Context, item: Accommodation): String = when {
        item.fundingType == FundingType.NSFAS -> context.getString(R.string.nsfas_funded)
        item.minPrice == null -> context.getString(R.string.price_on_request)
        else -> context.getString(R.string.from_price, context.getString(R.string.rent_per_month, ListingLogic.formatRand(item.minPrice)))
    }
}
