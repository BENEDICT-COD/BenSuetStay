package com.mansuetdev.bensuetstay

<<<<<<< HEAD
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
=======
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.mansuetdev.bensuetstay.databinding.ActivitySelfFundedRegistrationBinding
import java.text.SimpleDateFormat
import java.util.*

class SelfFundedRegistrationActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySelfFundedRegistrationBinding

    private var isIdUploaded = false
    private var isPorUploaded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySelfFundedRegistrationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val listingId = intent.getStringExtra("LISTING_ID")
        val roomIndex = intent.getIntExtra("ROOM_INDEX", -1)
        val listing = ListingRepository.findById(listingId ?: "")

        if (listing == null || roomIndex == -1) {
            finish()
            return
        }

        val room = listing.rooms[roomIndex]
        binding.tvAccommodationName.text = listing.name
        binding.tvRoomType.text = room.roomType

        binding.btnBack.setOnClickListener { finish() }

        binding.btnUploadId.setOnClickListener {
            simulateUpload(true)
        }

        binding.btnUploadPor.setOnClickListener {
            simulateUpload(false)
        }

        binding.btnSubmit.setOnClickListener {
            val kinName = binding.etKinName.text.toString()
            val kinPhone = binding.etKinPhone.text.toString()

            if (kinName.isBlank() || kinPhone.isBlank()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!isIdUploaded || !isPorUploaded) {
                Toast.makeText(this, "Please upload all required documents", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val student = StudentRepository.loggedInStudent
            if (student == null) {
                Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val application = StudentApplication(
                id = UUID.randomUUID().toString(),
                studentName = student.fullName,
                accommodationName = listing.name,
                applicationType = ApplicationType.SELF_FUNDED_REGISTRATION,
                dateApplied = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date()),
                status = ApplicationStatus.SUBMITTED,
                nextOfKinName = kinName,
                nextOfKinPhone = kinPhone,
                roomSelected = room.roomType,
                documents = listOf("ID_Copy.pdf", "Proof_of_Registration.pdf")
            )

            ApplicationRepository.applications.add(application)
            Toast.makeText(this, "Registration submitted successfully!", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private fun simulateUpload(isId: Boolean) {
        val label = if (isId) binding.tvIdStatusLabel else binding.tvPorStatusLabel
        val icon = if (isId) binding.ivIdStatusIcon else binding.ivPorStatusIcon
        val btn = if (isId) binding.btnUploadId else binding.btnUploadPor
        
        label.text = "Uploading\u2026"
        btn.isEnabled = false
        
        binding.root.postDelayed({
            if (isId) isIdUploaded = true else isPorUploaded = true
            label.text = if (isId) "ID_Copy.pdf uploaded" else "Proof_of_Registration.pdf uploaded"
            label.setTextColor(0xFF2FA36B.toInt())
            icon.setImageResource(R.drawable.ic_calendar_check)
            icon.imageTintList = android.content.res.ColorStateList.valueOf(0xFF2FA36B.toInt())
            btn.isEnabled = true
            Toast.makeText(this, "Document uploaded", Toast.LENGTH_SHORT).show()
        }, 1200)
    }
>>>>>>> d231b4d53e6f750ae3c0daf4f61493ce26eaf809
}
