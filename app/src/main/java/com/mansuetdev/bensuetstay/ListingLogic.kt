package com.mansuetdev.bensuetstay

/**
 * Listing rules with no Android dependencies, so they are covered by plain JUnit tests
 * (see ListingLogicTest).
 */
object ListingLogic {

    /** 2500 -> "R2 500" */
    fun formatRand(amount: Int): String =
        "R" + amount.coerceAtLeast(0).toString().reversed().chunked(3).joinToString(" ").reversed()

    /** 2500 -> "R2 500/mo" (English; used for the human-readable copy in Firestore) */
    fun formatRent(amount: Int): String = formatRand(amount) + "/mo"

    /**
     * Reads a rand amount from free text written by older versions of the app,
     * e.g. "R2 500/mo", "2,500", "R2500.00". Returns 0 when there is no number.
     */
    fun parseRent(text: String?): Int {
        if (text.isNullOrBlank()) return 0
        val beforeSlash = text.substringBefore("/")
        val withoutCents = beforeSlash.replace(Regex("[.,]\\d{2}\\s*$"), "")
        return withoutCents.filter { it.isDigit() }.take(9).toIntOrNull() ?: 0
    }

    fun hasCoordinates(latitude: Double, longitude: Double): Boolean = !(latitude == 0.0 && longitude == 0.0)

    /** Straight-line distance in km between two coordinates (haversine formula). */
    fun distanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
            Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2) * Math.sin(dLon / 2)
        return 2 * r * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
    }

    /** "Wits University, UJ Auckland Park" -> ["Wits University", "UJ Auckland Park"] */
    fun campusTokens(campusesServed: String): List<String> =
        campusesServed.split(',', ';', '/', '\n').map { it.trim() }.filter { it.isNotEmpty() }

    /**
     * True when a listing serves the student's institution. A listing with no campuses
     * listed is treated as open to every campus. Matching ignores case and punctuation and
     * works in both directions so "Rosebank College" matches "Rosebank College Durban".
     */
    fun matchesCampus(campusesServed: String, institution: String?): Boolean {
        if (institution.isNullOrBlank()) return true
        val tokens = campusTokens(campusesServed)
        if (tokens.isEmpty()) return true
        val student = normalise(institution)
        if (student.isEmpty()) return true
        return tokens.any { token ->
            val campus = normalise(token)
            campus.isNotEmpty() && (student.contains(campus) || campus.contains(student))
        }
    }

    private fun normalise(s: String): String =
        s.lowercase().replace(Regex("[^a-z0-9]+"), " ").trim()

    /** One homepage card per funding type a listing actually offers rooms for. */
    fun buildDisplayList(listings: List<AdminListing>): List<Accommodation> =
        listings.flatMap { listing ->
            FundingType.entries.mapNotNull { type ->
                val rooms = listing.rooms.filter { it.fundingType == type }
                if (rooms.isEmpty()) return@mapNotNull null

                // Cheapest self-funded rent; null for NSFAS cards or when no rent is set
                val minPrice = if (type == FundingType.NSFAS) null
                else rooms.map { it.priceAmount }.filter { it > 0 }.minOrNull()
                val photos = listing.photoUris.ifEmpty {
                    rooms.mapNotNull { it.imageUrl }.filter { it.isNotBlank() }
                }

                Accommodation(
                    id = "${listing.id}|${type.name}",
                    listingId = listing.id,
                    name = listing.name,
                    location = listing.location,
                    campusesServed = listing.campusesServed,
                    fundingType = type,
                    spacesAvailable = rooms.sumOf { it.spacesAvailable },
                    minPrice = minPrice,
                    roomTypes = rooms.map { it.roomType }.distinct(),
                    photoUris = photos
                )
            }
        }

    /**
     * Applies the homepage filters together.
     * @param funding null shows every funding type
     * @param campusOf the student's institution when "Near my campus" is on, otherwise null
     */
    fun filter(
        items: List<Accommodation>,
        funding: FundingType?,
        campusOf: String?,
        query: String
    ): List<Accommodation> {
        val q = query.trim()
        return items.filter { item ->
            (funding == null || item.fundingType == funding) &&
                (campusOf == null || matchesCampus(item.campusesServed, campusOf)) &&
                (q.isEmpty() ||
                    item.name.contains(q, ignoreCase = true) ||
                    item.location.contains(q, ignoreCase = true) ||
                    item.campusesServed.contains(q, ignoreCase = true) ||
                    item.roomTypes.any { it.contains(q, ignoreCase = true) })
        }
    }
}
