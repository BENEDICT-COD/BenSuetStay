package com.mansuetdev.bensuetstay

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ListingLogicTest {

    private fun room(type: String, funding: FundingType, price: Int = 0, spaces: Int = 1) =
        RoomConfig(roomType = type, fundingType = funding, priceAmount = price, spacesAvailable = spaces)

    private val mixed = AdminListing(
        id = "abc123",
        name = "Varsity Heights",
        location = "Braamfontein",
        campusesServed = "Wits University, UJ Auckland Park",
        rooms = listOf(
            room("Single", FundingType.NSFAS, spaces = 3),
            room("Single", FundingType.SELF_OR_BURSARY, price = 10_000, spaces = 1),
            room("2-Sharing", FundingType.SELF_OR_BURSARY, price = 2_500, spaces = 4)
        )
    )

    @Test fun formatRent_groupsThousandsWithSpaces() {
        assertEquals("R2 500/mo", ListingLogic.formatRent(2500))
        assertEquals("R10 000/mo", ListingLogic.formatRent(10000))
        assertEquals("R950/mo", ListingLogic.formatRent(950))
        assertEquals("R1 250 000", ListingLogic.formatRand(1_250_000))
    }

    @Test fun parseRent_readsOldFreeTextPrices() {
        assertEquals(2500, ListingLogic.parseRent("R2 500/mo"))
        assertEquals(2500, ListingLogic.parseRent("2,500"))
        assertEquals(2500, ListingLogic.parseRent("R2500.00"))
        assertEquals(2500, ListingLogic.parseRent("R2 500,00"))
        assertEquals(0, ListingLogic.parseRent("NSFAS Allowance"))
        assertEquals(0, ListingLogic.parseRent(null))
    }

    // Regression: prices used to be compared as strings, so "R10 000" counted as cheaper than "R2 500"
    @Test fun cheapestPrice_isNumericNotAlphabetical() {
        val selfFunded = ListingLogic.buildDisplayList(listOf(mixed)).single { it.fundingType == FundingType.SELF_OR_BURSARY }
        assertEquals(2500, selfFunded.minPrice)
        assertEquals(5, selfFunded.spacesAvailable)
    }

    @Test fun buildDisplayList_oneCardPerFundingTypeWithItsOwnRooms() {
        val cards = ListingLogic.buildDisplayList(listOf(mixed))
        assertEquals(2, cards.size)
        val nsfas = cards.single { it.fundingType == FundingType.NSFAS }
        assertEquals(null, nsfas.minPrice)
        assertEquals(3, nsfas.spacesAvailable)
        assertEquals(listOf("Single"), nsfas.roomTypes)
    }

    // Regression: the card id used to be "<id>_SELF_OR_BURSARY", and cutting at the last "_"
    // gave "<id>_SELF_OR", so self-funded cards couldn't be opened
    @Test fun everyCard_carriesTheRealListingId() {
        ListingLogic.buildDisplayList(listOf(mixed)).forEach { assertEquals("abc123", it.listingId) }
    }

    @Test fun matchesCampus_handlesListsCaseAndPartialNames() {
        assertTrue(ListingLogic.matchesCampus("Wits University, UJ Auckland Park", "wits university"))
        assertTrue(ListingLogic.matchesCampus("Wits University, UJ Auckland Park", "UJ Auckland Park"))
        assertTrue(ListingLogic.matchesCampus("Rosebank College", "Rosebank College Durban"))
        assertFalse(ListingLogic.matchesCampus("Wits University", "Durban University of Technology"))
    }

    @Test fun matchesCampus_blankMeansNoRestriction() {
        assertTrue(ListingLogic.matchesCampus("", "Wits University"))
        assertTrue(ListingLogic.matchesCampus("Wits University", null))
        assertTrue(ListingLogic.matchesCampus("Wits University", "   "))
    }

    @Test fun filter_combinesFundingCampusAndSearch() {
        val durban = mixed.copy(id = "d1", name = "Berea Lofts", location = "Berea, Durban", campusesServed = "DUT")
        val cards = ListingLogic.buildDisplayList(listOf(mixed, durban))

        // Self-funded student at Wits: only Varsity Heights' self-funded card
        val witsSelf = ListingLogic.filter(cards, FundingType.SELF_OR_BURSARY, "Wits University", "")
        assertEquals(listOf("abc123"), witsSelf.map { it.listingId })

        // Search narrows inside the active filters, and matches location
        assertEquals(1, ListingLogic.filter(cards, FundingType.NSFAS, null, "berea").size)
        // No filters: everything
        assertEquals(4, ListingLogic.filter(cards, null, null, "").size)
    }

    @Test fun fundingType_readsBothStoredSpellings() {
        assertEquals(FundingType.SELF_OR_BURSARY, FundingType.fromStorage("SELF_FUNDED"))
        assertEquals(FundingType.SELF_OR_BURSARY, FundingType.fromStorage("SELF_OR_BURSARY"))
        assertEquals(FundingType.NSFAS, FundingType.fromStorage("NSFAS"))
    }

    @Test fun distanceKm_matchesKnownDistances() {
        // Johannesburg CBD -> Durban CBD is roughly 500 km in a straight line
        val jhbToDurban = ListingLogic.distanceKm(-26.2041, 28.0473, -29.8587, 31.0218)
        assertTrue(jhbToDurban in 480.0..520.0)
        assertEquals(0.0, ListingLogic.distanceKm(-26.19, 28.03, -26.19, 28.03), 1e-9)
        assertFalse(ListingLogic.hasCoordinates(0.0, 0.0))
        assertTrue(ListingLogic.hasCoordinates(-26.19, 28.03))
    }
}
