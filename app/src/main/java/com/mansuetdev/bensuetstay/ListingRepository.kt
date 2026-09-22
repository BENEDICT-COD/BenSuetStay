package com.mansuetdev.bensuetstay

import com.google.firebase.firestore.FirebaseFirestore

object ListingRepository {
    private val db = FirebaseFirestore.getInstance()
    private val collection = db.collection("accommodations")

    val listings = mutableListOf<AdminListing>()

    private val listeners = mutableListOf<() -> Unit>()

    fun addListener(l: () -> Unit) { listeners.add(l) }
    fun removeListener(l: () -> Unit) { listeners.remove(l) }

    private fun notifyListeners() {
        listeners.forEach { it() }
    }

    init {
        collection.addSnapshotListener { snapshot, _ ->
            if (snapshot != null && !snapshot.isEmpty) {
                val newList = mutableListOf<AdminListing>()
                for (doc in snapshot.documents) {
                    val id = doc.id
                    val name = doc.getString("name") ?: ""
                    val location = doc.getString("location") ?: ""
                    val campusesServed = doc.getString("campusesServed") ?: ""
                    val amenities = doc.get("amenities") as? List<String> ?: emptyList()
                    val assignedLandlordId = doc.getString("assignedLandlordId")
                    val assignedLandlordName = doc.getString("assignedLandlordName")
                    val photoCount = doc.getLong("photoCount")?.toInt() ?: 0
                    val videoCount = doc.getLong("videoCount")?.toInt() ?: 0
                    val description = doc.getString("description") ?: ""
                    val hasTransport = doc.getBoolean("hasTransport") ?: false
                    val isFurnished = doc.getBoolean("isFurnished") ?: false
                    val isPrepaidElectricity = doc.getBoolean("isPrepaidElectricity") ?: false
                    val latitude = doc.getDouble("latitude") ?: 0.0
                    val longitude = doc.getDouble("longitude") ?: 0.0
                    val photoUris = doc.get("photoUris") as? List<String> ?: emptyList()
                    val videoUri = doc.getString("videoUri") ?: ""

                    val roomsRaw = doc.get("rooms") as? List<Map<String, Any>> ?: emptyList()
                    val rooms = roomsRaw.map { r ->
                        val rt = r["roomType"] as? String ?: ""
                        val ftStr = r["fundingType"] as? String ?: "NSFAS"
                        val ft = if (ftStr == "SELF_OR_BURSARY") FundingType.SELF_OR_BURSARY else FundingType.NSFAS
                        val pr = r["price"] as? String ?: ""
                        val sa = (r["spacesAvailable"] as? Long)?.toInt() ?: 0
                        val img = r["imageUrl"] as? String
                        RoomConfig(rt, ft, pr, sa, img)
                    }

                    newList.add(
                        AdminListing(
                            id = id,
                            name = name,
                            location = location,
                            campusesServed = campusesServed,
                            amenities = amenities,
                            assignedLandlordId = assignedLandlordId,
                            assignedLandlordName = assignedLandlordName,
                            rooms = rooms,
                            photoCount = photoCount,
                            videoCount = videoCount,
                            description = description,
                            hasTransport = hasTransport,
                            isFurnished = isFurnished,
                            isPrepaidElectricity = isPrepaidElectricity,
                            latitude = latitude,
                            longitude = longitude,
                            photoUris = photoUris,
                            videoUri = videoUri
                        )
                    )
                }
                listings.clear()
                listings.addAll(newList)
                notifyListeners()
            } else if (snapshot != null && snapshot.isEmpty) {
                listings.clear()
                notifyListeners()
            }
        }
    }

    private fun saveToFirestore(listing: AdminListing) {
        val roomsMap = listing.rooms.map { r ->
            mapOf(
                "roomType" to r.roomType,
                "fundingType" to r.fundingType.name,
                "price" to r.price,
                "spacesAvailable" to r.spacesAvailable,
                "imageUrl" to r.imageUrl
            )
        }
        val data = mapOf(
            "name" to listing.name,
            "location" to listing.location,
            "campusesServed" to listing.campusesServed,
            "amenities" to listing.amenities,
            "assignedLandlordId" to listing.assignedLandlordId,
            "assignedLandlordName" to listing.assignedLandlordName,
            "rooms" to roomsMap,
            "photoCount" to listing.photoCount,
            "videoCount" to listing.videoCount,
            "description" to listing.description,
            "hasTransport" to listing.hasTransport,
            "isFurnished" to listing.isFurnished,
            "isPrepaidElectricity" to listing.isPrepaidElectricity,
            "latitude" to listing.latitude,
            "longitude" to listing.longitude,
            "photoUris" to listing.photoUris,
            "videoUri" to listing.videoUri
        )
        collection.document(listing.id).set(data)
    }

    fun findById(id: String): AdminListing? = listings.find { it.id == id }

    fun addListing(listing: AdminListing) {
        val index = listings.indexOfFirst { it.id == listing.id }
        if (index == -1) listings.add(listing) else listings[index] = listing
        saveToFirestore(listing)
    }

    fun updateListing(updated: AdminListing) {
        val index = listings.indexOfFirst { it.id == updated.id }
        if (index != -1) listings[index] = updated else listings.add(updated)
        saveToFirestore(updated)
    }

    fun deleteListing(id: String) {
        listings.removeAll { it.id == id }
        collection.document(id).delete()
    }
}
