package com.mansuetdev.bensuetstay

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.mansuetdev.bensuetstay.databinding.ActivityApplicationDetailBinding
import com.mansuetdev.bensuetstay.databinding.ItemDocumentRowBinding
import com.mansuetdev.bensuetstay.databinding.ItemFieldRowBinding

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
    }

    private fun addField(label: String, value: String) {
        val row = ItemFieldRowBinding.inflate(LayoutInflater.from(this), binding.fieldsContainer, false)
        row.tvFieldLabel.text = label
        row.tvFieldValue.text = value
        binding.fieldsContainer.addView(row.root)
    }

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
            }
            binding.documentsContainer.addView(row.root)
        }
    }

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