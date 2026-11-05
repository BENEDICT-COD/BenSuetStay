package com.mansuetdev.bensuetstay

import android.content.Context
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.messaging.FirebaseMessaging

enum class AlertType {
    BOOKING_REQUESTED, BOOKING_CONFIRMED, BOOKING_DECLINED,
    APPLICATION_SUBMITTED, APPLICATION_APPROVED, APPLICATION_DECLINED
}

/**
 * Real-time notifications.
 *  - Event alerts: every booking/application action also writes a small document to
 *    notifications/ (in the same batch or transaction). The recipient's app listens for
 *    its unread alerts and shows them as system notifications straight away. Only the alert
 *    type and its values are stored, so each person reads it in their own app language.
 *  - Push (FCM): users are subscribed to topics ("all", "students", "landlords", "admins"),
 *    so announcements sent from the Firebase console arrive even when the app is closed.
 */
object NotificationCenter {
    const val COLLECTION = "notifications"
    private val collection get() = FirebaseFirestore.getInstance().collection(COLLECTION)
    private var registration: ListenerRegistration? = null

    fun newRef(): DocumentReference = collection.document()

    /** The alert document to write alongside the action that caused it. */
    fun alert(recipientId: String, type: AlertType, vararg args: String): Map<String, Any?> = mapOf(
        "recipientId" to recipientId,
        "senderId" to (FirebaseAuth.getInstance().currentUser?.uid ?: ""),
        "type" to type.name,
        "args" to args.toList(),
        "read" to false,
        "createdAt" to Timestamp.now()
    )

    /** Listen for this user's unread alerts while the app is running. */
    fun start(context: Context) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val app = context.applicationContext
        stop()
        if (!AppSettings.alertsEnabled(app)) return // turned off in Settings → Notifications
        registration = collection.whereEqualTo("recipientId", uid).whereEqualTo("read", false)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                for (change in snapshot.documentChanges) {
                    if (change.type != DocumentChange.Type.ADDED) continue
                    val doc = change.document
                    if (doc.metadata.hasPendingWrites()) continue
                    val type = AlertType.entries.firstOrNull { it.name == doc.getString("type") }
                    val args = (doc.get("args") as? List<*>).orEmpty().map { it?.toString().orEmpty() }
                    if (type != null) format(app, type, args)?.let { (title, body) -> Notifier.show(app, title, body) }
                    doc.reference.update("read", true)
                }
            }
    }

    fun stop() {
        registration?.remove()
        registration = null
    }

    fun format(context: Context, type: AlertType, args: List<String>): Pair<String, String>? {
        val a = { i: Int -> args.getOrElse(i) { "" } }
        return try {
            when (type) {
                AlertType.BOOKING_REQUESTED -> context.getString(R.string.notif_booking_requested_title) to
                    context.getString(R.string.notif_booking_requested_body, a(0), a(1), a(2))
                AlertType.BOOKING_CONFIRMED -> context.getString(R.string.notif_booking_confirmed_title) to
                    context.getString(R.string.notif_booking_confirmed_body, a(0), a(1))
                AlertType.BOOKING_DECLINED -> context.getString(R.string.viewing_declined) to
                    context.getString(R.string.notif_booking_declined_body, a(0))
                AlertType.APPLICATION_SUBMITTED -> context.getString(R.string.notif_application_submitted_title) to
                    context.getString(R.string.notif_application_submitted_body, a(0), Labels.roomType(context, a(1)), a(2))
                AlertType.APPLICATION_APPROVED -> context.getString(R.string.notif_application_approved_title) to
                    context.getString(R.string.notif_application_approved_body, a(0))
                AlertType.APPLICATION_DECLINED -> context.getString(R.string.notif_application_declined_title) to
                    context.getString(R.string.notif_application_declined_body, a(0))
            }
        } catch (e: Exception) {
            null
        }
    }

    // ---------- FCM topics ----------

    private val ROLE_TOPICS = mapOf(Role.STUDENT to "students", Role.LANDLORD to "landlords", Role.ADMIN to "admins")

    fun subscribeTopics(context: Context, role: Role) {
        val messaging = FirebaseMessaging.getInstance()
        if (!AppSettings.announcementsEnabled(context)) { // turned off in Settings → Notifications
            messaging.unsubscribeFromTopic("all")
            unsubscribeRoleTopics()
            return
        }
        messaging.subscribeToTopic("all")
        ROLE_TOPICS.forEach { (r, topic) ->
            if (r == role) messaging.subscribeToTopic(topic) else messaging.unsubscribeFromTopic(topic)
        }
    }

    fun unsubscribeRoleTopics() {
        val messaging = FirebaseMessaging.getInstance()
        ROLE_TOPICS.values.forEach { messaging.unsubscribeFromTopic(it) }
    }
}
