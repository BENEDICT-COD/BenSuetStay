package com.mansuetdev.bensuetstay

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.mansuetdev.bensuetstay.databinding.ActivityForgotPasswordBinding

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityForgotPasswordBinding
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }

        binding.btnResetPassword.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()

            if (email.isEmpty()) {
                Toast.makeText(this, "Please enter your email address", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            binding.btnResetPassword.isEnabled = false
            binding.btnResetPassword.text = "Sending..."

            // Check if it's a landlord first
            val isLandlord = LandlordRepository.landlords.any { it.email.equals(email, ignoreCase = true) }
            if (isLandlord) {
                binding.btnResetPassword.isEnabled = true
                binding.btnResetPassword.text = "Send Reset Link"
                Toast.makeText(this, "Landlord accounts must be reset by the Administrator.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            auth.sendPasswordResetEmail(email)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Reset link sent! Please check your inbox and spam folder.", Toast.LENGTH_LONG).show()
                        finish()
                    } else {
                        binding.btnResetPassword.isEnabled = true
                        binding.btnResetPassword.text = "Send Reset Link"
                        val errorMessage = task.exception?.message ?: "Unknown error occurred"
                        Toast.makeText(this, "Error: $errorMessage", Toast.LENGTH_LONG).show()
                    }
                }
        }
    }
}