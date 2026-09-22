package com.mansuetdev.bensuetstay

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.mansuetdev.bensuetstay.databinding.ActivityHomeBinding

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var adapter: AccommodationAdapter
    private var allAccommodations: List<Accommodation> = emptyList()

    // Whoever is logged in — null means a guest, so nothing is auto-filtered
    private var studentFundingType: String? = null
    private var studentInstitution: String? = null

    private val listingListener = {
        runOnUiThread {
            allAccommodations = buildDisplayList(ListingRepository.listings)
            preselectDefaultFilter()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        studentFundingType = intent.getStringExtra("fundingType")
        studentInstitution = intent.getStringExtra("institution")

        adapter = AccommodationAdapter(emptyList())
        binding.recyclerAccommodations.layoutManager = LinearLayoutManager(this)
        binding.recyclerAccommodations.adapter = adapter

        binding.chipAll.setOnClickListener { applyFilter(null, binding.chipAll) }
        binding.chipNsfas.setOnClickListener { applyFilter(FundingType.NSFAS, binding.chipNsfas) }
        binding.chipSelfFunded.setOnClickListener { applyFilter(FundingType.SELF_OR_BURSARY, binding.chipSelfFunded) }
        binding.chipNearCampus.setOnClickListener {
            // Actively filter to show only accommodations matching the student's campus
            val student = StudentRepository.loggedInStudent
            if (student != null && student.institution.isNotEmpty()) {
                studentInstitution = student.institution
                allAccommodations = buildDisplayList(ListingRepository.listings)
                applyFilter(null, binding.chipNearCampus)
            } else {
                android.widget.Toast.makeText(this, "Please log in to filter by your campus", android.widget.Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnLoginRegister.setOnClickListener {
            startActivity(android.content.Intent(this, LoginActivity::class.java))
        }

        binding.etSearch.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterBySearch(s.toString())
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })

        updateLoginState()
        setupBottomNavigation()
    }

    private fun updateLoginState() {
        val student = StudentRepository.loggedInStudent
        if (student != null) {
            binding.btnLoginRegister.visibility = android.view.View.GONE
            binding.tvWelcomeBack.visibility = android.view.View.VISIBLE
            binding.tvWelcomeBack.text = "Welcome, ${student.fullName.split(" ").first()}"
            
            // Auto-apply their funding filter if they just logged in
            if (studentFundingType == null) {
                studentFundingType = student.fundingType.name
                studentInstitution = student.institution
                allAccommodations = buildDisplayList(ListingRepository.listings)
                preselectDefaultFilter()
            }
        } else {
            binding.btnLoginRegister.visibility = android.view.View.VISIBLE
            binding.tvWelcomeBack.visibility = android.view.View.GONE
        }
    }

    private fun filterBySearch(query: String) {
        val filtered = allAccommodations.filter {
            it.name.contains(query, ignoreCase = true) ||
                    it.location.contains(query, ignoreCase = true) ||
                    it.roomTypes.any { type -> type.contains(query, ignoreCase = true) }
        }
        adapter.updateList(filtered)
        binding.tvResultsCount.text = "${filtered.size} accommodations found"
    }

    private fun setupBottomNavigation() {
        binding.bottomNav.selectedItemId = R.id.nav_home
        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true
                R.id.nav_search -> {
                    binding.etSearch.requestFocus()
                    val imm = getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
                    imm.showSoftInput(binding.etSearch, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT)
                    true
                }
                R.id.nav_bookings -> {
                    if (StudentRepository.loggedInStudent != null) {
                        startActivity(android.content.Intent(this, StudentBookingsActivity::class.java))
                    } else {
                        startActivity(android.content.Intent(this, LoginActivity::class.java))
                    }
                    false
                }
                R.id.nav_profile -> {
                    if (StudentRepository.loggedInStudent != null) {
                        startActivity(android.content.Intent(this, StudentProfileActivity::class.java))
                    } else {
                        startActivity(android.content.Intent(this, LoginActivity::class.java))
                    }
                    false
                }
                else -> false
            }
        }
    }

    override fun onStart() {
        super.onStart()
        ListingRepository.addListener(listingListener)
        allAccommodations = buildDisplayList(ListingRepository.listings)
        preselectDefaultFilter()
    }

    override fun onStop() {
        super.onStop()
        ListingRepository.removeListener(listingListener)
    }

    /**
     * Turns admin-created listings into the cards the Homepage shows.
     * A listing appears once per funding type it actually offers rooms for,
     * and only its matching rooms are counted/shown for that card.
     */
    private fun buildDisplayList(listings: List<AdminListing>): List<Accommodation> {
        val result = mutableListOf<Accommodation>()

        listings.forEach { listing ->
            // Only include listings whose campus matches the student's institution.
            // Guests (no institution known yet) see everything.
            val matchesCampus = studentInstitution.isNullOrBlank() ||
                    listing.campusesServed.contains(studentInstitution!!, ignoreCase = true) ||
                    studentInstitution!!.contains(listing.campusesServed, ignoreCase = true)

            if (!matchesCampus) return@forEach

            FundingType.entries.forEach { type ->
                val roomsOfType = listing.rooms.filter { it.fundingType == type }
                if (roomsOfType.isEmpty()) return@forEach

                val totalSpaces = roomsOfType.sumOf { it.spacesAvailable }
                val priceLabel = if (type == FundingType.NSFAS) {
                    "NSFAS Funded"
                } else {
                    val cheapest = roomsOfType.minByOrNull { it.price }?.price ?: ""
                    "From $cheapest"
                }

                result.add(
                    Accommodation(
                        id = "${listing.id}_${type.name}",
                        name = listing.name,
                        location = listing.location,
                        fundingType = type,
                        spacesAvailable = totalSpaces,
                        priceLabel = priceLabel,
                        roomTypes = roomsOfType.map { it.roomType },
                        photoUris = listing.photoUris,
                        description = listing.description,
                        hasTransport = listing.hasTransport,
                        isFurnished = listing.isFurnished,
                        isPrepaidElectricity = listing.isPrepaidElectricity,
                        latitude = listing.latitude,
                        longitude = listing.longitude
                    )
                )
            }
        }
        return result
    }

    private fun preselectDefaultFilter() {
        when (studentFundingType) {
            "NSFAS" -> applyFilter(FundingType.NSFAS, binding.chipNsfas)
            "SELF_FUNDED" -> applyFilter(FundingType.SELF_OR_BURSARY, binding.chipSelfFunded)
            else -> applyFilter(null, binding.chipAll) // guest browsing
        }
    }

    private fun applyFilter(type: FundingType?, selectedChip: android.widget.TextView) {
        listOf(binding.chipAll, binding.chipNsfas, binding.chipSelfFunded, binding.chipNearCampus)
            .forEach {
                it.setBackgroundResource(R.drawable.bg_chip_unselected)
                it.setTextColor(ContextCompat.getColor(this, R.color.text_muted))
            }
        selectedChip.setBackgroundResource(R.drawable.bg_chip_selected)
        selectedChip.setTextColor(ContextCompat.getColor(this, R.color.brand_blue))

        val filtered = if (type == null) {
            allAccommodations
        } else {
            allAccommodations.filter { it.fundingType == type }
        }
        adapter.updateList(filtered)
        binding.tvResultsCount.text = "${filtered.size} accommodations found"
    }
}