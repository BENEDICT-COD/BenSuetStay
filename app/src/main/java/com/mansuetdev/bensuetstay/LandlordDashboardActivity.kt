package com.mansuetdev.bensuetstay

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.mansuetdev.bensuetstay.databinding.ActivityLandlordDashboardBinding
import com.mansuetdev.bensuetstay.databinding.ItemStatCardBinding

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
                else -> false
            }
        }

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
