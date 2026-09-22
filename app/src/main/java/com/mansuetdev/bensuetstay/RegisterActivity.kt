package com.mansuetdev.bensuetstay

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.InputType
import android.text.Spannable
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mansuetdev.bensuetstay.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private var isPasswordVisible = false
    private var isNsfasSelected = true

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.ivTogglePassword.setOnClickListener {
            togglePasswordVisibility()
        }

        binding.btnNsfas.setOnClickListener { selectFundingType(nsfas = true) }
        binding.btnSelfFunded.setOnClickListener { selectFundingType(nsfas = false) }

        setupLoginLink()

        binding.btnCreateAccount.setOnClickListener {
            attemptRegistration()
        }
    }

    private fun attemptRegistration() {
        val fullNames = binding.etFullNames.text.toString().trim()
        val address = binding.etAddress.text.toString().trim()
        val institution = binding.etInstitution.text.toString().trim()
        val email = binding.etEmailPhone.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        if (fullNames.isEmpty() || address.isEmpty() || institution.isEmpty() ||
            email.isEmpty() || password.isEmpty()
        ) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }
        if (password.length < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
            return
        }

        binding.btnCreateAccount.isEnabled = false
        binding.btnCreateAccount.text = "Creating account\u2026"

        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val uid = result.user?.uid ?: return@addOnSuccessListener
                val fundingLabel = if (isNsfasSelected) "NSFAS" else "SELF_FUNDED"

                val profile = StudentProfile(
                    uid = uid,
                    fullNames = fullNames,
                    address = address,
                    institution = institution,
                    email = email,
                    fundingType = fundingLabel
                )

                firestore.collection("students").document(uid)
                    .set(profile)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Account created! Please log in.", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, LoginActivity::class.java))
                        finish()
                    }
                    .addOnFailureListener { e ->
                        resetCreateButton()
                        Toast.makeText(this, "Saved login, but profile failed: ${e.message}", Toast.LENGTH_LONG).show()
                    }
            }
            .addOnFailureListener { e ->
                resetCreateButton()
                Toast.makeText(this, "Registration failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun resetCreateButton() {
        binding.btnCreateAccount.isEnabled = true
        binding.btnCreateAccount.text = "Create Account"
    }

    private fun togglePasswordVisibility() {
        isPasswordVisible = !isPasswordVisible
        val cursorPosition = binding.etPassword.text?.length ?: 0
        if (isPasswordVisible) {
            binding.etPassword.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            binding.ivTogglePassword.setImageResource(R.drawable.ic_visibility_off)
        } else {
            binding.etPassword.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            binding.ivTogglePassword.setImageResource(R.drawable.ic_visibility)
        }
        binding.etPassword.setSelection(cursorPosition)
    }

    private fun selectFundingType(nsfas: Boolean) {
        isNsfasSelected = nsfas
        if (nsfas) {
            binding.btnNsfas.setBackgroundResource(R.drawable.bg_toggle_left_active)
            binding.btnNsfas.setTextColor(ContextCompat.getColor(this, R.color.surface_white))
            binding.btnSelfFunded.setBackgroundColor(
                ContextCompat.getColor(this, android.R.color.transparent)
            )
            binding.btnSelfFunded.setTextColor(ContextCompat.getColor(this, R.color.text_muted))
        } else {
            binding.btnSelfFunded.setBackgroundResource(R.drawable.bg_toggle_right_active)
            binding.btnSelfFunded.setTextColor(ContextCompat.getColor(this, R.color.surface_white))
            binding.btnNsfas.setBackgroundColor(
                ContextCompat.getColor(this, android.R.color.transparent)
            )
            binding.btnNsfas.setTextColor(ContextCompat.getColor(this, R.color.text_muted))
        }
    }

    private fun setupLoginLink() {
        val fullText = "Already have an account? Log in"
        val spannable = SpannableString(fullText)
        val startIndex = fullText.indexOf("Log in")

        spannable.setSpan(
            ForegroundColorSpan(ContextCompat.getColor(this, R.color.brand_blue)),
            startIndex, fullText.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spannable.setSpan(
            StyleSpan(Typeface.BOLD),
            startIndex, fullText.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spannable.setSpan(object : ClickableSpan() {
            override fun onClick(widget: View) {
                startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
            }
        }, startIndex, fullText.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        binding.tvLoginLink.text = spannable
        binding.tvLoginLink.movementMethod = LinkMovementMethod.getInstance()
    }
}