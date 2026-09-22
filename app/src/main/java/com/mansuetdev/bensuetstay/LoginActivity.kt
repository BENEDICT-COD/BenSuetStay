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
import com.mansuetdev.bensuetstay.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private var isPasswordVisible = false

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.ivTogglePassword.setOnClickListener {
            togglePasswordVisibility()
        }

        setupCreateAccountLink()

        binding.btnLogIn.setOnClickListener {
            attemptLogin()
        }

        binding.btnContinueAsGuest.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
        }

        binding.tvForgotPassword.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }
    }

    private fun attemptLogin() {
        val emailOrPhone = binding.etEmailPhone.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        if (emailOrPhone.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter your email and password", Toast.LENGTH_SHORT).show()
            return
        }

        // 1. Check for Admin (Stored in Firestore)
        val admin = AdminRepository.login(emailOrPhone, password)
        if (admin != null) {
            Toast.makeText(this, "Admin Login successful", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, AdminDashboardActivity::class.java))
            finish()
            return
        }

        // 2. Check for Landlord (Stored in Firestore, not Firebase Auth)
        val landlord = LandlordRepository.login(emailOrPhone, password)
        if (landlord != null) {
            Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LandlordDashboardActivity::class.java))
            finish()
            return
        }

        // 3. Everyone else (Students) goes through real Firebase Authentication.
        binding.btnLogIn.isEnabled = false
        binding.btnLogIn.text = "Logging in\u2026"

        auth.signInWithEmailAndPassword(emailOrPhone, password)
            .addOnSuccessListener { result ->
                val uid = result.user?.uid
                if (uid == null) {
                    resetLoginButton()
                    Toast.makeText(this, "Login failed: check your email or password", Toast.LENGTH_LONG).show()
                    return@addOnSuccessListener
                }

                firestore.collection("students").document(uid).get()
                    .addOnSuccessListener { doc ->
                        val profile = doc.toObject(StudentProfile::class.java)
                        if (profile != null) {
                            val funding = if (profile.fundingType == "NSFAS") FundingType.NSFAS else FundingType.SELF_OR_BURSARY
                            StudentRepository.loggedInStudent = Student(
                                id = uid,
                                fullName = profile.fullNames,
                                email = profile.email,
                                phone = "", 
                                address = profile.address,
                                password = "", 
                                fundingType = funding,
                                institution = profile.institution,
                                profileImageUri = profile.profileImageUri
                            )
                        }

                        Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, HomeActivity::class.java))
                        finishAffinity()
                    }
                    .addOnFailureListener {
                        // Signed in fine, but couldn't load their profile —
                        // still let them into the app rather than blocking them.
                        Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, HomeActivity::class.java))
                        finish()
                    }
            }
            .addOnFailureListener {
                resetLoginButton()
                Toast.makeText(this, "Login failed: check your email or password", Toast.LENGTH_LONG).show()
            }
    }

    private fun resetLoginButton() {
        binding.btnLogIn.isEnabled = true
        binding.btnLogIn.text = "Log In"
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

    private fun setupCreateAccountLink() {
        val fullText = "Don't have an account? Create one"
        val spannable = SpannableString(fullText)
        val startIndex = fullText.indexOf("Create one")

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
                startActivity(Intent(this@LoginActivity, RegisterActivity::class.java))
            }
        }, startIndex, fullText.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        binding.tvCreateAccountLink.text = spannable
        binding.tvCreateAccountLink.movementMethod = LinkMovementMethod.getInstance()
    }
}