package com.mansuetdev.bensuetstay

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.mansuetdev.bensuetstay.databinding.ActivityAddLandlordBinding
import java.util.UUID

class AddLandlordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddLandlordBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddLandlordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }

        binding.btnCreateLandlord.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val phone = binding.etPhone.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Password isn't stored anywhere yet — real account creation
            // (with proper hashing) happens once Firebase Auth is connected.
            val newLandlord = LandlordAccount(
                id = UUID.randomUUID().toString(),
                name = name,
                email = email,
                phone = phone,
                verified = true,
                listingsCount = 0,
                password = password,
                mustChangePassword = true
            )
            LandlordRepository.addLandlord(newLandlord)

            Toast.makeText(this, "$name's account was created", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}