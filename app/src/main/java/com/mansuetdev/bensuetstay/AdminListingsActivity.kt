package com.mansuetdev.bensuetstay

<<<<<<< HEAD
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
=======
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
>>>>>>> d231b4d53e6f750ae3c0daf4f61493ce26eaf809
        binding = ActivityAdminListingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }
        binding.btnAddListing.setOnClickListener {
            startActivity(Intent(this, AddEditListingActivity::class.java))
        }

        adapter = AdminListingAdapter(
<<<<<<< HEAD
            listings = emptyList(),
            onEdit = { listing ->
                startActivity(
                    Intent(this, AddEditListingActivity::class.java)
                        .putExtra(AddEditListingActivity.EXTRA_LISTING_ID, listing.id)
                )
=======
            listings = ListingRepository.listings,
            onEdit = { listing ->
                val intent = Intent(this, AddEditListingActivity::class.java)
                intent.putExtra("listing_id", listing.id)
                startActivity(intent)
>>>>>>> d231b4d53e6f750ae3c0daf4f61493ce26eaf809
            },
            onDelete = { listing -> confirmDelete(listing) }
        )
        binding.recyclerListings.layoutManager = LinearLayoutManager(this)
        binding.recyclerListings.adapter = adapter
    }

<<<<<<< HEAD
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
=======
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
>>>>>>> d231b4d53e6f750ae3c0daf4f61493ce26eaf809
