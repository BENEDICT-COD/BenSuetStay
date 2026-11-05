package com.mansuetdev.bensuetstay

<<<<<<< HEAD
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
=======
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.mansuetdev.bensuetstay.databinding.ActivityNsfasConfirmationBinding
import java.text.SimpleDateFormat
import java.util.*

class ConfirmNsfasActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNsfasConfirmationBinding

    private var isDocumentUploaded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNsfasConfirmationBinding.inflate(layoutInflater)
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

        binding.btnUploadNsfas.setOnClickListener {
            simulateDocumentUpload()
        }

        binding.btnSubmit.setOnClickListener {
            val idNum = binding.etIdNumber.text.toString()
            val studentNum = binding.etStudentNumber.text.toString()
            val campus = binding.etCampus.text.toString()

            if (idNum.isBlank() || studentNum.isBlank() || campus.isBlank()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!isDocumentUploaded) {
                Toast.makeText(this, "Please upload your NSFAS status document", Toast.LENGTH_SHORT).show()
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
                applicationType = ApplicationType.NSFAS_CONFIRMATION,
                dateApplied = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date()),
                status = ApplicationStatus.SUBMITTED,
                idNumber = idNum,
                studentNumber = studentNum,
                campus = campus,
                roomSelected = room.roomType,
                documents = listOf("NSFAS_Status.pdf")
            )

            ApplicationRepository.addApplication(application)
            Toast.makeText(this, "Confirmation submitted successfully!", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private fun simulateDocumentUpload() {
        binding.tvNsfasStatusLabel.text = "Uploading\u2026"
        binding.btnUploadNsfas.isEnabled = false
        
        binding.root.postDelayed({
            isDocumentUploaded = true
            binding.tvNsfasStatusLabel.text = "NSFAS_Status.pdf uploaded"
            binding.tvNsfasStatusLabel.setTextColor(0xFF2FA36B.toInt())
            binding.ivNsfasStatusIcon.setImageResource(R.drawable.ic_calendar_check)
            binding.ivNsfasStatusIcon.imageTintList = android.content.res.ColorStateList.valueOf(0xFF2FA36B.toInt())
            binding.btnUploadNsfas.isEnabled = true
            Toast.makeText(this, "Document uploaded", Toast.LENGTH_SHORT).show()
        }, 1500)
>>>>>>> d231b4d53e6f750ae3c0daf4f61493ce26eaf809
    }
}
