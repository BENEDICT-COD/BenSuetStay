package com.mansuetdev.bensuetstay

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.mansuetdev.bensuetstay.databinding.ActivityAdminLandlordsBinding

class AdminLandlordsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminLandlordsBinding
    private lateinit var adapter: LandlordAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminLandlordsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }
        binding.btnAddLandlord.setOnClickListener {
            startActivity(Intent(this, AddLandlordActivity::class.java))
        }

        adapter = LandlordAdapter(
            landlords = LandlordRepository.landlords,
            onDelete = { landlord -> confirmDelete(landlord) },
            onResetPassword = { landlord -> confirmReset(landlord) }
        )
        binding.recyclerLandlords.layoutManager = LinearLayoutManager(this)
        binding.recyclerLandlords.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        // Refresh in case a new landlord was added on the previous screen
        adapter.updateList(LandlordRepository.landlords)
    }

    private fun confirmDelete(landlord: LandlordAccount) {
        // AlertDialog pauses the app and asks the user to confirm before we
        // do something we can't undo, like deleting an account.
        AlertDialog.Builder(this)
            .setTitle("Delete landlord?")
            .setMessage("This will remove ${landlord.name}'s account. This can't be undone.")
            .setPositiveButton("Delete") { _, _ ->
                LandlordRepository.deleteLandlord(landlord.id)
                adapter.updateList(LandlordRepository.landlords)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun confirmReset(landlord: LandlordAccount) {
        AlertDialog.Builder(this)
            .setTitle("Reset Password?")
            .setMessage("Reset ${landlord.name}'s password to 'Stay@2025'? They will be forced to change it on login.")
            .setPositiveButton("Reset") { _, _ ->
                LandlordRepository.resetPassword(landlord.id)
                adapter.updateList(LandlordRepository.landlords)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}