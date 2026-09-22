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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemLandlordBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val landlord = landlords[position]
        holder.binding.apply {
            tvLandlordName.text = landlord.name
            tvLandlordEmail.text = landlord.email
            tvLandlordPhone.text = landlord.phone
            tvListingsCount.text = "${landlord.listingsCount} listing(s)"

            btnResetPassword.setOnClickListener { onResetPassword(landlord) }
            btnDeleteLandlord.setOnClickListener { onDelete(landlord) }
        }
    }

    override fun getItemCount() = landlords.size

    fun updateList(newList: List<LandlordAccount>) {
        landlords = newList
        notifyDataSetChanged()
    }
}