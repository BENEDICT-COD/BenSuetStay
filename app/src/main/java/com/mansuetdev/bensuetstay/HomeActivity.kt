package com.mansuetdev.bensuetstay

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.mansuetdev.bensuetstay.databinding.ActivityHomeBinding

/**
 * Student homepage. Two independent filters plus search:
 *  - funding chips (All / NSFAS / Self funded), defaulting to the student's own funding type;
 *  - "Near my campus", on by default for signed-in students, matching their institution.
 * Guests see everything until they choose a filter.
 */
class HomeActivity : BaseActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var adapter: AccommodationAdapter

    private var fundingFilter: FundingType? = null
    private var nearCampus = false
    private var query = ""

    private val listingListener: () -> Unit = { render() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (restoreSessionIfNeeded()) return
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Session.student?.let { student ->
            // Defaults chosen in Settings → Home screen
            fundingFilter = if (AppSettings.homeOwnFunding(this)) student.fundingType else null
            nearCampus = AppSettings.homeNearCampus(this) && student.institution.isNotBlank()
        }

        adapter = AccommodationAdapter(emptyList())
        binding.recyclerAccommodations.layoutManager = LinearLayoutManager(this)
        binding.recyclerAccommodations.adapter = adapter

        binding.chipAll.setOnClickListener { fundingFilter = null; render() }
        binding.chipNsfas.setOnClickListener { fundingFilter = FundingType.NSFAS; render() }
        binding.chipSelfFunded.setOnClickListener { fundingFilter = FundingType.SELF_OR_BURSARY; render() }
        binding.chipNearCampus.setOnClickListener {
            if (Session.student?.institution.isNullOrBlank()) {
                toast(getString(R.string.log_in_as_a_student_to))
            } else {
                nearCampus = !nearCampus
                render()
            }
        }

        binding.btnLoginRegister.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                query = s?.toString().orEmpty()
                render()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        updateLoginState()
        setupBottomNavigation()
        if (Session.role == Role.STUDENT) Notifier.requestPermission(this)
    }

    override fun onStart() {
        super.onStart()
        ListingRepository.addListener(listingListener)
        render()
    }

    override fun onStop() {
        super.onStop()
        ListingRepository.removeListener(listingListener)
    }

    override fun onResume() {
        super.onResume()
        binding.bottomNav.menu.findItem(R.id.nav_home)?.isChecked = true
    }

    private fun updateLoginState() {
        val student = Session.student
        if (student != null) {
            binding.btnLoginRegister.visibility = View.GONE
            binding.tvWelcomeBack.visibility = View.VISIBLE
            binding.tvWelcomeBack.text = getString(R.string.welcome, student.fullName.substringBefore(" "))
        } else {
            binding.btnLoginRegister.visibility = View.VISIBLE
            binding.tvWelcomeBack.visibility = View.GONE
        }
    }

    private fun render() {
        styleChip(binding.chipAll, fundingFilter == null)
        styleChip(binding.chipNsfas, fundingFilter == FundingType.NSFAS)
        styleChip(binding.chipSelfFunded, fundingFilter == FundingType.SELF_OR_BURSARY)
        styleChip(binding.chipNearCampus, nearCampus)

        val campus = if (nearCampus) Session.student?.institution else null
        val items = ListingLogic.filter(
            ListingLogic.buildDisplayList(ListingRepository.listings),
            fundingFilter,
            campus,
            query
        )
        adapter.updateList(items)
        binding.tvResultsCount.text = when {
            !ListingRepository.isLoaded -> getString(R.string.loading_accommodations)
            items.isEmpty() && nearCampus -> getString(R.string.no_matches_near_your_campus_tap)
            items.size == 1 -> getString(R.string.n_1_accommodation_found)
            else -> getString(R.string.accommodations_found, items.size)
        }
        // Offline mode: listings still show from Firestore's on-device cache
        if (!SyncManager.isOnline(this)) binding.tvResultsCount.append("  \u00b7  " + getString(R.string.offline_showing_saved))
    }

    private fun styleChip(chip: TextView, selected: Boolean) {
        chip.setBackgroundResource(if (selected) R.drawable.bg_chip_selected else R.drawable.bg_chip_unselected)
        chip.setTextColor(ContextCompat.getColor(this, if (selected) R.color.brand_blue else R.color.text_muted))
    }

    private fun setupBottomNavigation() {
        binding.bottomNav.selectedItemId = R.id.nav_home
        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true
                R.id.nav_search -> {
                    binding.etSearch.requestFocus()
                    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.showSoftInput(binding.etSearch, InputMethodManager.SHOW_IMPLICIT)
                    false
                }
                R.id.nav_bookings -> {
                    openForStudent(StudentBookingsActivity::class.java); false
                }
                R.id.nav_profile -> {
                    openForStudent(StudentProfileActivity::class.java); false
                }
                else -> false
            }
        }
    }

    private fun openForStudent(target: Class<*>) {
        val next = if (Session.role == Role.STUDENT) target else LoginActivity::class.java
        startActivity(Intent(this, next))
    }
}
