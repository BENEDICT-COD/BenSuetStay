package com.mansuetdev.bensuetstay

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mansuetdev.bensuetstay.databinding.ItemLandlordBinding

class LandlordAdapter(
    private var landlords: List<LandlordAccount>,
    private val onDelete: (LandlordAccount) -> Unit,
    private val onResetPassword: (LandlordAccount) -> Unit
) : RecyclerView.Adapter<LandlordAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemLandlordBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(ItemLandlordBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val landlord = landlords[position]
        val context = holder.itemView.context
        holder.binding.apply {
            tvLandlordName.text = landlord.name
            tvLandlordEmail.text = landlord.email
            tvLandlordPhone.text = landlord.phone
            // Counted from the live listings rather than a stored number that can drift
            val count = ListingRepository.listingsForLandlord(landlord.id).size
            val status = if (landlord.mustChangePassword) context.getString(R.string.hasn_t_logged_in_yet) else ""
            tvListingsCount.text = "$count listing(s)$status"
            btnResetPassword.contentDescription = context.getString(R.string.send_password_reset_email)
            btnResetPassword.setOnClickListener { onResetPassword(landlord) }
            btnDeleteLandlord.setOnClickListener { onDelete(landlord) }
        }
    }

    override fun getItemCount() = landlords.size

    fun updateList(newList: List<LandlordAccount>) {
        landlords = newList.toList()
        notifyDataSetChanged()
    }
}
