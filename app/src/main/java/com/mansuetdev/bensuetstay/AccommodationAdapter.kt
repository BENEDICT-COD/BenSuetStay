package com.mansuetdev.bensuetstay

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.mansuetdev.bensuetstay.databinding.ItemAccommodationBinding

class AccommodationAdapter(private var accommodations: List<Accommodation>) :
    RecyclerView.Adapter<AccommodationAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemAccommodationBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(ItemAccommodationBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = accommodations[position]
        val context = holder.itemView.context
        val isNsfas = item.fundingType == FundingType.NSFAS

        holder.binding.apply {
            tvName.text = item.name
            tvLocation.text = item.location
            tvSpaces.text = context.getString(R.string.spaces_available, item.spacesAvailable)
            Images.load(ivPhoto, item.photoUris.firstOrNull(), R.drawable.bg_image_placeholder)

            // Set every colour in both branches: these views are recycled between card types
            tvBadge.text = if (isNsfas) context.getString(R.string.nsfas_accepted) else context.getString(R.string.self_bursary_funded_2)
            tvBadge.setBackgroundResource(if (isNsfas) R.drawable.bg_badge_nsfas else R.drawable.bg_badge_selffunded)
            val accent = ContextCompat.getColor(context, if (isNsfas) R.color.brand_blue else R.color.accent_green)
            tvBadge.setTextColor(accent)
            tvPrice.text = Labels.cardPrice(context, item)
            tvPrice.setTextColor(accent)

            chipGroupRooms.removeAllViews()
            item.roomTypes.forEach { type ->
                chipGroupRooms.addView(Chip(context).apply {
                    text = Labels.roomType(context, type)
                    isCheckable = false
                    isClickable = false
                })
            }
        }

        holder.itemView.setOnClickListener {
            context.startActivity(
                Intent(context, ListingDetailActivity::class.java)
                    .putExtra(ListingDetailActivity.EXTRA_LISTING_ID, item.listingId)
            )
        }
    }

    override fun getItemCount() = accommodations.size

    fun updateList(newList: List<Accommodation>) {
        accommodations = newList
        notifyDataSetChanged()
    }
}
