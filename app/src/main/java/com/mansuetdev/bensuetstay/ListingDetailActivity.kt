package com.mansuetdev.bensuetstay

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.viewpager2.widget.ViewPager2
import com.mansuetdev.bensuetstay.databinding.ActivityListingDetailBinding
import com.mansuetdev.bensuetstay.databinding.ItemAmenityBinding
import com.mansuetdev.bensuetstay.databinding.ItemRoomTypeBinding

/** Public listing page. Guests can look; booking and applying require a student login. */
class ListingDetailActivity : BaseActivity() {

    companion object {
        const val EXTRA_LISTING_ID = "LISTING_ID"
    }

    private lateinit var binding: ActivityListingDetailBinding
    private var listing: AdminListing? = null
    private var selectedRoomIndex = -1

    private val amenityIcons = mapOf(
        "WiFi" to R.drawable.ic_wifi,
        "Security" to R.drawable.ic_security,
        "Meals" to R.drawable.ic_restaurant,
        "Water incl." to R.drawable.ic_water_drop
    )

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
            }
        }

        binding.btnBookViewing.setOnClickListener {
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
            }
        }
    }

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
    }

    private fun buildAmenities(amenities: List<String>) {
        binding.amenitiesContainer.removeAllViews()
        val density = resources.displayMetrics.density
        amenities.forEach { amenity ->
            val item = ItemAmenityBinding.inflate(LayoutInflater.from(this), binding.amenitiesContainer, false)
            item.tvAmenityLabel.text = Labels.amenity(this, amenity)
            item.ivAmenityIcon.setImageResource(amenityIcons[amenity] ?: R.drawable.ic_place)
            item.root.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                marginEnd = (6 * density).toInt()
            }
            binding.amenitiesContainer.addView(item.root)
        }
    }

    private fun buildRoomTypes(rooms: List<RoomConfig>) {
        binding.roomTypeContainer.removeAllViews()
        rooms.forEachIndexed { index, room ->
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
    }
}
