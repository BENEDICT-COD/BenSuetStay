package com.mansuetdev.bensuetstay

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.mansuetdev.bensuetstay.databinding.ActivityStudentBookingsBinding

class StudentBookingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStudentBookingsBinding
    private lateinit var adapter: BookingAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStudentBookingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        loadBookings()
    }

    private fun setupUI() {
        binding.btnBack.setOnClickListener { finish() }

        adapter = BookingAdapter(
            bookings = emptyList(),
            isLandlord = false
        )
        binding.recyclerBookings.layoutManager = LinearLayoutManager(this)
        binding.recyclerBookings.adapter = adapter
    }

    private fun loadBookings() {
        val student = StudentRepository.loggedInStudent
        if (student != null) {
            val studentBookings = BookingRepository.getBookingsForStudent(student.fullName)
            adapter.updateList(studentBookings)
        }
    }
}