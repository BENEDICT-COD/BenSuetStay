package com.mansuetdev.bensuetstay

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.material.chip.Chip
import com.mansuetdev.bensuetstay.databinding.ActivityAddEditListingBinding
import com.mansuetdev.bensuetstay.databinding.ItemRoomConfigRowBinding

/**
 * Admin create/edit listing. Photos, the video and room photos are saved as links to the
 * files on this phone (no Firebase Storage, so the project can stay on the free Spark plan).
 * Persistable access is kept so they keep showing here after a restart; other phones see
 * the placeholder image.
 */
class AddEditListingActivity : BaseActivity() {

    companion object {
        const val EXTRA_LISTING_ID = "listing_id"
        private const val MAX_SPACES = 500
    }

    private lateinit var binding: ActivityAddEditListingBinding

    private var editingId: String? = null
    private var landlords: List<LandlordAccount> = emptyList()
    private var assignedLandlordId: String? = null
    private val roomConfigs = mutableListOf<RoomConfig>()
    // Each entry is either an existing https:// download URL or a newly picked content:// URI
    private val photoUris = mutableListOf<String>()
    private var videoUri: String? = null
    private var pendingRoomImage: String? = null
    private var saving = false

    // Stored (English) values; the UI shows them translated via Labels
    private val roomTypeOptions = Labels.ROOM_TYPES.map { it.first }
    private val fundingOptions = listOf(FundingType.NSFAS, FundingType.SELF_OR_BURSARY)

    private val pickPhotos = registerForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
        if (uris.isNotEmpty()) {
            uris.forEach { keepAccess(it) }
            photoUris.clear()
            photoUris.addAll(uris.map { it.toString() })
            updateMediaSummary()
        }
    }

    private val pickVideo = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri != null) {
            keepAccess(uri)
            videoUri = uri.toString()
            updateMediaSummary()
        }
    }

    private val pickRoomImage = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri != null) {
            keepAccess(uri)
            pendingRoomImage = uri.toString()
            binding.tvRoomImageSummary.visibility = View.VISIBLE
            binding.tvRoomImageSummary.text = getString(R.string.room_photo_selected, displayName(uri))
        }
    }

    /** Keeps read access to a picked file after the app restarts (where the provider allows it). */
    private fun keepAccess(uri: Uri) {
        try {
            contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        } catch (e: SecurityException) {
            // This provider doesn't offer lasting access; the file still shows until the app restarts
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!requireRole(Role.ADMIN)) return
        binding = ActivityAddEditListingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }
        setupAmenityChips()
        setupRoomSpinners()
        updateMediaSummary()

        binding.btnAddRoom.setOnClickListener { addRoomConfig() }
        binding.btnAddRoomImage.setOnClickListener { pickRoomImage.launch(arrayOf("image/*")) }
        binding.btnAddPhotos.setOnClickListener { pickPhotos.launch(arrayOf("image/*")) }
        binding.btnAddVideo.setOnClickListener { pickVideo.launch(arrayOf("video/*")) }
        binding.btnSaveListing.setOnClickListener { saveListing() }

        editingId = intent.getStringExtra(EXTRA_LISTING_ID)
        val id = editingId
        if (id != null) {
            binding.tvScreenTitle.text = getString(R.string.edit_listing)
            binding.btnSaveListing.isEnabled = false
            ListingRepository.get(id,
                onResult = { listing ->
                    if (listing == null) { toast(getString(R.string.listing_not_found)); finish() }
                    else { fillForm(listing); binding.btnSaveListing.isEnabled = true }
                },
                onError = { e -> toast(AuthErrors.message(this, e)); finish() }
            )
        }
        loadLandlords()
    }

    private fun loadLandlords() {
        LandlordRepository.getAll()
            .addOnSuccessListener { all ->
                landlords = all.filter { it.verified }
                val names = listOf(getString(R.string.unassigned)) + landlords.map { it.name }
                binding.spinnerLandlord.adapter =
                    ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, names)
                selectAssignedLandlord()
            }
            .addOnFailureListener { e -> toast(getString(R.string.couldn_t_load_landlords, AuthErrors.message(this, e)), long = true) }
    }

    private fun selectAssignedLandlord() {
        val index = landlords.indexOfFirst { it.id == assignedLandlordId }
        if (index >= 0 && binding.spinnerLandlord.adapter != null) binding.spinnerLandlord.setSelection(index + 1)
    }

    private fun setupAmenityChips() {
        binding.chipGroupAmenities.removeAllViews()
        Labels.AMENITIES.forEach { (stored, label) ->
            binding.chipGroupAmenities.addView(Chip(this).apply {
                text = getString(label)
                tag = stored // the value saved to Firestore, independent of language
                isCheckable = true
            })
        }
    }

    private fun setupRoomSpinners() {
        binding.spinnerRoomType.adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, roomTypeOptions.map { Labels.roomType(this, it) })
        binding.spinnerRoomFunding.adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, fundingOptions.map { Labels.funding(this, it) })
        binding.spinnerRoomFunding.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val nsfas = fundingOptions[position] == FundingType.NSFAS
                binding.etRoomPrice.isEnabled = !nsfas
                if (nsfas) binding.etRoomPrice.setText("")
                binding.etRoomPrice.hint = if (nsfas) getString(R.string.covered_by_nsfas_allowance) else getString(R.string.monthly_rent_in_rand_e_g)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun addRoomConfig() {
        val roomType = roomTypeOptions.getOrNull(binding.spinnerRoomType.selectedItemPosition) ?: return
        val funding = fundingOptions.getOrNull(binding.spinnerRoomFunding.selectedItemPosition) ?: return
        val price = binding.etRoomPrice.text.toString().trim().toIntOrNull() ?: 0
        val spaces = binding.etRoomSpaces.text.toString().trim().toIntOrNull()

        when {
            funding == FundingType.SELF_OR_BURSARY && price <= 0 -> {
                binding.etRoomPrice.error = getString(R.string.enter_the_monthly_rent); return
            }
            spaces == null || spaces < 0 || spaces > MAX_SPACES -> {
                binding.etRoomSpaces.error = getString(R.string.enter_spaces_0, MAX_SPACES); return
            }
            roomConfigs.any { it.roomType == roomType && it.fundingType == funding } -> {
                // Approvals find the room by type + funding, so each pair must be unique
                toast(getString(R.string.is_already_added_remove_it_first, Labels.roomType(this, roomType), Labels.funding(this, funding)), long = true)
                return
            }
        }

        roomConfigs.add(
            RoomConfig(
                roomType = roomType,
                fundingType = funding,
                priceAmount = if (funding == FundingType.NSFAS) 0 else price,
                spacesAvailable = spaces,
                imageUrl = pendingRoomImage
            )
        )
        refreshRoomConfigViews()
        binding.etRoomPrice.text.clear()
        binding.etRoomSpaces.text.clear()
        pendingRoomImage = null
        binding.tvRoomImageSummary.visibility = View.GONE
    }

    private fun refreshRoomConfigViews() {
        binding.roomConfigContainer.removeAllViews()
        roomConfigs.forEachIndexed { index, config ->
            val row = ItemRoomConfigRowBinding.inflate(LayoutInflater.from(this), binding.roomConfigContainer, false)
            row.tvRoomTypeLabel.text = Labels.roomType(this, config.roomType)
            if (config.imageUrl != null) {
                row.ivRoomPreview.visibility = View.VISIBLE
                Images.load(row.ivRoomPreview, config.imageUrl, R.drawable.bg_image_placeholder)
            } else {
                row.ivRoomPreview.visibility = View.GONE
            }
            val fundingLabel = Labels.funding(this, config.fundingType)
            row.tvRoomDetails.text = if (config.fundingType == FundingType.NSFAS) {
                getString(R.string.spaces, fundingLabel, config.spacesAvailable)
            } else {
                getString(R.string.spaces_2, fundingLabel, Labels.roomPrice(this, config), config.spacesAvailable)
            }
            row.btnRemoveRoom.setOnClickListener {
                roomConfigs.removeAt(index)
                refreshRoomConfigViews()
            }
            binding.roomConfigContainer.addView(row.root)
        }
    }

    private fun updateMediaSummary() {
        val photoText = if (photoUris.isEmpty()) getString(R.string.no_photos) else getString(R.string.photo_s_selected, photoUris.size)
        val videoText = if (videoUri.isNullOrBlank()) getString(R.string.no_video) else getString(R.string.n_1_video_selected)
        binding.tvMediaSummary.text = "$photoText \u00b7 $videoText"
    }

    private fun fillForm(listing: AdminListing) {
        binding.etName.setText(listing.name)
        binding.etLocation.setText(listing.location)
        binding.etCampus.setText(listing.campusesServed)
        binding.etDescription.setText(listing.description)
        binding.cbTransport.isChecked = listing.hasTransport
        binding.cbFurnished.isChecked = listing.isFurnished
        binding.cbPrepaidElectricity.isChecked = listing.isPrepaidElectricity

        for (i in 0 until binding.chipGroupAmenities.childCount) {
            val chip = binding.chipGroupAmenities.getChildAt(i) as Chip
            chip.isChecked = listing.amenities.contains(chip.tag as? String)
        }

        assignedLandlordId = listing.assignedLandlordId
        selectAssignedLandlord()

        roomConfigs.clear()
        roomConfigs.addAll(listing.rooms)
        refreshRoomConfigViews()

        photoUris.clear()
        photoUris.addAll(listing.photoUris)
        videoUri = listing.videoUri.ifBlank { null }
        updateMediaSummary()
    }

    private fun saveListing() {
        if (saving) return
        val name = binding.etName.text.toString().trim()
        val location = binding.etLocation.text.toString().trim()
        val campus = binding.etCampus.text.toString().trim()
        val description = binding.etDescription.text.toString().trim()

        when {
            name.isEmpty() -> { binding.etName.error = getString(R.string.required); return }
            location.isEmpty() -> { binding.etLocation.error = getString(R.string.required); return }
            campus.isEmpty() -> { binding.etCampus.error = getString(R.string.required); return }
            roomConfigs.isEmpty() -> { toast(getString(R.string.add_at_least_one_room_type)); return }
            binding.spinnerLandlord.adapter == null -> {
                toast(getString(R.string.still_loading_landlords_try_again_in)); return
            }
        }

        val amenities = (0 until binding.chipGroupAmenities.childCount)
            .map { binding.chipGroupAmenities.getChildAt(it) as Chip }
            .filter { it.isChecked }
            .mapNotNull { it.tag as? String }

        // Spinner position 0 is "Unassigned"
        val landlord = landlords.getOrNull(binding.spinnerLandlord.selectedItemPosition - 1)
        val id = editingId ?: ListingRepository.newId()

        val listing = AdminListing(
            id = id,
            name = name,
            location = location,
            campusesServed = campus,
            amenities = amenities,
            assignedLandlordId = landlord?.id,
            assignedLandlordName = landlord?.name,
            assignedLandlordPhone = landlord?.phone,
            rooms = roomConfigs.toList(),
            description = description,
            hasTransport = binding.cbTransport.isChecked,
            isFurnished = binding.cbFurnished.isChecked,
            isPrepaidElectricity = binding.cbPrepaidElectricity.isChecked,
            photoUris = photoUris.toList(),
            videoUri = videoUri.orEmpty()
        )

        // REST API: store the listing's coordinates so students see the distance to campus
        setSaving(true, getString(R.string.finding_location))
        GeocodingApi.search(this, "$location, South Africa") { place ->
            if (isDestroyed) return@search
            setSaving(true, getString(R.string.saving))
            saveToFirestore(listing.copy(latitude = place?.latitude ?: 0.0, longitude = place?.longitude ?: 0.0))
        }
    }

    private fun saveToFirestore(listing: AdminListing) {
        ListingRepository.save(listing)
            .addOnSuccessListener {
                toast(if (editingId != null) getString(R.string.listing_updated) else getString(R.string.listing_created))
                finish()
            }
            .addOnFailureListener { e ->
                setSaving(false, getString(R.string.save_listing))
                toast(getString(R.string.couldn_t_save, AuthErrors.message(this, e)), long = true)
            }
    }

    private fun setSaving(value: Boolean, text: String) {
        saving = value
        binding.btnSaveListing.isEnabled = !value
        binding.btnSaveListing.text = text
    }
}
