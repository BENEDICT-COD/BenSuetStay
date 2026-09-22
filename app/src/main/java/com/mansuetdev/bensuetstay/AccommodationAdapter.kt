package com.mansuetdev.bensuetstay

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.mansuetdev.bensuetstay.databinding.ItemAccommodationBinding

class AccommodationAdapter(private var accommodations: List<Accommodation>) :
    RecyclerView.Adapter<AccommodationAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemAccommodationBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAccommodationBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = accommodations[position]
        val context = holder.itemView.context

        val isNsfas = item.fundingType == FundingType.NSFAS

        holder.binding.apply {
            tvName.text = item.name
            tvLocation.text = item.location
            tvSpaces.text = context.getString(R.string.spaces_available, item.spacesAvailable)

            if (item.photoUris.isNotEmpty()) {
                try {
                    val uri = android.net.Uri.parse(item.photoUris.first())
                    ivPhoto.setImageURI(uri)
                } catch (e: Exception) {
                    ivPhoto.setImageResource(R.drawable.bg_image_placeholder)
                }
            } else {
                ivPhoto.setImageResource(R.drawable.bg_image_placeholder)
            }

            if (isNsfas) {
                tvBadge.text = "NSFAS Accepted"
                tvBadge.setBackgroundResource(R.drawable.bg_badge_nsfas)
                tvBadge.setTextColor(ContextCompat.getColor(context, R.color.brand_blue))
                tvPrice.text = item.priceLabel
            } else {
                tvBadge.text = "Self / Bursary Funded"
                tvBadge.setBackgroundResource(R.drawable.bg_badge_selffunded)
                tvBadge.setTextColor(ContextCompat.getColor(context, R.color.accent_green))
                tvPrice.text = item.priceLabel
                tvPrice.setTextColor(ContextCompat.getColor(context, R.color.accent_green))
            }

            chipGroupRooms.removeAllViews()
            item.roomTypes.forEach { type ->
                val chip = Chip(context).apply {
                    text = type
                    isCheckable = false
                    isClickable = false
                }
                chipGroupRooms.addView(chip)
            }
        }

        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = android.content.Intent(context, ListingDetailActivity::class.java).apply {
                putExtra("ACCOMMODATION_ID", item.id)
                putExtra("ACCOMMODATION_NAME", item.name)
            }
            context.startActivity(intent)
        }
    }

    override fun getItemCount() = accommodations.size

    fun updateList(newList: List<Accommodation>) {
        accommodations = newList
        notifyDataSetChanged()
    }
}