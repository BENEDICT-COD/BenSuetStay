package com.mansuetdev.bensuetstay

import android.net.Uri
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ApplicationForm {
    const val EXTRA_ROOM_TYPE = "ROOM_TYPE"
    const val EXTRA_ROOM_FUNDING = "ROOM_FUNDING"
    /** Types the document picker offers. */
    val DOCUMENT_TYPES = arrayOf("application/pdf", "image/*")
}

/**
 * Shared plumbing for the NSFAS and self-funded application screens: loads the listing and
 * room from the intent, blocks duplicate applications, records the documents' names, saves.
 */
abstract class ApplicationFormActivity : BaseActivity() {

    protected var listing: AdminListing? = null
    protected var room: RoomConfig? = null
    private var submitting = false

    /** Call from onCreate after setContentView. Finishes the screen if the listing or room is gone. */
    protected fun loadListingAndRoom(onReady: (AdminListing, RoomConfig) -> Unit) {
        val listingId = intent.getStringExtra(ListingDetailActivity.EXTRA_LISTING_ID).orEmpty()
        val roomType = intent.getStringExtra(ApplicationForm.EXTRA_ROOM_TYPE).orEmpty()
        val funding = FundingType.fromStorage(intent.getStringExtra(ApplicationForm.EXTRA_ROOM_FUNDING))
        ListingRepository.get(
            listingId,
            onResult = { found ->
                val match = found?.rooms?.firstOrNull { it.roomType == roomType && it.fundingType == funding }
                if (found == null || match == null) {
                    toast(getString(R.string.this_room_is_no_longer_available))
                    finish()
                } else {
                    listing = found
                    room = match
                    onReady(found, match)
                }
            },
            onError = { e -> toast(AuthErrors.message(this, e)); finish() }
        )
    }

    protected fun showPickedDocument(label: TextView, uri: Uri) {
        label.text = displayName(uri)
        label.setTextColor(0xFF2FA36B.toInt())
    }

    /**
     * @param build creates the application from the new id, the student, the listing and the room
     * @param documentNames labels of the documents the student picked (only names are stored)
     */
    protected fun submit(
        button: TextView,
        idleText: String,
        documentNames: List<String>,
        successMessage: String,
        build: (id: String, student: Student, listing: AdminListing, room: RoomConfig) -> StudentApplication
    ) {
        if (submitting) return
        val student = Session.student ?: run { Navigator.restart(this); return }
        val currentListing = listing ?: return
        val currentRoom = room ?: return
        if (currentListing.assignedLandlordId == null) {
            toast(getString(R.string.this_residence_has_no_landlord_assigned), long = true)
            return
        }

        if (SyncManager.hasQueuedApplication(this, student.id, currentListing.id)) {
            toast(getString(R.string.you_already_have_an_application_for), long = true)
            return
        }
        // Offline mode: save to the SQLite outbox; SyncManager sends it when the phone reconnects
        if (!SyncManager.isOnline(this)) {
            val application = build(ApplicationRepository.newId(), student, currentListing, currentRoom)
                .copy(documents = documentNames.map { AppDocument(it, null) })
            SyncManager.queueApplication(this, application)
            toast(getString(R.string.saved_offline_application), long = true)
            finish()
            return
        }

        setSubmitting(button, idleText, true)
        ApplicationRepository.hasActiveApplication(student.id, currentListing.id)
            .addOnSuccessListener { alreadyApplied ->
                if (alreadyApplied) {
                    setSubmitting(button, idleText, false)
                    toast(getString(R.string.you_already_have_an_application_for), long = true)
                    return@addOnSuccessListener
                }
                val application = build(ApplicationRepository.newId(), student, currentListing, currentRoom)
                ApplicationRepository.submit(application, documentNames)
                    .addOnSuccessListener {
                        toast(successMessage, long = true)
                        finish()
                    }
                    .addOnFailureListener { e ->
                        setSubmitting(button, idleText, false)
                        toast(AuthErrors.message(this, e), long = true)
                    }
            }
            .addOnFailureListener { e ->
                setSubmitting(button, idleText, false)
                toast(AuthErrors.message(this, e), long = true)
            }
    }

    private fun setSubmitting(button: TextView, idleText: String, value: Boolean) {
        submitting = value
        button.isEnabled = !value
        button.text = if (value) getString(R.string.submitting) else idleText
    }

    protected fun baseApplication(
        id: String,
        student: Student,
        listing: AdminListing,
        room: RoomConfig,
        type: ApplicationType
    ): StudentApplication {
        val now = System.currentTimeMillis()
        return StudentApplication(
            id = id,
            studentId = student.id,
            studentName = student.fullName,
            studentEmail = student.email,
            listingId = listing.id,
            accommodationName = listing.name,
            landlordId = listing.assignedLandlordId.orEmpty(),
            roomType = room.roomType,
            applicationType = type,
            dateApplied = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(now)),
            createdAtMillis = now,
            status = ApplicationStatus.SUBMITTED
        )
    }
}
