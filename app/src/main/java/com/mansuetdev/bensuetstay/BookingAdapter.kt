package com.mansuetdev.bensuetstay

import android.view.LayoutInflater
<<<<<<< HEAD
import android.view.View
=======
>>>>>>> d231b4d53e6f750ae3c0daf4f61493ce26eaf809
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.mansuetdev.bensuetstay.databinding.ItemBookingBinding

class BookingAdapter(
    private var bookings: List<Booking>,
    private val isLandlord: Boolean = false,
    private val onConfirm: (Booking) -> Unit = {},
    private val onDecline: (Booking) -> Unit = {}
) : RecyclerView.Adapter<BookingAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemBookingBinding) : RecyclerView.ViewHolder(binding.root)

<<<<<<< HEAD
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(ItemBookingBinding.inflate(LayoutInflater.from(parent.context), parent, false))
=======
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemBookingBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }
>>>>>>> d231b4d53e6f750ae3c0daf4f61493ce26eaf809

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val booking = bookings[position]
        val context = holder.itemView.context

        holder.binding.apply {
<<<<<<< HEAD
            // Landlords care who is coming; students care where they're going
            tvStudentName.text = if (isLandlord) booking.studentName else booking.accommodationName
            tvAccommodation.text = if (isLandlord) booking.accommodationName else context.getString(R.string.viewing_request)
            tvDateTime.text = booking.requestedDateTime

            // Reset everything first: these rows are recycled between statuses
            actionButtonsRow.visibility = View.GONE
            rejectionLayout.visibility = View.GONE

            when (booking.status) {
                BookingStatus.PENDING -> {
                    tvStatus.text = context.getString(R.string.pending)
                    tvStatus.setBackgroundResource(R.drawable.bg_badge_nsfas)
                    tvStatus.setTextColor(ContextCompat.getColor(context, R.color.accent_orange))
                    if (isLandlord) actionButtonsRow.visibility = View.VISIBLE
                }
                BookingStatus.CONFIRMED -> {
                    tvStatus.text = context.getString(R.string.confirmed)
                    tvStatus.setBackgroundResource(R.drawable.bg_badge_selffunded)
                    tvStatus.setTextColor(ContextCompat.getColor(context, R.color.accent_green))
                }
                BookingStatus.DECLINED -> {
                    tvStatus.text = context.getString(R.string.declined)
                    tvStatus.setBackgroundResource(R.drawable.bg_button_outlined_red)
                    tvStatus.setTextColor(ContextCompat.getColor(context, R.color.accent_red))
                    if (!booking.rejectionReason.isNullOrEmpty() || !booking.suggestedTime.isNullOrEmpty()) {
                        rejectionLayout.visibility = View.VISIBLE
                        tvRejectionReason.text = booking.rejectionReason ?: context.getString(R.string.no_reason_provided)
                        tvSuggestedTime.text = context.getString(R.string.suggested, booking.suggestedTime.orEmpty())
                        tvSuggestedTime.visibility = if (booking.suggestedTime.isNullOrEmpty()) View.GONE else View.VISIBLE
=======
            tvStudentName.text = booking.studentName
            tvAccommodation.text = booking.accommodationName
            tvDateTime.text = booking.requestedDateTime

            when (booking.status) {
                BookingStatus.PENDING -> {
                    tvStatus.text = "Pending"
                    tvStatus.setBackgroundResource(R.drawable.bg_badge_nsfas)
                    tvStatus.setTextColor(ContextCompat.getColor(context, R.color.accent_orange))
                    actionButtonsRow.visibility = if (isLandlord) android.view.View.VISIBLE else android.view.View.GONE
                }
                BookingStatus.CONFIRMED -> {
                    tvStatus.text = "Confirmed"
                    tvStatus.setBackgroundResource(R.drawable.bg_badge_selffunded)
                    tvStatus.setTextColor(ContextCompat.getColor(context, R.color.accent_green))
                    actionButtonsRow.visibility = android.view.View.GONE
                }
                BookingStatus.DECLINED -> {
                    tvStatus.text = "Declined"
                    tvStatus.setBackgroundResource(R.drawable.bg_button_outlined_red)
                    tvStatus.setTextColor(ContextCompat.getColor(context, R.color.accent_red))
                    actionButtonsRow.visibility = android.view.View.GONE
                    
                    if (!booking.rejectionReason.isNullOrEmpty() || !booking.suggestedTime.isNullOrEmpty()) {
                        rejectionLayout.visibility = android.view.View.VISIBLE
                        tvRejectionReason.text = booking.rejectionReason ?: "No reason provided"
                        tvSuggestedTime.text = if (!booking.suggestedTime.isNullOrEmpty()) 
                            "Suggested: ${booking.suggestedTime}" else ""
                        tvSuggestedTime.visibility = if (booking.suggestedTime.isNullOrEmpty()) 
                            android.view.View.GONE else android.view.View.VISIBLE
                    } else {
                        rejectionLayout.visibility = android.view.View.GONE
>>>>>>> d231b4d53e6f750ae3c0daf4f61493ce26eaf809
                    }
                }
            }

<<<<<<< HEAD
            if (booking.pendingSync) {
                tvStatus.text = context.getString(R.string.waiting_to_sync)
                tvStatus.setBackgroundResource(R.drawable.bg_badge_nsfas)
                tvStatus.setTextColor(ContextCompat.getColor(context, R.color.text_muted))
                actionButtonsRow.visibility = View.GONE
            }

=======
>>>>>>> d231b4d53e6f750ae3c0daf4f61493ce26eaf809
            btnConfirm.setOnClickListener { onConfirm(booking) }
            btnDecline.setOnClickListener { onDecline(booking) }
        }
    }

    override fun getItemCount() = bookings.size

    fun updateList(newList: List<Booking>) {
<<<<<<< HEAD
        bookings = newList.toList()
        notifyDataSetChanged()
    }
}
=======
        bookings = newList
        notifyDataSetChanged()
    }
}
>>>>>>> d231b4d53e6f750ae3c0daf4f61493ce26eaf809
