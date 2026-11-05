package com.mansuetdev.bensuetstay

import android.os.Bundle
<<<<<<< HEAD
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.mansuetdev.bensuetstay.databinding.ActivityLandlordApplicationsBinding

/** Applications for this landlord's listings only. */
class LandlordApplicationsActivity : BaseActivity() {
=======
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.mansuetdev.bensuetstay.databinding.ActivityLandlordApplicationsBinding

class LandlordApplicationsActivity : AppCompatActivity() {
>>>>>>> d231b4d53e6f750ae3c0daf4f61493ce26eaf809

    private lateinit var binding: ActivityLandlordApplicationsBinding
    private lateinit var adapter: ApplicationAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
<<<<<<< HEAD
        if (!requireRole(Role.LANDLORD)) return
        binding = ActivityLandlordApplicationsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.btnBack.setOnClickListener { finish() }

        adapter = ApplicationAdapter(emptyList(), forStudent = false)
=======
        binding = ActivityLandlordApplicationsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }

        adapter = ApplicationAdapter(ApplicationRepository.applications)
>>>>>>> d231b4d53e6f750ae3c0daf4f61493ce26eaf809
        binding.recyclerApplications.layoutManager = LinearLayoutManager(this)
        binding.recyclerApplications.adapter = adapter
    }

<<<<<<< HEAD
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
=======
    override fun onResume() {
        super.onResume()
        // Refresh in case a status changed on the detail screen
        adapter.updateList(ApplicationRepository.applications)
    }
}
>>>>>>> d231b4d53e6f750ae3c0daf4f61493ce26eaf809
