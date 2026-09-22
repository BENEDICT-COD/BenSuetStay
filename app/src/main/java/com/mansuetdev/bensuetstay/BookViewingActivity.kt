package com.mansuetdev.bensuetstay

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
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
            finish()
            return
        }

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
        ).show()
    }

    private fun showTimePicker() {
        TimePickerDialog(
            this,
            { _, hourOfDay, minute ->
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                calendar.set(Calendar.MINUTE, minute)
                val format = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
                binding.etDateTime.setText(format.format(calendar.time))
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        ).show()
    }
}
