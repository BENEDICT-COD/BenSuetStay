package com.mansuetdev.bensuetstay

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.mansuetdev.bensuetstay.databinding.ActivityLandlordApplicationsBinding

/** Applications for this landlord's listings only. */
class LandlordApplicationsActivity : BaseActivity() {

    private lateinit var binding: ActivityLandlordApplicationsBinding
    private lateinit var adapter: ApplicationAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!requireRole(Role.LANDLORD)) return
        binding = ActivityLandlordApplicationsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.btnBack.setOnClickListener { finish() }

        adapter = ApplicationAdapter(emptyList(), forStudent = false)
        binding.recyclerApplications.layoutManager = LinearLayoutManager(this)
        binding.recyclerApplications.adapter = adapter
    }

    override fun onStart() {
        super.onStart()
        val uid = Session.landlord?.id ?: return
        ApplicationRepository.listenForLandlord(this, uid,
            onChange = { apps ->
                // Undecided first, newest first within each group
                val undecided = setOf(ApplicationStatus.SUBMITTED, ApplicationStatus.VIEWED)
                adapter.updateList(apps.sortedWith(compareBy({ it.status !in undecided }, { -it.createdAtMillis })))
                showEmpty(if (apps.isEmpty()) getString(R.string.no_applications_yet) else null)
            },
            onError = { e -> showEmpty(AuthErrors.message(this, e)) }
        )
    }

    private fun showEmpty(message: String?) {
        binding.tvEmpty.visibility = if (message == null) View.GONE else View.VISIBLE
        binding.tvEmpty.text = message.orEmpty()
    }
}
