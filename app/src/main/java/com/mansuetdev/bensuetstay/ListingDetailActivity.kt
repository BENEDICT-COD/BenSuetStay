package com.mansuetdev.bensuetstay

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.mansuetdev.bensuetstay.databinding.ActivityListingDetailBinding
import com.mansuetdev.bensuetstay.databinding.ItemAmenityBinding
import com.mansuetdev.bensuetstay.databinding.ItemRoomTypeBinding

class ListingDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityListingDetailBinding
    private var selectedRoomIndex: Int = -1

    private val amenityIcons = mapOf(
        "WiFi" to R.drawable.ic_wifi,
        "Security" to R.drawable.ic_security,
        "Meals" to R.drawable.ic_restaurant,
        "Water incl." to R.drawable.ic_water_drop
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListingDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val accommodationId = intent.getStringExtra("ACCOMMODATION_ID")
        // Accommodations are stored with IDs like "1_NSFAS" or "1_SELF_OR_BURSARY" due to the homepage split.
        // We strip the suffix to find the correct AdminListing object from the repository.
        val listingId = accommodationId?.substringBeforeLast("_") ?: ""
        val listing = ListingRepository.findById(listingId)
        
        if (listing == null) {
            finish()
            return
        }

        binding.btnBack.setOnClickListener { finish() }

        setupCarousel(listing.photoUris)
        bindDetails(listing)
        buildAmenities(listing.amenities)
        buildRoomTypes(listing.rooms)

        binding.btnCall.setOnClickListener {
            val landlordId = listing.assignedLandlordId
            if (landlordId != null) {
                val landlord = LandlordRepository.findById(landlordId)
                if (landlord != null && landlord.phone.isNotBlank()) {
                    val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                        data = Uri.parse("tel:${landlord.phone}")
                    }
                    startActivity(dialIntent)
                } else {
                    Toast.makeText(this, "Landlord contact number not available", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "No landlord assigned to this listing", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnBookViewing.setOnClickListener {
            val student = StudentRepository.loggedInStudent
            if (student == null) {
                startActivity(Intent(this, LoginActivity::class.java))
                return@setOnClickListener
            }
            val intent = Intent(this, BookViewingActivity::class.java)
            intent.putExtra("LISTING_ID", listing.id)
            startActivity(intent)
        }

        binding.btnSelectRoom.setOnClickListener {
            val student = StudentRepository.loggedInStudent
            if (student == null) {
                startActivity(Intent(this, LoginActivity::class.java))
                return@setOnClickListener
            }

            if (selectedRoomIndex == -1) return@setOnClickListener

            val room = listing.rooms[selectedRoomIndex]
            val intent = if (room.fundingType == FundingType.NSFAS) {
                Intent(this, ConfirmNsfasActivity::class.java)
            } else {
                Intent(this, SelfFundedRegistrationActivity::class.java)
            }
            intent.putExtra("LISTING_ID", listing.id)
            intent.putExtra("ROOM_INDEX", selectedRoomIndex)
            startActivity(intent)
        }

        binding.ivStaticMap.setOnClickListener {
            val mapIntentUri = android.net.Uri.parse("geo:0,0?q=" + android.net.Uri.encode(listing.location))
            val mapIntent = android.content.Intent(android.content.Intent.ACTION_VIEW, mapIntentUri)
            mapIntent.setPackage("com.google.android.apps.maps")
            if (mapIntent.resolveActivity(packageManager) != null) {
                startActivity(mapIntent)
            } else {
                val webIntent = android.content.Intent(android.content.Intent.ACTION_VIEW, 
                    android.net.Uri.parse("https://www.google.com/maps/search/?api=1&query=" + android.net.Uri.encode(listing.location)))
                startActivity(webIntent)
            }
        }
    }

    private fun setupCarousel(photoUris: List<String>) {
        binding.viewPagerImages.adapter = CarouselAdapter(photoUris)

        binding.dotsContainer.removeAllViews()
        val dots = mutableListOf<android.view.View>()
        val imageCount = photoUris.size.coerceAtLeast(1)
        repeat(imageCount) { index ->
            val dot = android.view.View(this)
            val size = (8 * resources.displayMetrics.density).toInt()
            val params = android.widget.LinearLayout.LayoutParams(size, size)
            params.marginStart = (4 * resources.displayMetrics.density).toInt()
            params.marginEnd = (4 * resources.displayMetrics.density).toInt()
            dot.layoutParams = params
            dot.setBackgroundResource(
                if (index == 0) R.drawable.bg_dot_active else R.drawable.bg_dot_inactive
            )
            binding.dotsContainer.addView(dot)
            dots.add(dot)
        }

        binding.viewPagerImages.registerOnPageChangeCallback(
            object : androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    dots.forEachIndexed { index, dot ->
                        dot.setBackgroundResource(
                            if (index == position) R.drawable.bg_dot_active
                            else R.drawable.bg_dot_inactive
                        )
                    }
                }
            }
        )
    }

    private fun bindDetails(listing: AdminListing) {
        binding.tvCampusAccepted.text = "ACCEPTS: ${listing.campusesServed.uppercase()}"
        binding.tvName.text = listing.name
        binding.tvLocation.text = listing.location
        binding.tvPhone.text = "Contact Landlord: ${listing.assignedLandlordName ?: "N/A"}"
        binding.tvSpacesOpen.text = "${listing.rooms.sumOf { it.spacesAvailable }} spaces open"
        binding.tvDescription.text = listing.description
        
        binding.tvTransport.text = if (listing.hasTransport) "Transport Included" else "No Transport"
        binding.ivTransportIcon.alpha = if (listing.hasTransport) 1.0f else 0.5f

        binding.tvFurnished.text = if (listing.isFurnished) "Fully Furnished" else "Unfurnished"
        binding.ivFurnishedIcon.alpha = if (listing.isFurnished) 1.0f else 0.5f

        binding.tvElectricity.text = if (listing.isPrepaidElectricity) "Prepaid Electricity" else "Postpaid/Incl. Electricity"
        binding.ivElectricityIcon.alpha = if (listing.isPrepaidElectricity) 1.0f else 0.5f
    }

    private fun buildAmenities(amenities: List<String>) {
        binding.amenitiesContainer.removeAllViews()
        amenities.forEach { amenity ->
            val itemBinding = ItemAmenityBinding.inflate(
                LayoutInflater.from(this), binding.amenitiesContainer, false
            )
            itemBinding.tvAmenityLabel.text = amenity
            itemBinding.ivAmenityIcon.setImageResource(
                amenityIcons[amenity] ?: R.drawable.ic_place
            )
            val params = android.widget.LinearLayout.LayoutParams(
                0, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, 1f
            )
            params.marginEnd = (6 * resources.displayMetrics.density).toInt()
            itemBinding.root.layoutParams = params
            binding.amenitiesContainer.addView(itemBinding.root)
        }
    }

    private fun buildRoomTypes(rooms: List<RoomConfig>) {
        binding.roomTypeContainer.removeAllViews()
        rooms.forEachIndexed { index, room ->
            val itemBinding = ItemRoomTypeBinding.inflate(
                LayoutInflater.from(this), binding.roomTypeContainer, false
            )
            itemBinding.tvRoomName.text = room.roomType
            if (room.imageUrl != null) {
                try {
                    itemBinding.ivRoomPhoto.setImageURI(android.net.Uri.parse(room.imageUrl))
                } catch (e: Exception) {
                    itemBinding.ivRoomPhoto.setImageResource(R.drawable.bg_image_placeholder)
                }
            } else {
                itemBinding.ivRoomPhoto.setImageResource(R.drawable.bg_image_placeholder)
            }
            val isNsfas = room.fundingType == FundingType.NSFAS
            itemBinding.tvRoomFunding.text = if (isNsfas) "NSFAS Funded" else "Self Funded"
            itemBinding.tvRoomAllowance.text = if (isNsfas) "Allowance Covered" else "Price: ${room.price}"
            itemBinding.tvRoomSpaces.text = "${room.spacesAvailable} spaces available"
            itemBinding.dotSpaces.background.setTint(if (room.spacesAvailable > 0) 0xFF2FA36B.toInt() else 0xFFEB5757.toInt())
            itemBinding.radioRoom.isChecked = index == selectedRoomIndex

            itemBinding.root.setOnClickListener {
                selectedRoomIndex = index
                buildRoomTypes(rooms)
                val roomSelected = selectedRoomIndex >= 0
                binding.btnSelectRoom.isEnabled = roomSelected
                binding.btnSelectRoom.setBackgroundResource(
                    if (roomSelected) R.drawable.bg_button_primary else R.drawable.bg_button_disabled
                )
            }
            binding.roomTypeContainer.addView(itemBinding.root)
        }
    }
}
