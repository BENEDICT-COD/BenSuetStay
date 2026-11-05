package com.mansuetdev.bensuetstay

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
<<<<<<< HEAD
import com.mansuetdev.bensuetstay.databinding.ActivityBookViewingBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class BookViewingActivity : BaseActivity() {

    private lateinit var binding: ActivityBookViewingBinding
    private val calendar = Calendar.getInstance()
    private var dateChosen = false
    private var listing: AdminListing? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!requireRole(Role.STUDENT)) return
        binding = ActivityBookViewingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.btnBack.setOnClickListener { finish() }
        binding.btnConfirmBooking.isEnabled = false

        val listingId = intent.getStringExtra(ListingDetailActivity.EXTRA_LISTING_ID).orEmpty()
        ListingRepository.get(
            listingId,
            onResult = { found ->
                if (found == null) {
                    toast(getString(R.string.this_listing_is_no_longer_available)); finish()
                } else {
                    listing = found
                    binding.tvAccommodationName.text = found.name
                    binding.btnConfirmBooking.isEnabled = true
                }
            },
            onError = { e -> toast(AuthErrors.message(this, e)); finish() }
        )

        binding.etDateTime.setOnClickListener { showDatePicker() }
        binding.btnConfirmBooking.setOnClickListener { confirmBooking() }
    }

    private fun confirmBooking() {
        val current = listing ?: return
        val student = Session.student ?: run { Navigator.restart(this); return }
        val landlordId = current.assignedLandlordId
        when {
            !dateChosen -> { toast(getString(R.string.please_select_a_date_and_time)); return }
            calendar.timeInMillis <= System.currentTimeMillis() -> { toast(getString(R.string.choose_a_time_in_the_future)); return }
            landlordId == null -> {
                toast(getString(R.string.this_residence_has_no_landlord_assigned_2), long = true); return
            }
        }

        val booking = Booking(
            id = BookingRepository.newId(),
            studentId = student.id,
            studentName = student.fullName,
            listingId = current.id,
            accommodationName = current.name,
            landlordId = landlordId.orEmpty(),
            requestedDateTime = binding.etDateTime.text.toString(),
            requestedAtMillis = calendar.timeInMillis,
            status = BookingStatus.PENDING
        )

        // Offline mode: save to the SQLite outbox; SyncManager sends it when the phone reconnects
        if (!SyncManager.isOnline(this)) {
            SyncManager.queueBooking(this, booking)
            toast(getString(R.string.saved_offline_booking), long = true)
=======
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.mansuetdev.bensuetstay.databinding.ActivityBookViewingBinding
import java.text.SimpleDateFormat
import java.util.*

class BookViewingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBookViewingBinding
    private val calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBookViewingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val listingId = intent.getStringExtra("LISTING_ID")
        val listing = ListingRepository.findById(listingId ?: "")

        if (listing == null) {
>>>>>>> d231b4d53e6f750ae3c0daf4f61493ce26eaf809
            finish()
            return
        }

<<<<<<< HEAD
        binding.btnConfirmBooking.isEnabled = false
        BookingRepository.create(booking)
            .addOnSuccessListener {
                toast(getString(R.string.viewing_requested_the_landlord_will_confirm), long = true)
                finish()
            }
            .addOnFailureListener { e ->
                if (e is com.google.firebase.firestore.FirebaseFirestoreException &&
                    e.code == com.google.firebase.firestore.FirebaseFirestoreException.Code.UNAVAILABLE
                ) {
                    SyncManager.queueBooking(this, booking)
                    toast(getString(R.string.saved_offline_booking), long = true)
                    finish()
                    return@addOnFailureListener
                }
                binding.btnConfirmBooking.isEnabled = true
                toast(AuthErrors.message(this, e), long = true)
            }
    }

    private fun showDatePicker() {
        val dialog = DatePickerDialog(
=======
        binding.tvAccommodationName.text = listing.name

        binding.btnBack.setOnClickListener { finish() }

        binding.etDateTime.setOnClickListener {
            showDatePicker()
        }

        binding.btnConfirmBooking.setOnClickListener {
            val dateTime = binding.etDateTime.text.toString()
            if (dateTime.isBlank()) {
                Toast.makeText(this, "Please select a date and time", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val student = StudentRepository.loggedInStudent
            if (student == null) {
                Toast.makeText(this, "Please login to book a viewing", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val booking = Booking(
                id = UUID.randomUUID().toString(),
                studentName = student.fullName,
                accommodationName = listing.name,
                requestedDateTime = dateTime,
                status = BookingStatus.PENDING
            )

            BookingRepository.addBooking(booking)
            Toast.makeText(this, "Viewing requested successfully!", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private fun showDatePicker() {
        DatePickerDialog(
>>>>>>> d231b4d53e6f750ae3c0daf4f61493ce26eaf809
            this,
            { _, year, month, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                showTimePicker()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
<<<<<<< HEAD
        )
        dialog.datePicker.minDate = System.currentTimeMillis() - 1000
        dialog.show()
=======
        ).show()
>>>>>>> d231b4d53e6f750ae3c0daf4f61493ce26eaf809
    }

    private fun showTimePicker() {
        TimePickerDialog(
            this,
            { _, hourOfDay, minute ->
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                calendar.set(Calendar.MINUTE, minute)
<<<<<<< HEAD
                calendar.set(Calendar.SECOND, 0)
                dateChosen = true
                val format = SimpleDateFormat("EEE, dd MMM yyyy \u00b7 HH:mm", Locale.getDefault())
                binding.etDateTime.setText(format.format(calendar.time))
                if (calendar.timeInMillis <= System.currentTimeMillis()) toast(getString(R.string.that_time_has_already_passed))
=======
                val format = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
                binding.etDateTime.setText(format.format(calendar.time))
>>>>>>> d231b4d53e6f750ae3c0daf4f61493ce26eaf809
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        ).show()
    }
}
