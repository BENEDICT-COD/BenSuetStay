package com.mansuetdev.bensuetstay

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Handler
import android.os.Looper
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Offline mode with sync. Bookings and applications made without internet go into the
 * SQLite outbox (OfflineStore); when Android reports a working connection again, everything
 * queued is sent to Firestore automatically. Screens subscribe with [addListener] to refresh.
 */
object SyncManager {
    private const val MAX_ATTEMPTS = 3

    private val listeners = CopyOnWriteArrayList<() -> Unit>()
    private val mainThread = Handler(Looper.getMainLooper())
    @Volatile private var syncing = false
    private var watchingNetwork = false

    fun isOnline(context: Context): Boolean {
        val cm = context.getSystemService(ConnectivityManager::class.java) ?: return false
        val caps = cm.getNetworkCapabilities(cm.activeNetwork) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    /** Called once from BenSuetStayApp: sync now and every time the internet comes back. */
    fun start(context: Context) {
        val app = context.applicationContext
        if (!watchingNetwork) {
            app.getSystemService(ConnectivityManager::class.java)?.registerDefaultNetworkCallback(
                object : ConnectivityManager.NetworkCallback() {
                    override fun onCapabilitiesChanged(network: Network, caps: NetworkCapabilities) {
                        if (caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)) {
                            mainThread.post { syncNow(app) }
                        }
                    }
                }
            )
            watchingNetwork = true
        }
        syncNow(app)
    }

    fun addListener(l: () -> Unit) { listeners.add(l) }
    fun removeListener(l: () -> Unit) { listeners.remove(l) }
    private fun changed() = mainThread.post { listeners.forEach { it() } }

    // ---------- queueing ----------

    fun queueBooking(context: Context, booking: Booking) {
        OfflineStore.get(context).add(OfflineStore.TYPE_BOOKING, booking.studentId, booking.listingId, booking.id, bookingToJson(booking))
        changed()
    }

    fun queueApplication(context: Context, application: StudentApplication) {
        OfflineStore.get(context).add(
            OfflineStore.TYPE_APPLICATION, application.studentId, application.listingId, application.id, applicationToJson(application)
        )
        changed()
    }

    fun queuedCount(context: Context, ownerId: String): Int = OfflineStore.get(context).pending(ownerId).size

    fun hasQueuedApplication(context: Context, studentId: String, listingId: String): Boolean =
        OfflineStore.get(context).has(studentId, OfflineStore.TYPE_APPLICATION, listingId)

    fun queuedBookings(context: Context, studentId: String): List<Booking> =
        OfflineStore.get(context).pending(studentId, OfflineStore.TYPE_BOOKING)
            .mapNotNull { bookingFromJson(it.payload)?.copy(pendingSync = true) }

    fun queuedApplications(context: Context, studentId: String): List<StudentApplication> =
        OfflineStore.get(context).pending(studentId, OfflineStore.TYPE_APPLICATION)
            .mapNotNull { applicationFromJson(it.payload)?.copy(pendingSync = true) }

    // ---------- syncing ----------

    fun syncNow(context: Context) {
        val app = context.applicationContext
        if (syncing || !isOnline(app)) return
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return // Firestore rules need the owner signed in
        val store = OfflineStore.get(app)
        val items = store.pending(uid)
        if (items.isEmpty()) return

        syncing = true
        var dropped = 0
        val results: List<Task<Boolean>> = items.map { item ->
            send(item).continueWith { t ->
                if (t.isSuccessful) {
                    store.remove(item.id)
                    true
                } else {
                    if (store.recordFailure(item.id, t.exception?.message) >= MAX_ATTEMPTS) {
                        store.remove(item.id) // e.g. the listing was deleted meanwhile; stop retrying
                        dropped++
                    }
                    false
                }
            }
        }
        Tasks.whenAllComplete(results).addOnCompleteListener {
            syncing = false
            val sent = results.count { it.isSuccessful && it.result == true }
            changed()
            if (sent > 0) Notifier.show(app, app.getString(R.string.sync_done_title), app.getString(R.string.sync_done_body, sent))
            if (dropped > 0) Notifier.show(app, app.getString(R.string.app_name), app.getString(R.string.sync_dropped_body))
        }
    }

    private fun send(item: OfflineStore.Item): Task<Void> = when (item.type) {
        OfflineStore.TYPE_BOOKING ->
            bookingFromJson(item.payload)?.let { BookingRepository.create(it) } ?: Tasks.forResult<Void>(null)
        OfflineStore.TYPE_APPLICATION -> applicationFromJson(item.payload)?.let { application ->
            // Re-check for duplicates now that we can reach the server
            ApplicationRepository.hasActiveApplication(application.studentId, application.listingId)
                .continueWithTask { check ->
                    if (check.isSuccessful && check.result == true) Tasks.forResult<Void>(null)
                    else ApplicationRepository.submit(application, application.documents.map { it.name })
                }
        } ?: Tasks.forResult<Void>(null)
        else -> Tasks.forResult<Void>(null)
    }

    // ---------- JSON payloads stored in SQLite ----------

    private fun bookingToJson(b: Booking): String = JSONObject()
        .put("id", b.id).put("studentId", b.studentId).put("studentName", b.studentName)
        .put("listingId", b.listingId).put("accommodationName", b.accommodationName)
        .put("landlordId", b.landlordId).put("requestedDateTime", b.requestedDateTime)
        .put("requestedAtMillis", b.requestedAtMillis)
        .toString()

    private fun bookingFromJson(s: String): Booking? = try {
        val j = JSONObject(s)
        Booking(
            id = j.getString("id"),
            studentId = j.getString("studentId"),
            studentName = j.getString("studentName"),
            listingId = j.getString("listingId"),
            accommodationName = j.getString("accommodationName"),
            landlordId = j.getString("landlordId"),
            requestedDateTime = j.getString("requestedDateTime"),
            requestedAtMillis = j.getLong("requestedAtMillis"),
            status = BookingStatus.PENDING
        )
    } catch (e: JSONException) { null }

    private fun applicationToJson(a: StudentApplication): String = JSONObject()
        .put("id", a.id).put("studentId", a.studentId).put("studentName", a.studentName)
        .put("studentEmail", a.studentEmail).put("listingId", a.listingId)
        .put("accommodationName", a.accommodationName).put("landlordId", a.landlordId)
        .put("roomType", a.roomType).put("applicationType", a.applicationType.name)
        .put("dateApplied", a.dateApplied).put("createdAtMillis", a.createdAtMillis)
        .put("idNumber", a.idNumber).put("studentNumber", a.studentNumber).put("campus", a.campus)
        .put("nextOfKinName", a.nextOfKinName).put("nextOfKinPhone", a.nextOfKinPhone)
        .put("documents", JSONArray(a.documents.map { it.name }))
        .toString()

    private fun applicationFromJson(s: String): StudentApplication? = try {
        val j = JSONObject(s)
        val docs = j.optJSONArray("documents")
        StudentApplication(
            id = j.getString("id"),
            studentId = j.getString("studentId"),
            studentName = j.getString("studentName"),
            studentEmail = j.getString("studentEmail"),
            listingId = j.getString("listingId"),
            accommodationName = j.getString("accommodationName"),
            landlordId = j.getString("landlordId"),
            roomType = j.getString("roomType"),
            applicationType = ApplicationType.valueOf(j.getString("applicationType")),
            dateApplied = j.getString("dateApplied"),
            createdAtMillis = j.getLong("createdAtMillis"),
            status = ApplicationStatus.SUBMITTED,
            idNumber = j.optString("idNumber", null),
            studentNumber = j.optString("studentNumber", null),
            campus = j.optString("campus", null),
            nextOfKinName = j.optString("nextOfKinName", null),
            nextOfKinPhone = j.optString("nextOfKinPhone", null),
            documents = (0 until (docs?.length() ?: 0)).map { AppDocument(docs!!.getString(it), null) }
        )
    } catch (e: Exception) { null }
}
