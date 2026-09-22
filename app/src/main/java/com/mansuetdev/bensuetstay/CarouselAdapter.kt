package com.mansuetdev.bensuetstay

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class CarouselAdapter(private val photoUris: List<String>) :
    RecyclerView.Adapter<CarouselAdapter.ViewHolder>() {

    class ViewHolder(val binding: com.mansuetdev.bensuetstay.databinding.ItemCarouselImageBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = com.mansuetdev.bensuetstay.databinding.ItemCarouselImageBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (photoUris.isNotEmpty()) {
            try {
                val uri = android.net.Uri.parse(photoUris[position])
                holder.binding.ivCarousel.setImageURI(uri)
            } catch (e: Exception) {
                holder.binding.ivCarousel.setImageResource(R.drawable.bg_image_placeholder)
            }
        } else {
            holder.binding.ivCarousel.setImageResource(R.drawable.bg_image_placeholder)
        }
    }

    override fun getItemCount(): Int = if (photoUris.isNotEmpty()) photoUris.size else 1
}