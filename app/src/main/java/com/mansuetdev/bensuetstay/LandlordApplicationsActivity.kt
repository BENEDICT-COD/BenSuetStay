package com.mansuetdev.bensuetstay

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.mansuetdev.bensuetstay.databinding.ActivityLandlordApplicationsBinding

class LandlordApplicationsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLandlordApplicationsBinding
    private lateinit var adapter: ApplicationAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLandlordApplicationsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }

        adapter = ApplicationAdapter(ApplicationRepository.applications)
        binding.recyclerApplications.layoutManager = LinearLayoutManager(this)
        binding.recyclerApplications.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        // Refresh in case a status changed on the detail screen
        adapter.updateList(ApplicationRepository.applications)
    }
}