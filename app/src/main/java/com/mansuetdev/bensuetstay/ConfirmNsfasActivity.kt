package com.mansuetdev.bensuetstay

import android.net.Uri
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import com.mansuetdev.bensuetstay.databinding.ActivityNsfasConfirmationBinding

class ConfirmNsfasActivity : ApplicationFormActivity() {

    private lateinit var binding: ActivityNsfasConfirmationBinding
    private var nsfasDocument: Uri? = null

    private val pickDocument = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            nsfasDocument = uri
            showPickedDocument(binding.tvNsfasStatusLabel, uri)
            binding.ivNsfasStatusIcon.setImageResource(R.drawable.ic_calendar_check)
            binding.ivNsfasStatusIcon.imageTintList = android.content.res.ColorStateList.valueOf(0xFF2FA36B.toInt())
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!requireRole(Role.STUDENT)) return
        binding = ActivityNsfasConfirmationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.btnBack.setOnClickListener { finish() }

        binding.etCampus.setText(Session.student?.institution.orEmpty())
        binding.btnUploadNsfas.setOnClickListener { pickDocument.launch(ApplicationForm.DOCUMENT_TYPES) }
        binding.btnSubmit.isEnabled = false

        loadListingAndRoom { listing, room ->
            binding.tvAccommodationName.text = listing.name
            binding.tvRoomType.text = Labels.roomType(this, room.roomType)
            binding.btnSubmit.isEnabled = true
            binding.btnSubmit.setOnClickListener { onSubmit() }
        }
    }

    private fun onSubmit() {
        val idNumber = binding.etIdNumber.text.toString().trim()
        val studentNumber = binding.etStudentNumber.text.toString().trim()
        val campus = binding.etCampus.text.toString().trim()

        when {
            !Validators.isValidSaId(idNumber) -> {
                binding.etIdNumber.error = getString(R.string.enter_a_valid_13_digit_south); return
            }
            studentNumber.isEmpty() -> {
                binding.etStudentNumber.error = getString(R.string.enter_your_student_number); return
            }
            campus.isEmpty() -> {
                binding.etCampus.error = getString(R.string.enter_your_campus); return
            }
        }
        val document = nsfasDocument ?: run {
            toast(getString(R.string.please_upload_your_nsfas_approval_status)); return
        }

        submit(
            button = binding.btnSubmit,
            idleText = getString(R.string.submit_confirmation),
            documentNames = listOf(getString(R.string.nsfas_status, displayName(document))),
            successMessage = getString(R.string.confirmation_submitted_track_it_under_settings)
        ) { id, student, listing, room ->
            baseApplication(id, student, listing, room, ApplicationType.NSFAS_CONFIRMATION).copy(
                idNumber = idNumber,
                studentNumber = studentNumber,
                campus = campus
            )
        }
    }
}
