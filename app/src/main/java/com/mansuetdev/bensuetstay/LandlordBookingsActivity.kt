package com.mansuetdev.bensuetstay

import android.os.Bundle
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
