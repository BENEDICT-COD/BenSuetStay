package com.mansuetdev.bensuetstay

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.mansuetdev.bensuetstay.databinding.ActivityStudentBookingsBinding

class StudentBookingsActivity : BaseActivity() {

    private lateinit var binding: ActivityStudentBookingsBinding
    private lateinit var adapter: BookingAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!requireRole(Role.STUDENT)) return
        binding = ActivityStudentBookingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.btnBack.setOnClickListener { finish() }

        adapter = BookingAdapter(emptyList(), isLandlord = false)
        binding.recyclerBookings.layoutManager = LinearLayoutManager(this)
        binding.recyclerBookings.adapter = adapter
    }

    private var fromServer: List<Booking> = emptyList()
    private val syncListener: () -> Unit = { render() }

    override fun onStart() {
        super.onStart()
        val uid = Session.student?.id ?: return
        SyncManager.addListener(syncListener)
        SyncManager.syncNow(this)
        render()
        // Activity-scoped: Firestore removes this listener in onStop (and serves cached data offline)
        BookingRepository.listenForStudent(this, uid,
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
        val queued = SyncManager.queuedBookings(this, uid)
        val queuedIds = queued.map { it.id }.toSet()
        val all = queued + fromServer.filter { it.id !in queuedIds }
        adapter.updateList(all)
        showEmpty(if (all.isEmpty()) getString(R.string.no_viewings_booked_yet_open_a) else null)
    }

    private fun showEmpty(message: String?) {
        binding.tvEmpty.visibility = if (message == null) View.GONE else View.VISIBLE
        binding.tvEmpty.text = message.orEmpty()
    }
}
