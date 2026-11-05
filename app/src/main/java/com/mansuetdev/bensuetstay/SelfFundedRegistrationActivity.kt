package com.mansuetdev.bensuetstay

import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import com.mansuetdev.bensuetstay.databinding.ActivitySelfFundedRegistrationBinding

class SelfFundedRegistrationActivity : ApplicationFormActivity() {

    private lateinit var binding: ActivitySelfFundedRegistrationBinding
    private var idDocument: Uri? = null
    private var registrationProof: Uri? = null

    private val pickId = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            idDocument = uri
            showPickedDocument(binding.tvIdStatusLabel, uri)
            markDone(binding.ivIdStatusIcon)
        }
    }

    private val pickProof = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            registrationProof = uri
            showPickedDocument(binding.tvPorStatusLabel, uri)
            markDone(binding.ivPorStatusIcon)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!requireRole(Role.STUDENT)) return
        binding = ActivitySelfFundedRegistrationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.btnBack.setOnClickListener { finish() }

        binding.btnUploadId.setOnClickListener { pickId.launch(ApplicationForm.DOCUMENT_TYPES) }
        binding.btnUploadPor.setOnClickListener { pickProof.launch(ApplicationForm.DOCUMENT_TYPES) }
        binding.btnSubmit.isEnabled = false

        loadListingAndRoom { listing, room ->
            binding.tvAccommodationName.text = listing.name
            binding.tvRoomType.text = "${Labels.roomType(this, room.roomType)} \u00b7 ${Labels.roomPrice(this, room)}"
            binding.btnSubmit.isEnabled = true
            binding.btnSubmit.setOnClickListener { onSubmit() }
        }
    }

    private fun markDone(icon: android.widget.ImageView) {
        icon.setImageResource(R.drawable.ic_calendar_check)
        icon.imageTintList = ColorStateList.valueOf(0xFF2FA36B.toInt())
    }

    private fun onSubmit() {
        val kinName = binding.etKinName.text.toString().trim()
        val kinPhone = binding.etKinPhone.text.toString().trim()

        if (kinName.isEmpty()) {
            binding.etKinName.error = getString(R.string.enter_your_next_of_kin_s); return
        }
        if (!Validators.isValidSaPhone(kinPhone)) {
            binding.etKinPhone.error = getString(R.string.enter_a_valid_sa_number_e); return
        }
        val id = idDocument ?: run { toast(getString(R.string.please_upload_a_copy_of_your)); return }
        val proof = registrationProof ?: run { toast(getString(R.string.please_upload_your_proof_of_registration)); return }

        submit(
            button = binding.btnSubmit,
            idleText = getString(R.string.submit_registration),
            documentNames = listOf(
                getString(R.string.id_copy, displayName(id)),
                getString(R.string.proof_of_registration, displayName(proof))
            ),
            successMessage = getString(R.string.registration_submitted_track_it_under_settings)
        ) { appId, student, listing, room ->
            baseApplication(appId, student, listing, room, ApplicationType.SELF_FUNDED_REGISTRATION).copy(
                nextOfKinName = kinName,
                nextOfKinPhone = kinPhone
            )
        }
    }
}
