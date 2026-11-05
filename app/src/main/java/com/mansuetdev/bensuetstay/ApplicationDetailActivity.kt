package com.mansuetdev.bensuetstay

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import androidx.core.content.ContextCompat
import com.mansuetdev.bensuetstay.databinding.ActivityApplicationDetailBinding
import com.mansuetdev.bensuetstay.databinding.ItemDocumentRowBinding
import com.mansuetdev.bensuetstay.databinding.ItemFieldRowBinding

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
    }

    private fun addField(label: String, value: String) {
        val row = ItemFieldRowBinding.inflate(LayoutInflater.from(this), binding.fieldsContainer, false)
        row.tvFieldLabel.text = label
        row.tvFieldValue.text = value
        binding.fieldsContainer.addView(row.root)
    }

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
            }
            binding.documentsContainer.addView(row.root)
        }
    }

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
