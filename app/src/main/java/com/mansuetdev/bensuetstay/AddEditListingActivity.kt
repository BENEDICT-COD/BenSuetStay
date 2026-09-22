package com.mansuetdev.bensuetstay

import com.google.firebase.firestore.FirebaseFirestore
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.chip.Chip
import com.mansuetdev.bensuetstay.databinding.ActivityAddEditListingBinding
import com.mansuetdev.bensuetstay.databinding.ItemRoomConfigRowBinding
import java.util.UUID

class AddEditListingActivity : AppCompatActivity() {
    private val firestore = FirebaseFirestore.getInstance()
    private lateinit var binding: ActivityAddEditListingBinding

    private var editingListingId: String? = null
    private val roomConfigs = mutableListOf<RoomConfig>()
    private val selectedPhotoUris = mutableListOf<Uri>()
    private var selectedVideoUri: Uri? = null
    private var tempRoomImageUri: Uri? = null

    private val amenityOptions = listOf("WiFi", "Security", "Meals", "Water incl.")
    private val roomTypeOptions = listOf("Single", "2-Sharing", "3-Sharing", "4-Sharing")
    private val fundingOptions = listOf("NSFAS", "Self Funded")

    private val pickPhotos = registerForActivityResult(
        ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        selectedPhotoUris.clear()
        if (uris != null) {
            for (uri in uris) {
                try {
                    contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (e: Exception) {
                    // Fallback if not persistable
                }
                selectedPhotoUris.add(uri)
            }
        }
        updateMediaSummary()
    }

    private val pickVideo = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            try {
                contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (e: Exception) {
                // Fallback
            }
            selectedVideoUri = uri
        }
        updateMediaSummary()
    }

    private val pickRoomImage = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            try {
                contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (e: Exception) {
                // Fallback
            }
            tempRoomImageUri = uri
            binding.tvRoomImageSummary.visibility = android.view.View.VISIBLE
            binding.tvRoomImageSummary.text = "Photo selected"
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEditListingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }

        setupAmenityChips()
        setupSpinners()

        editingListingId = intent.getStringExtra("listing_id")
        if (editingListingId != null) {
            binding.tvScreenTitle.text = "Edit Listing"
            loadExistingListing(editingListingId!!)
        }

        binding.btnAddRoom.setOnClickListener { addRoomConfig() }
        binding.btnAddRoomImage.setOnClickListener { pickRoomImage.launch(arrayOf("image/*")) }
        binding.btnAddPhotos.setOnClickListener { pickPhotos.launch(arrayOf("image/*")) }
        binding.btnAddVideo.setOnClickListener { pickVideo.launch(arrayOf("video/*")) }
        binding.btnSaveListing.setOnClickListener { saveListing() }
    }

    private fun setupAmenityChips() {
        binding.chipGroupAmenities.removeAllViews()
        amenityOptions.forEach { amenity ->
            val chip = Chip(this).apply {
                text = amenity
                isCheckable = true
            }
            binding.chipGroupAmenities.addView(chip)
        }
    }

    private fun setupSpinners() {
        binding.spinnerLandlord.adapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_dropdown_item,
            LandlordRepository.landlords.filter { it.verified }.map { it.name }
        )
        binding.spinnerRoomType.adapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_dropdown_item, roomTypeOptions
        )
        binding.spinnerRoomFunding.adapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_dropdown_item, fundingOptions
        )

        binding.spinnerRoomFunding.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                if (fundingOptions[position] == "NSFAS") {
                    binding.etRoomPrice.setText("")
                    binding.etRoomPrice.isEnabled = false
                    binding.etRoomPrice.hint = "No amount required"
                } else {
                    binding.etRoomPrice.isEnabled = true
                    binding.etRoomPrice.hint = "e.g. R2 500/mo"
                }
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }
    }

    private fun addRoomConfig() {
        val roomType = binding.spinnerRoomType.selectedItem?.toString() ?: return
        val fundingLabel = binding.spinnerRoomFunding.selectedItem?.toString() ?: return
        val price = if (fundingLabel == "NSFAS") "NSFAS Allowance" else binding.etRoomPrice.text.toString().trim()
        val spacesText = binding.etRoomSpaces.text.toString().trim()

        if ((fundingLabel != "NSFAS" && price.isEmpty()) || spacesText.isEmpty()) {
            Toast.makeText(this, "Enter room details and spaces first", Toast.LENGTH_SHORT).show()
            return
        }

        val funding = if (fundingLabel == "NSFAS") FundingType.NSFAS else FundingType.SELF_OR_BURSARY
        val spaces = spacesText.toIntOrNull() ?: 0

        roomConfigs.add(RoomConfig(roomType, funding, price, spaces, tempRoomImageUri?.toString()))
        refreshRoomConfigViews()

        binding.etRoomPrice.text.clear()
        binding.etRoomSpaces.text.clear()
        tempRoomImageUri = null
        binding.tvRoomImageSummary.visibility = android.view.View.GONE
    }

    private fun refreshRoomConfigViews() {
        binding.roomConfigContainer.removeAllViews()
        roomConfigs.forEachIndexed { index, config ->
            val row = ItemRoomConfigRowBinding.inflate(
                LayoutInflater.from(this), binding.roomConfigContainer, false
            )
            val fundingLabel = if (config.fundingType == FundingType.NSFAS) "NSFAS" else "Self Funded"
            row.tvRoomTypeLabel.text = config.roomType
            if (config.imageUrl != null) {
                row.ivRoomPreview.visibility = android.view.View.VISIBLE
                row.ivRoomPreview.setImageURI(Uri.parse(config.imageUrl))
            } else {
                row.ivRoomPreview.visibility = android.view.View.GONE
            }
            val detailsText = if (config.fundingType == FundingType.NSFAS) {
                "$fundingLabel \u00b7 ${config.spacesAvailable} spaces"
            } else {
                "$fundingLabel \u00b7 ${config.price} \u00b7 ${config.spacesAvailable} spaces"
            }
            row.tvRoomDetails.text = detailsText
            row.btnRemoveRoom.setOnClickListener {
                roomConfigs.removeAt(index)
                refreshRoomConfigViews()
            }
            binding.roomConfigContainer.addView(row.root)
        }
    }

    private fun updateMediaSummary() {
        binding.tvMediaSummary.text =
            "${selectedPhotoUris.size} photo(s) selected \u00b7 ${if (selectedVideoUri != null) "1 video" else "0 videos"} selected"
    }

    private fun loadExistingListing(id: String) {
        val listing = ListingRepository.findById(id) ?: return

        binding.etName.setText(listing.name)
        binding.etLocation.setText(listing.location)
        binding.etCampus.setText(listing.campusesServed)
        binding.etDescription.setText(listing.description)

        binding.cbTransport.isChecked = listing.hasTransport
        binding.cbFurnished.isChecked = listing.isFurnished
        binding.cbPrepaidElectricity.isChecked = listing.isPrepaidElectricity

        for (i in 0 until binding.chipGroupAmenities.childCount) {
            val chip = binding.chipGroupAmenities.getChildAt(i) as Chip
            chip.isChecked = listing.amenities.contains(chip.text.toString())
        }

        val landlordIndex = LandlordRepository.landlords
            .filter { it.verified }
            .indexOfFirst { it.id == listing.assignedLandlordId }
        if (landlordIndex != -1) binding.spinnerLandlord.setSelection(landlordIndex)

        roomConfigs.clear()
        roomConfigs.addAll(listing.rooms)
        refreshRoomConfigViews()

        binding.tvMediaSummary.text =
            "${listing.photoCount} photo(s) already uploaded \u00b7 ${listing.videoCount} video(s) already uploaded"

        selectedPhotoUris.clear()
        listing.photoUris.forEach { selectedPhotoUris.add(Uri.parse(it)) }
        selectedVideoUri = if (listing.videoUri.isNotEmpty()) Uri.parse(listing.videoUri) else null
        updateMediaSummary()
    }

    private fun saveListing() {
        val name = binding.etName.text.toString().trim()
        val location = binding.etLocation.text.toString().trim()
        val campus = binding.etCampus.text.toString().trim()
        val description = binding.etDescription.text.toString().trim()

        if (name.isEmpty() || location.isEmpty() || campus.isEmpty()) {
            Toast.makeText(this, "Please fill in name, location, and campus", Toast.LENGTH_SHORT).show()
            return
        }
        if (roomConfigs.isEmpty()) {
            Toast.makeText(this, "Add at least one room type", Toast.LENGTH_SHORT).show()
            return
        }

        val selectedAmenities = mutableListOf<String>()
        for (i in 0 until binding.chipGroupAmenities.childCount) {
            val chip = binding.chipGroupAmenities.getChildAt(i) as Chip
            if (chip.isChecked) selectedAmenities.add(chip.text.toString())
        }

        val verifiedLandlords = LandlordRepository.landlords.filter { it.verified }
        val selectedLandlord = verifiedLandlords.getOrNull(binding.spinnerLandlord.selectedItemPosition)

        val listing = AdminListing(
            id = editingListingId ?: UUID.randomUUID().toString(),
            name = name,
            location = location,
            campusesServed = campus,
            amenities = selectedAmenities,
            assignedLandlordId = selectedLandlord?.id,
            assignedLandlordName = selectedLandlord?.name,
            rooms = roomConfigs.toList(),
            photoCount = selectedPhotoUris.size,
            videoCount = if (selectedVideoUri != null) 1 else 0,
            description = description,
            hasTransport = binding.cbTransport.isChecked,
            isFurnished = binding.cbFurnished.isChecked,
            isPrepaidElectricity = binding.cbPrepaidElectricity.isChecked,
            photoUris = selectedPhotoUris.map { it.toString() },
            videoUri = selectedVideoUri?.toString() ?: ""
        )

        binding.btnSaveListing.isEnabled = false
        binding.btnSaveListing.text = "Saving\u2026"

        firestore.collection("accommodations").document(listing.id)
            .set(listing)
            .addOnSuccessListener {
                // Keep the local repository in sync too, so Manage Listings
                // reflects the change immediately without waiting on a re-fetch.
                if (editingListingId != null) {
                    ListingRepository.updateListing(listing)
                } else {
                    ListingRepository.addListing(listing)
                }
                val message = if (editingListingId != null) "Listing updated" else "Listing created"
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                binding.btnSaveListing.isEnabled = true
                binding.btnSaveListing.text = "Save Listing"
                Toast.makeText(this, "Failed to save: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}