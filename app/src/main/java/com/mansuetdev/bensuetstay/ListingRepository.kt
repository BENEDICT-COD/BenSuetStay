package com.mansuetdev.bensuetstay

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

/**
 * Accommodation listings. Listings are public (guests can browse), so one app-wide
 * real-time listener keeps an in-memory copy that every screen reads from.
 * Screens subscribe with [addListener] in onStart and unsubscribe in onStop.
 */
object ListingRepository {
    const val COLLECTION = "accommodations"
    private const val TAG = "ListingRepository"

    private val collection get() = FirebaseFirestore.getInstance().collection(COLLECTION)

    @Volatile
    var listings: List<AdminListing> = emptyList()
        private set
    var isLoaded = false
        private set

    private val listeners = mutableListOf<() -> Unit>()
    private var registration: ListenerRegistration? = null

    fun start() {
        if (registration != null) return
        registration = collection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.w(TAG, "Listing sync failed", error)
                registration?.remove()
                registration = null // allow start() to retry later
                return@addSnapshotListener
            }
            if (snapshot != null) {
                listings = snapshot.documents.map { fromDoc(it) }.sortedBy { it.name.lowercase() }
                isLoaded = true
                listeners.toList().forEach { it() }
            }
        }
    }

    fun addListener(l: () -> Unit) {
        start()
        listeners.add(l)
    }

    fun removeListener(l: () -> Unit) {
        listeners.remove(l)
    }

    fun findById(id: String): AdminListing? = listings.find { it.id == id }

    /** Cached copy when available, otherwise a one-off read (e.g. right after a cold start). */
    fun get(id: String, onResult: (AdminListing?) -> Unit, onError: (Exception) -> Unit) {
        findById(id)?.let { onResult(it); return }
        collection.document(id).get()
            .addOnSuccessListener { doc -> onResult(if (doc.exists()) fromDoc(doc) else null) }
            .addOnFailureListener(onError)
    }

    fun newId(): String = collection.document().id

    fun save(listing: AdminListing): Task<Void> = collection.document(listing.id).set(toMap(listing))

    fun delete(id: String): Task<Void> = collection.document(id).delete()

    fun listingsForLandlord(landlordId: String): List<AdminListing> =
        listings.filter { it.assignedLandlordId == landlordId }

    // ---- Firestore mapping (explicit keys, so Kotlin's "is" prefix can't rename fields) ----

    fun fromDoc(doc: DocumentSnapshot): AdminListing {
        val rooms = (doc.get("rooms") as? List<*>).orEmpty()
            .mapNotNull { it as? Map<*, *> }
            .map { roomFromMap(it) }
        return AdminListing(
            id = doc.id,
            name = doc.getString("name") ?: "",
            location = doc.getString("location") ?: "",
            campusesServed = doc.getString("campusesServed") ?: "",
            amenities = (doc.get("amenities") as? List<*>).orEmpty().filterIsInstance<String>(),
            assignedLandlordId = doc.getString("assignedLandlordId")?.takeIf { it.isNotBlank() },
            assignedLandlordName = doc.getString("assignedLandlordName"),
            assignedLandlordPhone = doc.getString("assignedLandlordPhone"),
            rooms = rooms,
            description = doc.getString("description") ?: "",
            hasTransport = doc.getBoolean("hasTransport") ?: false,
            isFurnished = doc.getBoolean("isFurnished") ?: doc.getBoolean("furnished") ?: false,
            isPrepaidElectricity = doc.getBoolean("isPrepaidElectricity")
                ?: doc.getBoolean("prepaidElectricity") ?: false,
            latitude = doc.getDouble("latitude") ?: 0.0,
            longitude = doc.getDouble("longitude") ?: 0.0,
            // Links to files on the admin's phone: they display there, other phones show the placeholder
            photoUris = (doc.get("photoUris") as? List<*>).orEmpty().filterIsInstance<String>(),
            videoUri = doc.getString("videoUri") ?: ""
        )
    }

    fun roomFromMap(r: Map<*, *>): RoomConfig {
        val funding = FundingType.fromStorage(r["fundingType"] as? String)
        val amount = (r["priceAmount"] as? Number)?.toInt()
            ?: ListingLogic.parseRent(r["price"] as? String)
        return RoomConfig(
            roomType = r["roomType"] as? String ?: "",
            fundingType = funding,
            priceAmount = if (funding == FundingType.NSFAS) 0 else amount,
            spacesAvailable = (r["spacesAvailable"] as? Number)?.toInt() ?: 0,
            imageUrl = (r["imageUrl"] as? String)?.takeIf { it.isNotBlank() }
        )
    }

    fun roomToMap(r: RoomConfig): Map<String, Any?> = mapOf(
        "roomType" to r.roomType,
        "fundingType" to r.fundingType.name,
        "priceAmount" to r.priceAmount,
        "price" to r.priceLabel, // human-readable copy for the Firebase console
        "spacesAvailable" to r.spacesAvailable,
        "imageUrl" to r.imageUrl
    )

    fun toMap(l: AdminListing): Map<String, Any?> = mapOf(
        "name" to l.name,
        "location" to l.location,
        "campusesServed" to l.campusesServed,
        "amenities" to l.amenities,
        "assignedLandlordId" to l.assignedLandlordId,
        "assignedLandlordName" to l.assignedLandlordName,
        "assignedLandlordPhone" to l.assignedLandlordPhone,
        "rooms" to l.rooms.map { roomToMap(it) },
        "description" to l.description,
        "hasTransport" to l.hasTransport,
        "isFurnished" to l.isFurnished,
        "isPrepaidElectricity" to l.isPrepaidElectricity,
        "latitude" to l.latitude,
        "longitude" to l.longitude,
        "photoUris" to l.photoUris,
        "videoUri" to l.videoUri
    )
}
