package com.mansuetdev.bensuetstay

import android.os.Bundle
<<<<<<< HEAD
import androidx.appcompat.app.AlertDialog
import com.mansuetdev.bensuetstay.databinding.ActivityAddLandlordBinding

class AddLandlordActivity : BaseActivity() {
=======
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.mansuetdev.bensuetstay.databinding.ActivityAddLandlordBinding
import java.util.UUID

class AddLandlordActivity : AppCompatActivity() {
>>>>>>> d231b4d53e6f750ae3c0daf4f61493ce26eaf809

    private lateinit var binding: ActivityAddLandlordBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
<<<<<<< HEAD
        if (!requireRole(Role.ADMIN)) return
=======
>>>>>>> d231b4d53e6f750ae3c0daf4f61493ce26eaf809
        binding = ActivityAddLandlordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }
<<<<<<< HEAD
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
=======

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
>>>>>>> d231b4d53e6f750ae3c0daf4f61493ce26eaf809
