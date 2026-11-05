package com.mansuetdev.bensuetstay

import android.os.Bundle
<<<<<<< HEAD
import com.google.firebase.auth.FirebaseAuth
import com.mansuetdev.bensuetstay.databinding.ActivityForgotPasswordBinding

/** Works for every role now that landlords and admins are real Firebase Auth users. */
class ForgotPasswordActivity : BaseActivity() {

    private lateinit var binding: ActivityForgotPasswordBinding
=======
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.mansuetdev.bensuetstay.databinding.ActivityForgotPasswordBinding

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityForgotPasswordBinding
    private val auth = FirebaseAuth.getInstance()
>>>>>>> d231b4d53e6f750ae3c0daf4f61493ce26eaf809

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }
<<<<<<< HEAD
        binding.btnResetPassword.setOnClickListener { sendReset() }
    }

    private fun sendReset() {
        val email = binding.etEmail.text.toString().trim()
        if (!Validators.isValidEmail(email)) {
            binding.etEmail.error = getString(R.string.enter_a_valid_email_address)
            return
        }
        setLoading(true)
        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
            .addOnSuccessListener {
                // Same message whether or not the account exists, so emails can't be probed
                toast(getString(R.string.if_an_account_exists_for_a, email), long = true)
                finish()
            }
            .addOnFailureListener { e ->
                setLoading(false)
                toast(AuthErrors.message(this, e), long = true)
            }
    }

    private fun setLoading(loading: Boolean) {
        binding.btnResetPassword.isEnabled = !loading
        binding.btnResetPassword.text = if (loading) getString(R.string.sending) else getString(R.string.send_reset_link)
    }
}
=======

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
>>>>>>> d231b4d53e6f750ae3c0daf4f61493ce26eaf809
