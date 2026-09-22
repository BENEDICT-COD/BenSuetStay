package com.mansuetdev.bensuetstay

import android.view.LayoutInflater
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemBookingBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val booking = bookings[position]
        val context = holder.itemView.context

        holder.binding.apply {
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
                    }
                }
            }

            btnConfirm.setOnClickListener { onConfirm(booking) }
            btnDecline.setOnClickListener { onDecline(booking) }
        }
    }

    override fun getItemCount() = bookings.size

    fun updateList(newList: List<Booking>) {
        bookings = newList
        notifyDataSetChanged()
    }
}