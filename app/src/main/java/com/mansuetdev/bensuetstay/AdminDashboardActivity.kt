package com.mansuetdev.bensuetstay

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
<<<<<<< HEAD
import android.widget.FrameLayout
import com.mansuetdev.bensuetstay.databinding.ActivityAdminDashboardBinding
import com.mansuetdev.bensuetstay.databinding.ItemStatCardBinding

class AdminDashboardActivity : BaseActivity() {
=======
import androidx.appcompat.app.AppCompatActivity
import com.mansuetdev.bensuetstay.databinding.ActivityAdminDashboardBinding
import com.mansuetdev.bensuetstay.databinding.ItemStatCardBinding

class AdminDashboardActivity : AppCompatActivity() {
>>>>>>> d231b4d53e6f750ae3c0daf4f61493ce26eaf809

    private lateinit var binding: ActivityAdminDashboardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
<<<<<<< HEAD
        if (!requireRole(Role.ADMIN)) return
        binding = ActivityAdminDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Notifier.requestPermission(this)
=======
        binding = ActivityAdminDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        updateStats()
>>>>>>> d231b4d53e6f750ae3c0daf4f61493ce26eaf809

        binding.btnManageListings.setOnClickListener {
            startActivity(Intent(this, AdminListingsActivity::class.java))
        }
        binding.btnManageLandlords.setOnClickListener {
            startActivity(Intent(this, AdminLandlordsActivity::class.java))
        }
<<<<<<< HEAD

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
=======
        
        binding.bottomNavAdmin.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_admin_dashboard -> true
                R.id.nav_admin_listings -> {
                    startActivity(Intent(this, AdminListingsActivity::class.java))
                    true
                }
                R.id.nav_admin_landlords -> {
                    startActivity(Intent(this, AdminLandlordsActivity::class.java))
                    true
                }
                R.id.nav_admin_profile -> {
                    // Admin settings are now unified under the same UI as Landlord/Student
                    val intent = Intent(this, LandlordProfileActivity::class.java)
                    intent.putExtra("isAdmin", true)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }
>>>>>>> d231b4d53e6f750ae3c0daf4f61493ce26eaf809
    }

    override fun onResume() {
        super.onResume()
<<<<<<< HEAD
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
=======
        updateStats()
    }

    private fun updateStats() {
        val stats = AdminStats(
            totalListings = ListingRepository.listings.size,
            totalLandlords = LandlordRepository.landlords.size,
            pendingApprovals = LandlordRepository.landlords.count { !it.verified },
            totalStudents = StudentRepository.students.size,
            totalApplications = ApplicationRepository.applications.size,
            totalBookings = BookingRepository.bookings.size
        )
        
        binding.statListings.removeAllViews()
        binding.statLandlords.removeAllViews()
        binding.statPendingApprovals.removeAllViews()
        binding.statStudents.removeAllViews()
        binding.statApplications.removeAllViews()
        binding.statBookings.removeAllViews()

        bindStat(binding.statListings, stats.totalListings, "Total Listings")
        bindStat(binding.statLandlords, stats.totalLandlords, "Total Landlords")
        bindStat(binding.statPendingApprovals, stats.pendingApprovals, "Pending Approvals")
        bindStat(binding.statStudents, stats.totalStudents, "Total Students")
        bindStat(binding.statApplications, stats.totalApplications, "Applications")
        bindStat(binding.statBookings, stats.totalBookings, "Bookings")
    }

    private fun bindStat(container: android.widget.FrameLayout, number: Int, label: String) {
        val itemBinding = ItemStatCardBinding.inflate(LayoutInflater.from(this), container, true)
        itemBinding.tvStatNumber.text = number.toString()
        itemBinding.tvStatLabel.text = label
>>>>>>> d231b4d53e6f750ae3c0daf4f61493ce26eaf809
    }
}
