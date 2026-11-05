package com.mansuetdev.bensuetstay

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mansuetdev.bensuetstay.databinding.ItemCarouselImageBinding

class CarouselAdapter(private val photoUris: List<String>) :
    RecyclerView.Adapter<CarouselAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemCarouselImageBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(ItemCarouselImageBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Images.load(holder.binding.ivCarousel, photoUris.getOrNull(position), R.drawable.bg_image_placeholder)
    }

    override fun getItemCount(): Int = if (photoUris.isNotEmpty()) photoUris.size else 1
}
