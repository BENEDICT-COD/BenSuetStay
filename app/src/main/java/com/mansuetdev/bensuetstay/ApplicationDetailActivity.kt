package com.mansuetdev.bensuetstay

import android.os.Bundle
import android.view.LayoutInflater
<<<<<<< HEAD
import android.view.View
import android.widget.EditText
=======
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
>>>>>>> d231b4d53e6f750ae3c0daf4f61493ce26eaf809
import androidx.core.content.ContextCompat
import com.mansuetdev.bensuetstay.databinding.ActivityApplicationDetailBinding
import com.mansuetdev.bensuetstay.databinding.ItemDocumentRowBinding
import com.mansuetdev.bensuetstay.databinding.ItemFieldRowBinding

<<<<<<< HEAD
/**
 * One application. Landlords review documents and approve/reject; students see the
 * outcome and the landlord's feedback (read-only).
 */
class ApplicationDetailActivity : BaseActivity() {

    companion object {
        const val EXTRA_APPLICATION_ID = "application_id"
    }

    private lateinit var binding: ActivityApplicationDetailBinding
    private var application: StudentApplication? = null
    private var deciding = false
    private val isLandlord get() = Session.role == Role.LANDLORD

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!requireRole(Role.LANDLORD, Role.STUDENT)) return
        binding = ActivityApplicationDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.btnBack.setOnClickListener { finish() }
        binding.actionButtonsRow.visibility = View.GONE
        binding.btnApprove.setOnClickListener { showDecisionDialog(ApplicationStatus.APPROVED) }
        binding.btnReject.setOnClickListener { showDecisionDialog(ApplicationStatus.DECLINED) }
    }

    override fun onStart() {
        super.onStart()
        val id = intent.getStringExtra(EXTRA_APPLICATION_ID) ?: run { finish(); return }
        ApplicationRepository.listenToOne(this, id,
            onChange = { app ->
                if (app == null) {
                    toast(getString(R.string.this_application_no_longer_exists))
                    finish()
                } else {
                    val firstLoad = application == null
                    application = app
                    bind(app)
                    // Opening a new application tells the student it has been seen
                    if (firstLoad && isLandlord && app.status == ApplicationStatus.SUBMITTED) {
                        ApplicationRepository.markViewed(app.id)
                    }
                }
            },
            onError = { e -> toast(AuthErrors.message(this, e)); finish() }
        )
    }

    private fun bind(app: StudentApplication) {
        binding.tvStudentName.text = if (isLandlord) app.studentName else app.accommodationName
        val roomLabel = Labels.roomType(this, app.roomType)
        binding.tvAccommodation.text = if (isLandlord) "${app.accommodationName} \u00b7 $roomLabel" else roomLabel
        binding.tvDateApplied.text = getString(R.string.applied, app.dateApplied)

        val isNsfas = app.applicationType == ApplicationType.NSFAS_CONFIRMATION
        binding.tvFundingBadge.text = Labels.funding(this, app.roomFundingType)
        binding.tvFundingBadge.setBackgroundResource(if (isNsfas) R.drawable.bg_badge_nsfas else R.drawable.bg_badge_selffunded)
        binding.tvFundingBadge.setTextColor(
            ContextCompat.getColor(this, if (isNsfas) R.color.brand_blue else R.color.accent_green)
        )

        val (label, bg, color) = ApplicationAdapter.statusStyle(this, app.status)
        binding.tvStatus.text = label
        binding.tvStatus.setBackgroundResource(bg)
        binding.tvStatus.setTextColor(ContextCompat.getColor(this, color))

        buildFields(app)
        buildDocuments(app)

        val undecided = app.status == ApplicationStatus.SUBMITTED || app.status == ApplicationStatus.VIEWED
        binding.actionButtonsRow.visibility = if (isLandlord && undecided) View.VISIBLE else View.GONE
=======
class ApplicationDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityApplicationDetailBinding
    private lateinit var application: StudentApplication

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityApplicationDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val appId = intent.getStringExtra("application_id") ?: run {
            finish()
            return
        }
        application = ApplicationRepository.findById(appId) ?: run {
            finish()
            return
        }

        binding.btnBack.setOnClickListener { finish() }

        bindHeader()
        buildFields()
        buildDocuments()
        bindActions()
    }

    private fun bindHeader() {
        binding.tvStudentName.text = application.studentName
        binding.tvAccommodation.text = application.accommodationName
        binding.tvDateApplied.text = "Applied ${application.dateApplied}"

        if (application.applicationType == ApplicationType.NSFAS_CONFIRMATION) {
            binding.tvFundingBadge.text = "NSFAS"
            binding.tvFundingBadge.setBackgroundResource(R.drawable.bg_badge_nsfas)
            binding.tvFundingBadge.setTextColor(ContextCompat.getColor(this, R.color.brand_blue))
        } else {
            binding.tvFundingBadge.text = "Self Funded"
            binding.tvFundingBadge.setBackgroundResource(R.drawable.bg_badge_selffunded)
            binding.tvFundingBadge.setTextColor(ContextCompat.getColor(this, R.color.accent_green))
        }

        when (application.status) {
            ApplicationStatus.SUBMITTED -> setStatusBadge("Submitted", R.drawable.bg_badge_nsfas, R.color.accent_orange)
            ApplicationStatus.VIEWED -> setStatusBadge("Viewed", R.drawable.bg_badge_nsfas, R.color.brand_blue)
            ApplicationStatus.APPROVED -> setStatusBadge("Approved", R.drawable.bg_badge_selffunded, R.color.accent_green)
            ApplicationStatus.DECLINED -> setStatusBadge("Declined", R.drawable.bg_badge_declined, R.color.accent_red)
        }
    }

    private fun setStatusBadge(text: String, bg: Int, colorRes: Int) {
        binding.tvStatus.text = text
        binding.tvStatus.setBackgroundResource(bg)
        binding.tvStatus.setTextColor(ContextCompat.getColor(this, colorRes))
>>>>>>> d231b4d53e6f750ae3c0daf4f61493ce26eaf809
    }

    private fun addField(label: String, value: String) {
        val row = ItemFieldRowBinding.inflate(LayoutInflater.from(this), binding.fieldsContainer, false)
        row.tvFieldLabel.text = label
        row.tvFieldValue.text = value
        binding.fieldsContainer.addView(row.root)
    }

<<<<<<< HEAD
    private fun buildFields(app: StudentApplication) {
        binding.fieldsContainer.removeAllViews()
        if (isLandlord) addField(getString(R.string.student_email), app.studentEmail.ifBlank { "\u2014" })
        addField(getString(R.string.room_selected), Labels.roomType(this, app.roomType).ifBlank { "\u2014" })
        if (app.applicationType == ApplicationType.NSFAS_CONFIRMATION) {
            addField(getString(R.string.id_number), app.idNumber ?: "\u2014")
            addField(getString(R.string.student_number), app.studentNumber ?: "\u2014")
            addField(getString(R.string.campus), app.campus ?: "\u2014")
        } else {
            addField(getString(R.string.next_of_kin), "${app.nextOfKinName ?: "\u2014"}  \u00b7  ${app.nextOfKinPhone ?: "\u2014"}")
        }
        if (!app.feedback.isNullOrBlank()) {
            addField(if (isLandlord) getString(R.string.your_feedback) else getString(R.string.landlord_feedback), app.feedback)
        } else if (!isLandlord) {
            addField(getString(R.string.landlord_feedback), getString(R.string.no_feedback_yet))
        }
    }

    private fun buildDocuments(app: StudentApplication) {
        binding.documentsContainer.removeAllViews()
        binding.documentsSection.visibility = if (app.documents.isEmpty()) View.GONE else View.VISIBLE
        app.documents.forEach { doc ->
            val row = ItemDocumentRowBinding.inflate(LayoutInflater.from(this), binding.documentsContainer, false)
            row.tvDocumentName.text = doc.name
            row.root.setOnClickListener {
                val url = doc.url
                when {
                    url == null -> toast(getString(R.string.only_the_file_name_is_stored), long = true)
                    !openUrl(url) -> toast(getString(R.string.no_app_on_this_device_can))
                }
=======
    private fun buildFields() {
        binding.fieldsContainer.removeAllViews()
        if (application.applicationType == ApplicationType.NSFAS_CONFIRMATION) {
            addField("ID Number", application.idNumber ?: "\u2014")
            addField("Student Number", application.studentNumber ?: "\u2014")
            addField("Campus", application.campus ?: "\u2014")
        } else {
            addField("Room Selected", application.roomSelected ?: "\u2014")
            addField("Next of Kin", "${application.nextOfKinName ?: "\u2014"}  \u00b7  ${application.nextOfKinPhone ?: "\u2014"}")
        }
    }

    private fun buildDocuments() {
        if (application.documents.isEmpty()) {
            binding.documentsSection.visibility = android.view.View.GONE
            return
        }
        binding.documentsSection.visibility = android.view.View.VISIBLE
        binding.documentsContainer.removeAllViews()
        application.documents.forEach { docName ->
            val row = ItemDocumentRowBinding.inflate(LayoutInflater.from(this), binding.documentsContainer, false)
            row.tvDocumentName.text = docName
            row.root.setOnClickListener {
                // Real document preview comes once Firebase Storage is connected
                Toast.makeText(this, "Opening $docName\u2026", Toast.LENGTH_SHORT).show()
>>>>>>> d231b4d53e6f750ae3c0daf4f61493ce26eaf809
            }
            binding.documentsContainer.addView(row.root)
        }
    }

<<<<<<< HEAD
    private fun showDecisionDialog(target: ApplicationStatus) {
        val app = application ?: return
        if (deciding) return
        val view = layoutInflater.inflate(R.layout.dialog_reject_booking, null)
        val etFeedback = view.findViewById<EditText>(R.id.etRejectionReason)
        view.findViewById<View>(R.id.etSuggestedTime).visibility = View.GONE
        etFeedback.hint = if (target == ApplicationStatus.APPROVED) {
            getString(R.string.move_in_date_what_to_bring)
        } else {
            getString(R.string.why_the_application_was_declined)
        }
        val approving = target == ApplicationStatus.APPROVED

        showFormDialog(
            title = if (approving) getString(R.string.approve_application) else getString(R.string.decline_application),
            view = view,
            positive = if (approving) getString(R.string.approve) else getString(R.string.decline)
        ) { dialog ->
            val feedback = etFeedback.text.toString().trim()
            if (feedback.isEmpty()) {
                etFeedback.error = getString(R.string.the_student_will_see_this_message)
                return@showFormDialog
            }
            deciding = true
            dialog.dismiss()
            ApplicationRepository.decide(app, target, feedback)
                .addOnSuccessListener {
                    deciding = false
                    toast(
                        if (approving) getString(R.string.approved_one_space_was_taken_off, Labels.roomType(this, app.roomType))
                        else getString(R.string.declined_the_student_can_see_your),
                        long = true
                    )
                }
                .addOnFailureListener { e ->
                    deciding = false
                    toast(AuthErrors.message(this, e), long = true)
                }
        }
    }
}
=======
    private fun bindActions() {
        val alreadyDecided = application.status == ApplicationStatus.APPROVED ||
                application.status == ApplicationStatus.DECLINED

        binding.actionButtonsRow.visibility =
            if (alreadyDecided) android.view.View.GONE else android.view.View.VISIBLE

        binding.btnApprove.setOnClickListener {
            showFeedbackDialog(ApplicationStatus.APPROVED)
        }
        binding.btnReject.setOnClickListener {
            showFeedbackDialog(ApplicationStatus.DECLINED)
        }
    }

    private fun showFeedbackDialog(targetStatus: ApplicationStatus) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_reject_booking, null)
        val etReason = dialogView.findViewById<android.widget.EditText>(R.id.etRejectionReason)
        val etTime = dialogView.findViewById<android.widget.EditText>(R.id.etSuggestedTime)
        
        etReason.hint = "Provide feedback/notes for the student..."
        etTime.visibility = android.view.View.GONE // Only needed for Bookings, not applications

        val title = if (targetStatus == ApplicationStatus.APPROVED) "Approve & Provide Notes" else "Reject & Provide Reason"

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle(title)
            .setView(dialogView)
            .setPositiveButton("Submit") { _, _ ->
                val feedbackText = etReason.text.toString().trim()
                if (feedbackText.isEmpty()) {
                    Toast.makeText(this, "Please provide some notes/feedback", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                
                ApplicationRepository.updateStatus(application.id, targetStatus, feedbackText)
                
                // Simulate email sending logic
                val actionWord = if (targetStatus == ApplicationStatus.APPROVED) "Approved" else "Declined"
                Toast.makeText(this, "Application $actionWord. Feedback sent to student email!", Toast.LENGTH_LONG).show()
                finish()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
>>>>>>> d231b4d53e6f750ae3c0daf4f61493ce26eaf809
