package com.mansuetdev.bensuetstay

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.mansuetdev.bensuetstay.databinding.ActivityAdminListingsBinding

class AdminListingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminListingsBinding
    private lateinit var adapter: AdminListingAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminListingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }
        binding.btnAddListing.setOnClickListener {
            startActivity(Intent(this, AddEditListingActivity::class.java))
        }

        adapter = AdminListingAdapter(
            listings = ListingRepository.listings,
            onEdit = { listing ->
                val intent = Intent(this, AddEditListingActivity::class.java)
                intent.putExtra("listing_id", listing.id)
                startActivity(intent)
            },
            onDelete = { listing -> confirmDelete(listing) }
        )
        binding.recyclerListings.layoutManager = LinearLayoutManager(this)
        binding.recyclerListings.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        adapter.updateList(ListingRepository.listings)
    }

    private fun confirmDelete(listing: AdminListing) {
        AlertDialog.Builder(this)
            .setTitle("Delete listing?")
            .setMessage("This will remove ${listing.name} from the app. This can't be undone.")
            .setPositiveButton("Delete") { _, _ ->
                ListingRepository.deleteListing(listing.id)
                adapter.updateList(ListingRepository.listings)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}