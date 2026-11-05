package com.mansuetdev.bensuetstay

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import com.mansuetdev.bensuetstay.databinding.ActivityAddLandlordBinding

class AddLandlordActivity : BaseActivity() {

    private lateinit var binding: ActivityAddLandlordBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!requireRole(Role.ADMIN)) return
        binding = ActivityAddLandlordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }
        binding.btnCreateLandlord.setOnClickListener { createLandlord() }
    }

    private fun createLandlord() {
        val name = binding.etName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val phone = binding.etPhone.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        when {
            name.isEmpty() -> { binding.etName.error = getString(R.string.enter_the_landlord_s_name); return }
            !Validators.isValidEmail(email) -> { binding.etEmail.error = getString(R.string.enter_a_valid_email_address); return }
            !Validators.isValidSaPhone(phone) -> { binding.etPhone.error = getString(R.string.enter_a_valid_sa_number_e); return }
            password.length < Validators.MIN_PASSWORD_LENGTH -> {
                binding.etPassword.error = getString(R.string.temporary_password_needs_at_least_characters, Validators.MIN_PASSWORD_LENGTH); return
            }
        }

        setLoading(true)
        LandlordRepository.createAccount(this, name, email, phone, password)
            .addOnSuccessListener {
                AlertDialog.Builder(this)
                    .setTitle(getString(R.string.account_created))
                    .setMessage(
                        getString(R.string.share_these_login_details_with_email, name, email, password) +
                            getString(R.string.they_ll_be_asked_to_choose)
                    )
                    .setCancelable(false)
                    .setPositiveButton(getString(R.string.done)) { _, _ -> finish() }
                    .show()
            }
            .addOnFailureListener { e ->
                setLoading(false)
                toast(AuthErrors.message(this, e), long = true)
            }
    }

    private fun setLoading(loading: Boolean) {
        binding.btnCreateLandlord.isEnabled = !loading
        binding.btnCreateLandlord.text = if (loading) getString(R.string.creating) else getString(R.string.create_landlord_account)
    }
}
