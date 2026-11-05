package com.mansuetdev.bensuetstay

import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.mansuetdev.bensuetstay.databinding.ActivityAdminLandlordsBinding

class AdminLandlordsActivity : BaseActivity() {

    private lateinit var binding: ActivityAdminLandlordsBinding
    private lateinit var adapter: LandlordAdapter
    private var landlords: List<LandlordAccount> = emptyList()
    private val listingListener: () -> Unit = { adapter.updateList(landlords) } // refresh listing counts

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!requireRole(Role.ADMIN)) return
        binding = ActivityAdminLandlordsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }
        binding.btnAddLandlord.setOnClickListener {
            startActivity(Intent(this, AddLandlordActivity::class.java))
        }

        adapter = LandlordAdapter(
            landlords = emptyList(),
            onDelete = { landlord -> confirmDelete(landlord) },
            onResetPassword = { landlord -> confirmReset(landlord) }
        )
        binding.recyclerLandlords.layoutManager = LinearLayoutManager(this)
        binding.recyclerLandlords.adapter = adapter
    }

    override fun onStart() {
        super.onStart()
        ListingRepository.addListener(listingListener)
        LandlordRepository.listenAll(this,
            onChange = { list -> landlords = list; adapter.updateList(list) },
            onError = { e -> toast(AuthErrors.message(this, e), long = true) }
        )
    }

    override fun onStop() {
        super.onStop()
        ListingRepository.removeListener(listingListener)
    }

    private fun confirmDelete(landlord: LandlordAccount) {
        val listingCount = ListingRepository.listingsForLandlord(landlord.id).size
        val unassignNote = if (listingCount > 0) getString(R.string.their_listing_s_will_become_unassigned, listingCount) else ""
        confirm(
            title = getString(R.string.delete_landlord_2),
            message = getString(R.string.will_no_longer_be_able_to, landlord.name, unassignNote),
            positive = getString(R.string.delete)
        ) {
            LandlordRepository.delete(landlord.id)
                .addOnSuccessListener {
                    toast(getString(R.string.removed_to_reuse_their_email_also, landlord.name), long = true)
                }
                .addOnFailureListener { e -> toast(AuthErrors.message(this, e), long = true) }
        }
    }

    private fun confirmReset(landlord: LandlordAccount) {
        confirm(
            title = getString(R.string.send_password_reset),
            message = getString(R.string.will_get_an_email_at_with, landlord.name, landlord.email),
            positive = getString(R.string.send)
        ) {
            LandlordRepository.sendPasswordReset(landlord.email)
                .addOnSuccessListener { toast(getString(R.string.reset_email_sent_to, landlord.email)) }
                .addOnFailureListener { e -> toast(AuthErrors.message(this, e), long = true) }
        }
    }
}
