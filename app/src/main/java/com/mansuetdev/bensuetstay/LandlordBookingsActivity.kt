package com.mansuetdev.bensuetstay

import android.os.Bundle
<<<<<<< HEAD
import android.view.View
import android.widget.EditText
import androidx.recyclerview.widget.LinearLayoutManager
import com.mansuetdev.bensuetstay.databinding.ActivityLandlordBookingsBinding

/** Viewing requests for this landlord's listings only. */
class LandlordBookingsActivity : BaseActivity() {

    private lateinit var binding: ActivityLandlordBookingsBinding
    private lateinit var adapter: BookingAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!requireRole(Role.LANDLORD)) return
        binding = ActivityLandlordBookingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.btnBack.setOnClickListener { finish() }

        adapter = BookingAdapter(
            bookings = emptyList(),
            isLandlord = true,
            onConfirm = { booking -> confirmBooking(booking) },
            onDecline = { booking -> showRejectionDialog(booking) }
        )
        binding.recyclerBookings.layoutManager = LinearLayoutManager(this)
        binding.recyclerBookings.adapter = adapter
    }

    override fun onStart() {
        super.onStart()
        val uid = Session.landlord?.id ?: return
        BookingRepository.listenForLandlord(this, uid,
            onChange = { bookings ->
                // Pending first, then by date
                adapter.updateList(bookings.sortedWith(compareBy({ it.status != BookingStatus.PENDING }, { it.requestedAtMillis })))
                showEmpty(if (bookings.isEmpty()) getString(R.string.no_viewing_requests_yet) else null)
            },
            onError = { e -> showEmpty(AuthErrors.message(this, e)) }
        )
    }

    private fun showEmpty(message: String?) {
        binding.tvEmpty.visibility = if (message == null) View.GONE else View.VISIBLE
        binding.tvEmpty.text = message.orEmpty()
    }

    private fun confirmBooking(booking: Booking) {
        BookingRepository.respond(booking, BookingStatus.CONFIRMED)
            .addOnSuccessListener { toast(getString(R.string.viewing_confirmed_for, booking.studentName)) }
            .addOnFailureListener { e -> toast(AuthErrors.message(this, e), long = true) }
    }

    private fun showRejectionDialog(booking: Booking) {
        val view = layoutInflater.inflate(R.layout.dialog_reject_booking, null)
        val etReason = view.findViewById<EditText>(R.id.etRejectionReason)
        val etTime = view.findViewById<EditText>(R.id.etSuggestedTime)

        showFormDialog(title = getString(R.string.decline_viewing), view = view, positive = getString(R.string.submit)) { dialog ->
            val reason = etReason.text.toString().trim()
            if (reason.isEmpty()) {
                etReason.error = getString(R.string.please_give_the_student_a_reason)
                return@showFormDialog
            }
            val suggested = etTime.text.toString().trim().ifEmpty { null }
            BookingRepository.respond(booking, BookingStatus.DECLINED, reason, suggested)
                .addOnSuccessListener { dialog.dismiss(); toast(getString(R.string.viewing_declined)) }
                .addOnFailureListener { e -> toast(AuthErrors.message(this, e), long = true) }
        }
    }
}
=======
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.mansuetdev.bensuetstay.databinding.ActivityLandlordBookingsBinding

class LandlordBookingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLandlordBookingsBinding
    private lateinit var adapter: BookingAdapter
    private var bookings = mutableListOf<Booking>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLandlordBookingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }

        bookings = BookingRepository.bookings.toMutableList()

        adapter = BookingAdapter(
            bookings = bookings,
            isLandlord = true,
            onConfirm = { booking -> updateStatus(booking, BookingStatus.CONFIRMED) },
            onDecline = { booking -> showRejectionDialog(booking) }
        )

        binding.recyclerBookings.layoutManager = LinearLayoutManager(this)
        binding.recyclerBookings.adapter = adapter

        // Listen for real-time changes from Firestore
        BookingRepository.addBookingListener {
            bookings.clear()
            bookings.addAll(BookingRepository.bookings)
            adapter.updateList(bookings)
        }
    }

    private fun updateStatus(
        booking: Booking, 
        newStatus: BookingStatus, 
        reason: String? = null, 
        suggestedTime: String? = null
    ) {
        val index = bookings.indexOfFirst { it.id == booking.id }
        if (index != -1) {
            val updated = bookings[index].copy(
                status = newStatus,
                rejectionReason = reason,
                suggestedTime = suggestedTime
            )
            bookings[index] = updated
            BookingRepository.updateStatus(booking.id, newStatus, reason, suggestedTime)
            adapter.updateList(bookings)
        }
    }

    private fun showRejectionDialog(booking: Booking) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_reject_booking, null)
        val etReason = dialogView.findViewById<android.widget.EditText>(R.id.etRejectionReason)
        val etTime = dialogView.findViewById<android.widget.EditText>(R.id.etSuggestedTime)

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Reject Booking")
            .setView(dialogView)
            .setPositiveButton("Submit") { _, _ ->
                val reason = etReason.text.toString().trim()
                val time = etTime.text.toString().trim()
                
                if (reason.isEmpty()) {
                    Toast.makeText(this, "Please provide a reason", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                
                updateStatus(booking, BookingStatus.DECLINED, reason, time)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun sampleBookings(): List<Booking> = listOf(
        Booking("1", "Sipho Ndlovu", "Varsity Heights Residence", "Wed, 17 Sep \u00b7 10:00 AM", BookingStatus.PENDING),
        Booking("2", "Amara Dlamini", "Varsity Heights Residence", "Wed, 17 Sep \u00b7 2:00 PM", BookingStatus.PENDING),
        Booking("3", "Thabo Mokoena", "Riverside Residence", "Thu, 18 Sep \u00b7 11:30 AM", BookingStatus.CONFIRMED),
        Booking("4", "Lindiwe Zulu", "Riverside Residence", "Fri, 19 Sep \u00b7 9:00 AM", BookingStatus.PENDING)
    )
}
>>>>>>> d231b4d53e6f750ae3c0daf4f61493ce26eaf809
