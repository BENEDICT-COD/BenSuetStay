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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(ItemAdminListingBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val listing = listings[position]
        val context = holder.itemView.context
        holder.binding.apply {
            tvListingName.text = listing.name
            tvListingLocation.text = listing.location
            tvListingLandlord.text = context.getString(R.string.landlord, listing.assignedLandlordName ?: context.getString(R.string.unassigned))
            val highlights = mutableListOf<String>()
            if (listing.hasTransport) highlights.add(context.getString(R.string.transport))
            if (listing.isFurnished) highlights.add(context.getString(R.string.furnished))
            if (listing.isPrepaidElectricity) highlights.add(context.getString(R.string.prepaid_elec))
            val extra = if (highlights.isNotEmpty()) " \u00b7 " + highlights.joinToString(", ") else ""
            tvRoomSummary.text =
                context.getString(R.string.room_type_s_spaces_photos, listing.rooms.size, listing.totalSpaces, listing.photoUris.size, extra)
            btnEdit.setOnClickListener { onEdit(listing) }
            btnDelete.setOnClickListener { onDelete(listing) }
        }
    }

    override fun getItemCount() = listings.size

    /** Takes a copy so later changes to the caller's list can't desync the RecyclerView. */
    fun updateList(newList: List<AdminListing>) {
        listings = newList.toList()
        notifyDataSetChanged()
    }
}
