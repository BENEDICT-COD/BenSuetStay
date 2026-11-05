package com.mansuetdev.bensuetstay

<<<<<<< HEAD
import android.content.Intent
=======
>>>>>>> d231b4d53e6f750ae3c0daf4f61493ce26eaf809
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.mansuetdev.bensuetstay.databinding.ItemAccommodationBinding

class AccommodationAdapter(private var accommodations: List<Accommodation>) :
    RecyclerView.Adapter<AccommodationAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemAccommodationBinding) : RecyclerView.ViewHolder(binding.root)

<<<<<<< HEAD
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(ItemAccommodationBinding.inflate(LayoutInflater.from(parent.context), parent, false))
=======
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAccommodationBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }
>>>>>>> d231b4d53e6f750ae3c0daf4f61493ce26eaf809

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = accommodations[position]
        val context = holder.itemView.context
<<<<<<< HEAD
=======

>>>>>>> d231b4d53e6f750ae3c0daf4f61493ce26eaf809
        val isNsfas = item.fundingType == FundingType.NSFAS

        holder.binding.apply {
            tvName.text = item.name
            tvLocation.text = item.location
            tvSpaces.text = context.getString(R.string.spaces_available, item.spacesAvailable)
<<<<<<< HEAD
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
=======

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
>>>>>>> d231b4d53e6f750ae3c0daf4f61493ce26eaf809
            }
        }

        holder.itemView.setOnClickListener {
<<<<<<< HEAD
            context.startActivity(
                Intent(context, ListingDetailActivity::class.java)
                    .putExtra(ListingDetailActivity.EXTRA_LISTING_ID, item.listingId)
            )
=======
            val context = holder.itemView.context
            val intent = android.content.Intent(context, ListingDetailActivity::class.java).apply {
                putExtra("ACCOMMODATION_ID", item.id)
                putExtra("ACCOMMODATION_NAME", item.name)
            }
            context.startActivity(intent)
>>>>>>> d231b4d53e6f750ae3c0daf4f61493ce26eaf809
        }
    }

    override fun getItemCount() = accommodations.size

    fun updateList(newList: List<Accommodation>) {
        accommodations = newList
        notifyDataSetChanged()
    }
<<<<<<< HEAD
}
=======
}
>>>>>>> d231b4d53e6f750ae3c0daf4f61493ce26eaf809
