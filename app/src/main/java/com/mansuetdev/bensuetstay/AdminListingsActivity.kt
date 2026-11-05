package com.mansuetdev.bensuetstay

import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.mansuetdev.bensuetstay.databinding.ActivityAdminListingsBinding

class AdminListingsActivity : BaseActivity() {

    private lateinit var binding: ActivityAdminListingsBinding
    private lateinit var adapter: AdminListingAdapter
    private val listingListener: () -> Unit = { adapter.updateList(ListingRepository.listings) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!requireRole(Role.ADMIN)) return
        binding = ActivityAdminListingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }
        binding.btnAddListing.setOnClickListener {
            startActivity(Intent(this, AddEditListingActivity::class.java))
        }

        adapter = AdminListingAdapter(
            listings = emptyList(),
            onEdit = { listing ->
                startActivity(
                    Intent(this, AddEditListingActivity::class.java)
                        .putExtra(AddEditListingActivity.EXTRA_LISTING_ID, listing.id)
                )
            },
            onDelete = { listing -> confirmDelete(listing) }
        )
        binding.recyclerListings.layoutManager = LinearLayoutManager(this)
        binding.recyclerListings.adapter = adapter
    }

    override fun onStart() {
        super.onStart()
        ListingRepository.addListener(listingListener)
        adapter.updateList(ListingRepository.listings)
    }

    override fun onStop() {
        super.onStop()
        ListingRepository.removeListener(listingListener)
    }

    private fun confirmDelete(listing: AdminListing) {
        confirm(
            title = getString(R.string.delete_listing),
            message = getString(R.string.this_will_remove_from_the_app, listing.name),
            positive = getString(R.string.delete)
        ) {
            ListingRepository.delete(listing.id)
                .addOnSuccessListener { toast("${listing.name} deleted") }
                .addOnFailureListener { e -> toast(AuthErrors.message(this, e), long = true) }
        }
    }
}
