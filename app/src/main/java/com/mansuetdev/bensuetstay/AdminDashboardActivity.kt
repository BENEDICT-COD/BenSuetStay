package com.mansuetdev.bensuetstay

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import com.mansuetdev.bensuetstay.databinding.ActivityAdminDashboardBinding
import com.mansuetdev.bensuetstay.databinding.ItemStatCardBinding

class AdminDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminDashboardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        updateStats()

        binding.btnManageListings.setOnClickListener {
            startActivity(Intent(this, AdminListingsActivity::class.java))
        }
        binding.btnManageLandlords.setOnClickListener {
            startActivity(Intent(this, AdminLandlordsActivity::class.java))
        }
        
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
    }

    override fun onResume() {
        super.onResume()
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
    }
}
