package com.mansuetdev.bensuetstay

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mansuetdev.bensuetstay.databinding.ItemAdminListingBinding

class AdminListingAdapter(
    private var listings: List<AdminListing>,
    private val onEdit: (AdminListing) -> Unit,
    private val onDelete: (AdminListing) -> Unit
) : RecyclerView.Adapter<AdminListingAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemAdminListingBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAdminListingBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val listing = listings[position]
        holder.binding.apply {
            tvListingName.text = listing.name
            tvListingLocation.text = listing.location
            tvListingLandlord.text = "Landlord: ${listing.assignedLandlordName ?: "Unassigned"}"
            val highlights = mutableListOf<String>()
            if (listing.hasTransport) highlights.add("Transport")
            if (listing.isFurnished) highlights.add("Furnished")
            if (listing.isPrepaidElectricity) highlights.add("Prepaid Elec")
            val highlightsText = if (highlights.isNotEmpty()) " \u00b7 " + highlights.joinToString(", ") else ""
            tvRoomSummary.text = "${listing.rooms.size} room type(s) \u00b7 ${listing.photoCount} photos${highlightsText}"

            btnEdit.setOnClickListener { onEdit(listing) }
            btnDelete.setOnClickListener { onDelete(listing) }
        }
    }

    override fun getItemCount() = listings.size

    fun updateList(newList: List<AdminListing>) {
        listings = newList
        notifyDataSetChanged()
    }
}