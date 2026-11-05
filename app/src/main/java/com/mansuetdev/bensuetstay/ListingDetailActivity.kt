package com.mansuetdev.bensuetstay

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
<<<<<<< HEAD
import android.view.View
import android.widget.LinearLayout
import androidx.viewpager2.widget.ViewPager2
=======
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
>>>>>>> d231b4d53e6f750ae3c0daf4f61493ce26eaf809
import com.mansuetdev.bensuetstay.databinding.ActivityListingDetailBinding
import com.mansuetdev.bensuetstay.databinding.ItemAmenityBinding
import com.mansuetdev.bensuetstay.databinding.ItemRoomTypeBinding

<<<<<<< HEAD
/** Public listing page. Guests can look; booking and applying require a student login. */
class ListingDetailActivity : BaseActivity() {

    companion object {
        const val EXTRA_LISTING_ID = "LISTING_ID"
    }

    private lateinit var binding: ActivityListingDetailBinding
    private var listing: AdminListing? = null
    private var selectedRoomIndex = -1
=======
class ListingDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityListingDetailBinding
    private var selectedRoomIndex: Int = -1
>>>>>>> d231b4d53e6f750ae3c0daf4f61493ce26eaf809

    private val amenityIcons = mapOf(
        "WiFi" to R.drawable.ic_wifi,
        "Security" to R.drawable.ic_security,
        "Meals" to R.drawable.ic_restaurant,
        "Water incl." to R.drawable.ic_water_drop
    )

<<<<<<< HEAD
    // Re-bind when the listing changes (e.g. a space is taken) while this page is open
    private val listingListener: () -> Unit = {
        listing?.let { current -> ListingRepository.findById(current.id)?.let { bind(it) } }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (restoreSessionIfNeeded()) return
        binding = ActivityListingDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.btnBack.setOnClickListener { finish() }

        // Landlords and admins can preview listings but not book or apply
        val canAct = Session.role == null || Session.role == Role.STUDENT
        binding.btnBookViewing.visibility = if (canAct) View.VISIBLE else View.GONE
        binding.btnSelectRoom.visibility = if (canAct) View.VISIBLE else View.GONE
        setSelectButtonEnabled(false)

        val listingId = intent.getStringExtra(EXTRA_LISTING_ID)
        if (listingId.isNullOrBlank()) {
            finish()
            return
        }
        ListingRepository.get(
            listingId,
            onResult = { found ->
                if (found == null) {
                    toast(getString(R.string.this_listing_is_no_longer_available))
                    finish()
                } else {
                    bind(found)
                    setupActions()
                }
            },
            onError = { e ->
                toast(AuthErrors.message(this, e))
                finish()
            }
        )
    }

    override fun onStart() {
        super.onStart()
        ListingRepository.addListener(listingListener)
    }

    override fun onStop() {
        super.onStop()
        ListingRepository.removeListener(listingListener)
    }

    private fun bind(value: AdminListing) {
        val firstBind = listing == null
        listing = value
        if (firstBind) {
            setupCarousel(value.photoUris)
            showDistanceToCampus(value)
        }

        binding.tvCampusAccepted.text =
            if (value.campusesServed.isBlank()) "ACCEPTS: ALL CAMPUSES" else "ACCEPTS: ${value.campusesServed.uppercase()}"
        binding.tvName.text = value.name
        binding.tvLocation.text = value.location
        binding.tvPhone.text = getString(R.string.contact_landlord_2, value.assignedLandlordName ?: getString(R.string.not_assigned_yet))
        binding.tvSpacesOpen.text = getString(R.string.spaces_open, value.totalSpaces)
        binding.tvDescription.text = value.description.ifBlank { getString(R.string.no_description_provided) }

        binding.tvTransport.text = if (value.hasTransport) getString(R.string.transport_included) else getString(R.string.no_transport)
        binding.ivTransportIcon.alpha = if (value.hasTransport) 1.0f else 0.5f
        binding.tvFurnished.text = if (value.isFurnished) getString(R.string.fully_furnished) else getString(R.string.unfurnished)
        binding.ivFurnishedIcon.alpha = if (value.isFurnished) 1.0f else 0.5f
        binding.tvElectricity.text = if (value.isPrepaidElectricity) getString(R.string.prepaid_electricity) else getString(R.string.postpaid_incl_electricity)
        binding.ivElectricityIcon.alpha = if (value.isPrepaidElectricity) 1.0f else 0.5f

        buildAmenities(value.amenities)
        if (selectedRoomIndex >= value.rooms.size) selectedRoomIndex = -1
        buildRoomTypes(value.rooms)
    }

    /** REST API (Nominatim): "≈ 2.4 km from <your campus>" for signed-in students. */
    private fun showDistanceToCampus(value: AdminListing) {
        val campus = Session.student?.institution?.takeIf { it.isNotBlank() } ?: return
        val showFrom = { place: GeocodingApi.Place ->
            GeocodingApi.search(this, "$campus, South Africa") { campusPlace ->
                if (!isDestroyed && campusPlace != null) {
                    val km = ListingLogic.distanceKm(place.latitude, place.longitude, campusPlace.latitude, campusPlace.longitude)
                    binding.tvDistance.text = getString(
                        R.string.distance_from_campus, String.format(java.util.Locale.getDefault(), "%.1f", km), campus
                    )
                    binding.tvDistance.visibility = View.VISIBLE
                }
            }
        }
        if (ListingLogic.hasCoordinates(value.latitude, value.longitude)) {
            showFrom(GeocodingApi.Place(value.latitude, value.longitude))
        } else {
            // Older listings saved without coordinates: look the address up now
            GeocodingApi.search(this, "${value.location}, South Africa") { place ->
                if (!isDestroyed && place != null) showFrom(place)
            }
        }
    }

    private fun setupActions() {
        binding.btnCall.setOnClickListener {
            val phone = listing?.assignedLandlordPhone
            when {
                listing?.assignedLandlordId == null -> toast(getString(R.string.no_landlord_is_assigned_to_this))
                phone.isNullOrBlank() -> toast(getString(R.string.the_landlord_s_number_isn_t))
                !dial(phone) -> toast(getString(R.string.no_phone_app_found_on_this))
=======
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
>>>>>>> d231b4d53e6f750ae3c0daf4f61493ce26eaf809
            }
        }

        binding.btnBookViewing.setOnClickListener {
<<<<<<< HEAD
            val current = listing ?: return@setOnClickListener
            if (!requireStudentLogin()) return@setOnClickListener
            startActivity(
                Intent(this, BookViewingActivity::class.java).putExtra(EXTRA_LISTING_ID, current.id)
            )
        }

        binding.btnSelectRoom.setOnClickListener {
            val current = listing ?: return@setOnClickListener
            if (!requireStudentLogin()) return@setOnClickListener
            val room = current.rooms.getOrNull(selectedRoomIndex) ?: run {
                toast(getString(R.string.choose_a_room_type_first)); return@setOnClickListener
            }
            if (room.spacesAvailable <= 0) {
                toast(getString(R.string.that_room_type_is_full)); return@setOnClickListener
            }
            val target = if (room.fundingType == FundingType.NSFAS) {
                ConfirmNsfasActivity::class.java
            } else {
                SelfFundedRegistrationActivity::class.java
            }
            startActivity(
                Intent(this, target)
                    .putExtra(EXTRA_LISTING_ID, current.id)
                    .putExtra(ApplicationForm.EXTRA_ROOM_TYPE, room.roomType)
                    .putExtra(ApplicationForm.EXTRA_ROOM_FUNDING, room.fundingType.name)
            )
        }

        binding.ivStaticMap.setOnClickListener {
            val current = listing ?: return@setOnClickListener
            val location = current.location
            val query = if (ListingLogic.hasCoordinates(current.latitude, current.longitude))
                "${current.latitude},${current.longitude}(${current.name})" else location
            val geo = Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=" + Uri.encode(query)))
                .setPackage("com.google.android.apps.maps")
            if (geo.resolveActivity(packageManager) != null) {
                startActivity(geo)
            } else if (!openUrl("https://www.google.com/maps/search/?api=1&query=" + Uri.encode(location))) {
                toast(getString(R.string.no_maps_or_browser_app_found))
=======
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
>>>>>>> d231b4d53e6f750ae3c0daf4f61493ce26eaf809
            }
        }
    }

<<<<<<< HEAD
    private fun requireStudentLogin(): Boolean {
        if (Session.role == Role.STUDENT) return true
        toast(getString(R.string.log_in_as_a_student_to_2))
        startActivity(Intent(this, LoginActivity::class.java))
        return false
    }

    private fun setupCarousel(photoUris: List<String>) {
        binding.viewPagerImages.adapter = CarouselAdapter(photoUris)
        binding.dotsContainer.removeAllViews()
        val dots = mutableListOf<View>()
        val density = resources.displayMetrics.density
        repeat(photoUris.size.coerceAtLeast(1)) { index ->
            val dot = View(this)
            val size = (8 * density).toInt()
            dot.layoutParams = LinearLayout.LayoutParams(size, size).apply {
                marginStart = (4 * density).toInt()
                marginEnd = (4 * density).toInt()
            }
            dot.setBackgroundResource(if (index == 0) R.drawable.bg_dot_active else R.drawable.bg_dot_inactive)
            binding.dotsContainer.addView(dot)
            dots.add(dot)
        }
        binding.viewPagerImages.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                dots.forEachIndexed { index, dot ->
                    dot.setBackgroundResource(if (index == position) R.drawable.bg_dot_active else R.drawable.bg_dot_inactive)
                }
            }
        })
=======
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
>>>>>>> d231b4d53e6f750ae3c0daf4f61493ce26eaf809
    }

    private fun buildAmenities(amenities: List<String>) {
        binding.amenitiesContainer.removeAllViews()
<<<<<<< HEAD
        val density = resources.displayMetrics.density
        amenities.forEach { amenity ->
            val item = ItemAmenityBinding.inflate(LayoutInflater.from(this), binding.amenitiesContainer, false)
            item.tvAmenityLabel.text = Labels.amenity(this, amenity)
            item.ivAmenityIcon.setImageResource(amenityIcons[amenity] ?: R.drawable.ic_place)
            item.root.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                marginEnd = (6 * density).toInt()
            }
            binding.amenitiesContainer.addView(item.root)
=======
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
>>>>>>> d231b4d53e6f750ae3c0daf4f61493ce26eaf809
        }
    }

    private fun buildRoomTypes(rooms: List<RoomConfig>) {
        binding.roomTypeContainer.removeAllViews()
        rooms.forEachIndexed { index, room ->
<<<<<<< HEAD
            val item = ItemRoomTypeBinding.inflate(LayoutInflater.from(this), binding.roomTypeContainer, false)
            item.tvRoomName.text = Labels.roomType(this, room.roomType)
            Images.load(item.ivRoomPhoto, room.imageUrl, R.drawable.bg_image_placeholder)
            val isNsfas = room.fundingType == FundingType.NSFAS
            item.tvRoomFunding.text = if (isNsfas) getString(R.string.nsfas_funded) else getString(R.string.self_funded)
            item.tvRoomAllowance.text = if (isNsfas) getString(R.string.allowance_covered) else getString(R.string.price, Labels.roomPrice(this, room))
            item.tvRoomSpaces.text = if (room.spacesAvailable > 0) getString(R.string.spaces_available_3, room.spacesAvailable) else getString(R.string.full)
            item.dotSpaces.background.mutate().setTint(if (room.spacesAvailable > 0) 0xFF2FA36B.toInt() else 0xFFEB5757.toInt())
            item.radioRoom.isChecked = index == selectedRoomIndex
            item.root.alpha = if (room.spacesAvailable > 0) 1f else 0.6f

            item.root.setOnClickListener {
                if (room.spacesAvailable <= 0) {
                    toast(getString(R.string.that_room_type_is_full))
                    return@setOnClickListener
                }
                selectedRoomIndex = index
                buildRoomTypes(rooms)
            }
            binding.roomTypeContainer.addView(item.root)
        }
        setSelectButtonEnabled(selectedRoomIndex >= 0)
    }

    private fun setSelectButtonEnabled(enabled: Boolean) {
        binding.btnSelectRoom.isEnabled = enabled
        binding.btnSelectRoom.setBackgroundResource(
            if (enabled) R.drawable.bg_button_primary else R.drawable.bg_button_disabled
        )
=======
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
>>>>>>> d231b4d53e6f750ae3c0daf4f61493ce26eaf809
    }
}
