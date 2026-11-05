package com.mansuetdev.bensuetstay

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.mansuetdev.bensuetstay.databinding.ActivityAdminDashboardBinding
import com.mansuetdev.bensuetstay.databinding.ItemStatCardBinding

class AdminDashboardActivity : BaseActivity() {

    private lateinit var binding: ActivityAdminDashboardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!requireRole(Role.ADMIN)) return
        binding = ActivityAdminDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Notifier.requestPermission(this)

        binding.btnManageListings.setOnClickListener {
            startActivity(Intent(this, AdminListingsActivity::class.java))
        }
        binding.btnManageLandlords.setOnClickListener {
            startActivity(Intent(this, AdminLandlordsActivity::class.java))
        }

        binding.bottomNavAdmin.selectedItemId = R.id.nav_admin_dashboard
        binding.bottomNavAdmin.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_admin_dashboard -> true
                R.id.nav_admin_listings -> { startActivity(Intent(this, AdminListingsActivity::class.java)); false }
                R.id.nav_admin_landlords -> { startActivity(Intent(this, AdminLandlordsActivity::class.java)); false }
                R.id.nav_admin_profile -> { startActivity(Intent(this, LandlordProfileActivity::class.java)); false }
                else -> false
            }
        }
        bindStats(null)
    }

    override fun onResume() {
        super.onResume()
        if (Session.role != Role.ADMIN) return
        AdminRepository.loadStats()
            .addOnSuccessListener { stats -> bindStats(stats) }
            .addOnFailureListener { e -> toast(getString(R.string.couldn_t_load_stats, AuthErrors.message(this, e))) }
    }

    /** null shows placeholders while loading. */
    private fun bindStats(stats: AdminStats?) {
        bindStat(binding.statListings, stats?.totalListings, getString(R.string.total_listings))
        bindStat(binding.statLandlords, stats?.totalLandlords, getString(R.string.total_landlords))
        bindStat(binding.statPendingApprovals, stats?.pendingApprovals, getString(R.string.unverified_landlords))
        bindStat(binding.statStudents, stats?.totalStudents, getString(R.string.total_students))
        bindStat(binding.statApplications, stats?.totalApplications, getString(R.string.applications))
        bindStat(binding.statBookings, stats?.totalBookings, getString(R.string.bookings))
    }

    private fun bindStat(container: FrameLayout, number: Int?, label: String) {
        container.removeAllViews()
        val item = ItemStatCardBinding.inflate(LayoutInflater.from(this), container, true)
        item.tvStatNumber.text = number?.toString() ?: "\u2013"
        item.tvStatLabel.text = label
    }
}
