package com.mansuetdev.bensuetstay

<<<<<<< HEAD
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
=======
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
>>>>>>> d231b4d53e6f750ae3c0daf4f61493ce26eaf809
            }
        }
    }

<<<<<<< HEAD
    fun addListener(l: () -> Unit) {
        start()
        listeners.add(l)
    }

    fun removeListener(l: () -> Unit) {
        listeners.remove(l)
=======
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
>>>>>>> d231b4d53e6f750ae3c0daf4f61493ce26eaf809
    }

    fun findById(id: String): AdminListing? = listings.find { it.id == id }

<<<<<<< HEAD
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
=======
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
>>>>>>> d231b4d53e6f750ae3c0daf4f61493ce26eaf809
}
