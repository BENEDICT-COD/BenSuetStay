package com.mansuetdev.bensuetstay

import android.view.LayoutInflater
import android.view.View
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(ItemBookingBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val booking = bookings[position]
        val context = holder.itemView.context

        holder.binding.apply {
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
                    }
                }
            }

            if (booking.pendingSync) {
                tvStatus.text = context.getString(R.string.waiting_to_sync)
                tvStatus.setBackgroundResource(R.drawable.bg_badge_nsfas)
                tvStatus.setTextColor(ContextCompat.getColor(context, R.color.text_muted))
                actionButtonsRow.visibility = View.GONE
            }

            btnConfirm.setOnClickListener { onConfirm(booking) }
            btnDecline.setOnClickListener { onDecline(booking) }
        }
    }

    override fun getItemCount() = bookings.size

    fun updateList(newList: List<Booking>) {
        bookings = newList.toList()
        notifyDataSetChanged()
    }
}
