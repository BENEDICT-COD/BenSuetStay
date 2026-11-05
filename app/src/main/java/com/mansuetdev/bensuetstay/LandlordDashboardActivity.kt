package com.mansuetdev.bensuetstay

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
<<<<<<< HEAD
import android.widget.FrameLayout
=======
import androidx.appcompat.app.AppCompatActivity
>>>>>>> d231b4d53e6f750ae3c0daf4f61493ce26eaf809
import androidx.recyclerview.widget.LinearLayoutManager
import com.mansuetdev.bensuetstay.databinding.ActivityLandlordDashboardBinding
import com.mansuetdev.bensuetstay.databinding.ItemStatCardBinding

<<<<<<< HEAD
class LandlordDashboardActivity : BaseActivity() {

    private lateinit var binding: ActivityLandlordDashboardBinding
    private lateinit var adapter: AccommodationAdapter

    private var applications: List<StudentApplication> = emptyList()
    private var bookings: List<Booking> = emptyList()
    private var passwordDialogShown = false

    private val listingListener: () -> Unit = { render() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!requireRole(Role.LANDLORD)) return
        binding = ActivityLandlordDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Notifier.requestPermission(this)

        val landlord = Session.landlord ?: return
        binding.tvGreeting.text = getString(R.string.welcome_back_2, landlord.name.substringBefore(" ").ifBlank { landlord.name })

        adapter = AccommodationAdapter(emptyList())
        binding.recyclerMyListings.layoutManager = LinearLayoutManager(this)
        binding.recyclerMyListings.adapter = adapter

        binding.bottomNavLandlord.selectedItemId = R.id.nav_dashboard
        binding.bottomNavLandlord.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_dashboard -> true
                R.id.nav_applications -> { startActivity(Intent(this, LandlordApplicationsActivity::class.java)); false }
                R.id.nav_bookings -> { startActivity(Intent(this, LandlordBookingsActivity::class.java)); false }
                R.id.nav_profile -> { startActivity(Intent(this, LandlordProfileActivity::class.java)); false }
=======
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
>>>>>>> d231b4d53e6f750ae3c0daf4f61493ce26eaf809
                else -> false
            }
        }

<<<<<<< HEAD
        if (landlord.mustChangePassword) showForcedPasswordChange()
    }

    override fun onStart() {
        super.onStart()
        val uid = Session.landlord?.id ?: return
        ListingRepository.addListener(listingListener)
        // Activity-scoped listeners: Firestore removes them in onStop
        ApplicationRepository.listenForLandlord(this, uid,
            onChange = { applications = it; render() },
            onError = { e -> toast(AuthErrors.message(this, e)) }
        )
        BookingRepository.listenForLandlord(this, uid,
            onChange = { bookings = it; render() },
            onError = { e -> toast(AuthErrors.message(this, e)) }
        )
        render()
    }

    override fun onStop() {
        super.onStop()
        ListingRepository.removeListener(listingListener)
    }

    private fun render() {
        val uid = Session.landlord?.id ?: return
        val myListings = ListingRepository.listingsForLandlord(uid)
        val now = System.currentTimeMillis()
        val stats = LandlordStats(
            activeListings = myListings.size,
            pendingApplications = applications.count {
                it.status == ApplicationStatus.SUBMITTED || it.status == ApplicationStatus.VIEWED
            },
            upcomingViewings = bookings.count { it.status != BookingStatus.DECLINED && it.requestedAtMillis >= now }
        )
        bindStat(binding.statActiveListings, stats.activeListings, getString(R.string.active_listings))
        bindStat(binding.statPendingApplications, stats.pendingApplications, getString(R.string.pending_applications))
        bindStat(binding.statUpcomingViewings, stats.upcomingViewings, getString(R.string.upcoming_viewings))
        adapter.updateList(ListingLogic.buildDisplayList(myListings))
    }

    private fun bindStat(container: FrameLayout, number: Int, label: String) {
        container.removeAllViews()
        val item = ItemStatCardBinding.inflate(LayoutInflater.from(this), container, true)
        item.tvStatNumber.text = number.toString()
        item.tvStatLabel.text = label
    }

    private fun showForcedPasswordChange() {
        if (passwordDialogShown) return
        passwordDialogShown = true
        PasswordDialog.show(this, forced = true) {
            val landlord = Session.landlord ?: return@show
            LandlordRepository.markPasswordChanged(landlord.id)
                .addOnSuccessListener { Session.setLandlord(landlord.copy(mustChangePassword = false)) }
                .addOnFailureListener { e -> toast(AuthErrors.message(this, e)) }
        }
    }
}
=======
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
>>>>>>> d231b4d53e6f750ae3c0daf4f61493ce26eaf809
