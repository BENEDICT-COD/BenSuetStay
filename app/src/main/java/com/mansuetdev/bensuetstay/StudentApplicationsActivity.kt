package com.mansuetdev.bensuetstay

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.mansuetdev.bensuetstay.databinding.ActivityLandlordApplicationsBinding

/** Lets students see the status of what they applied for, and the landlord's feedback. */
class StudentApplicationsActivity : BaseActivity() {

    // Same layout as the landlord's list; only the title and row content differ
    private lateinit var binding: ActivityLandlordApplicationsBinding
    private lateinit var adapter: ApplicationAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!requireRole(Role.STUDENT)) return
        binding = ActivityLandlordApplicationsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.tvTitle.text = getString(R.string.my_applications)
        binding.btnBack.setOnClickListener { finish() }

        adapter = ApplicationAdapter(emptyList(), forStudent = true)
        binding.recyclerApplications.layoutManager = LinearLayoutManager(this)
        binding.recyclerApplications.adapter = adapter
    }

    private var fromServer: List<StudentApplication> = emptyList()
    private val syncListener: () -> Unit = { render() }

    override fun onStart() {
        super.onStart()
        val uid = Session.student?.id ?: return
        SyncManager.addListener(syncListener)
        SyncManager.syncNow(this)
        render()
        // Activity-scoped: Firestore removes this listener in onStop (and serves cached data offline)
        ApplicationRepository.listenForStudent(this, uid,
            onChange = { items ->
                fromServer = items
                render()
            },
            onError = { e -> showEmpty(AuthErrors.message(this, e)) }
        )
    }

    override fun onStop() {
        super.onStop()
        SyncManager.removeListener(syncListener)
    }

    /** Items from Firestore plus anything still waiting in the offline (SQLite) outbox. */
    private fun render() {
        val uid = Session.student?.id ?: return
        val queued = SyncManager.queuedApplications(this, uid)
        val queuedIds = queued.map { it.id }.toSet()
        val all = queued + fromServer.filter { it.id !in queuedIds }
        adapter.updateList(all)
        showEmpty(if (all.isEmpty()) getString(R.string.you_haven_t_applied_for_a) else null)
    }

    private fun showEmpty(message: String?) {
        binding.tvEmpty.visibility = if (message == null) View.GONE else View.VISIBLE
        binding.tvEmpty.text = message.orEmpty()
    }
}
