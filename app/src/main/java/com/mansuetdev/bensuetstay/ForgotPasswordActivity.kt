package com.mansuetdev.bensuetstay

import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import com.mansuetdev.bensuetstay.databinding.ActivityForgotPasswordBinding

/** Works for every role now that landlords and admins are real Firebase Auth users. */
class ForgotPasswordActivity : BaseActivity() {

    private lateinit var binding: ActivityForgotPasswordBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }
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
