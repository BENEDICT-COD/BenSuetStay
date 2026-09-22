package com.mansuetdev.bensuetstay

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.mansuetdev.bensuetstay.databinding.ActivityLandlordDashboardBinding
import com.mansuetdev.bensuetstay.databinding.ItemStatCardBinding

class LandlordDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLandlordDashboardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLandlordDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val landlord = LandlordRepository.loggedInLandlord
        if (landlord == null) {
            finish()
            return
        }

        binding.tvGreeting.text = "Welcome back, ${landlord.name.split(" ").firstOrNull() ?: landlord.name}"

        // Calculate real stats based on repository data
        val landlordListings = ListingRepository.listings.filter { it.assignedLandlordId == landlord.id }
        val landlordListingNames = landlordListings.map { it.name }

        val stats = LandlordStats(
            activeListings = landlordListings.size,
            pendingApplications = ApplicationRepository.applications.count { app ->
                landlordListingNames.contains(app.accommodationName) && app.status == ApplicationStatus.SUBMITTED
            },
            upcomingViewings = BookingRepository.bookings.count { booking ->
                landlordListingNames.contains(booking.accommodationName) && booking.status == BookingStatus.PENDING
            }
        )

        bindStat(binding.statActiveListings, stats.activeListings, "Active Listings")
        bindStat(binding.statPendingApplications, stats.pendingApplications, "Pending Applications")
        bindStat(binding.statUpcomingViewings, stats.upcomingViewings, "Upcoming Viewings")

        val myListings = ListingRepository.listings
            .filter { it.assignedLandlordId == landlord.id }
            .flatMap { listing ->
                FundingType.entries.mapNotNull { type ->
                    val roomsOfType = listing.rooms.filter { it.fundingType == type }
                    if (roomsOfType.isEmpty()) null
                    else {
                        val totalSpaces = roomsOfType.sumOf { it.spacesAvailable }
                        val priceLabel = if (type == FundingType.NSFAS) "NSFAS Funded"
                        else "From ${roomsOfType.minByOrNull { it.price }?.price ?: ""}"

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
                    }
                }
            }
        val adapter = AccommodationAdapter(myListings)
        binding.recyclerMyListings.layoutManager = LinearLayoutManager(this)
        binding.recyclerMyListings.adapter = adapter

        binding.bottomNavLandlord.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_dashboard -> true
                R.id.nav_applications -> {
                    startActivity(Intent(this, LandlordApplicationsActivity::class.java))
                    true
                }
                R.id.nav_bookings -> {
                    startActivity(Intent(this, LandlordBookingsActivity::class.java))
                    true
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, LandlordProfileActivity::class.java))
                    true
                }
                else -> false
            }
        }

        if (landlord.mustChangePassword) {
            showChangePasswordDialog()
        }
    }

    override fun onResume() {
        super.onResume()
        refreshStats()
    }

    private fun refreshStats() {
        val landlord = LandlordRepository.loggedInLandlord ?: return
        val landlordListings = ListingRepository.listings.filter { it.assignedLandlordId == landlord.id }
        val landlordListingNames = landlordListings.map { it.name }

        val stats = LandlordStats(
            activeListings = landlordListings.size,
            pendingApplications = ApplicationRepository.applications.count { app ->
                landlordListingNames.contains(app.accommodationName) && app.status == ApplicationStatus.SUBMITTED
            },
            upcomingViewings = BookingRepository.bookings.count { booking ->
                landlordListingNames.contains(booking.accommodationName) && booking.status == BookingStatus.PENDING
            }
        )

        binding.statActiveListings.removeAllViews()
        binding.statPendingApplications.removeAllViews()
        binding.statUpcomingViewings.removeAllViews()

        bindStat(binding.statActiveListings, stats.activeListings, "Active Listings")
        bindStat(binding.statPendingApplications, stats.pendingApplications, "Pending Applications")
        bindStat(binding.statUpcomingViewings, stats.upcomingViewings, "Upcoming Viewings")
    }

    private fun showChangePasswordDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_change_password, null)
        val etNewPassword = dialogView.findViewById<android.widget.EditText>(R.id.etNewPassword)
        
        android.app.AlertDialog.Builder(this)
            .setTitle("Change Temporary Password")
            .setMessage("For security, please change your temporary password before continuing.")
            .setView(dialogView)
            .setCancelable(false)
            .setPositiveButton("Update") { _, _ ->
                val newPassword = etNewPassword.text.toString().trim()
                if (newPassword.isNotEmpty()) {
                    val current = LandlordRepository.loggedInLandlord
                    if (current != null) {
                        val updated = current.copy(password = newPassword, mustChangePassword = false)
                        LandlordRepository.updateLandlord(updated)
                        LandlordRepository.loggedInLandlord = updated
                        android.widget.Toast.makeText(this, "Password updated successfully", android.widget.Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .show()
    }

    private fun bindStat(container: android.widget.FrameLayout, number: Int, label: String) {
        val itemBinding = ItemStatCardBinding.inflate(LayoutInflater.from(this), container, true)
        itemBinding.tvStatNumber.text = number.toString()
        itemBinding.tvStatLabel.text = label
    }
}