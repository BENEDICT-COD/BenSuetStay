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
<<<<<<< HEAD
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.mansuetdev.bensuetstay.databinding.ActivityLoginBinding

/** One login for all three roles: Firebase Auth checks the password, Session works out the role. */
class LoginActivity : BaseActivity() {

    private lateinit var binding: ActivityLoginBinding
    private var isPasswordVisible = false
    private val auth = FirebaseAuth.getInstance()
=======
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
>>>>>>> d231b4d53e6f750ae3c0daf4f61493ce26eaf809

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

<<<<<<< HEAD
        binding.btnBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
        binding.ivTogglePassword.setOnClickListener { togglePasswordVisibility() }
        binding.btnLogIn.setOnClickListener { attemptLogin() }
        binding.btnGoogle.setOnClickListener { signInWithGoogle() }
        binding.btnContinueAsGuest.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
        }
        binding.tvForgotPassword.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }
        setupCreateAccountLink()
    }

    private fun attemptLogin() {
        val email = binding.etEmailPhone.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        if (!Validators.isValidEmail(email)) {
            binding.etEmailPhone.error = getString(R.string.enter_a_valid_email_address)
            return
        }
        if (password.isEmpty()) {
            binding.etPassword.error = getString(R.string.enter_your_password)
            return
        }

        setLoading(true)
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val uid = result.user?.uid
                if (uid == null) {
                    setLoading(false)
                    toast(getString(R.string.login_failed_please_try_again))
                    return@addOnSuccessListener
                }
                Session.load(
                    uid,
                    onLoaded = { role ->
                        if (role == null) {
                            Session.signOut()
                            setLoading(false)
                            toast(getString(R.string.this_account_isn_t_active_contact), long = true)
                        } else {
                            Navigator.openHome(this, role)
                        }
                    },
                    onFailed = { e ->
                        Session.signOut()
                        setLoading(false)
                        toast(getString(R.string.signed_in_but_your_profile_couldn, AuthErrors.message(this, e)), long = true)
                    }
                )
            }
            .addOnFailureListener { e ->
                setLoading(false)
                toast(AuthErrors.message(this, e), long = true)
            }
    }

    private fun signInWithGoogle() {
        binding.btnGoogle.isEnabled = false
        GoogleSignIn.signIn(this,
            onSignedIn = { user ->
                GoogleSignIn.route(this, user,
                    onNewUser = {
                        // New Google user: finish a short student sign-up (no password needed)
                        startActivity(Intent(this, RegisterActivity::class.java).putExtra(RegisterActivity.EXTRA_COMPLETE_GOOGLE, true))
                        binding.btnGoogle.isEnabled = true
                    },
                    onError = { message -> binding.btnGoogle.isEnabled = true; toast(message, long = true) })
            },
            onError = { message ->
                binding.btnGoogle.isEnabled = true
                if (message != null) toast(message, long = true)
            })
    }

    private fun setLoading(loading: Boolean) {
        binding.btnLogIn.isEnabled = !loading
        binding.btnLogIn.text = if (loading) getString(R.string.logging_in) else getString(R.string.log_in)
=======
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
>>>>>>> d231b4d53e6f750ae3c0daf4f61493ce26eaf809
    }

    private fun togglePasswordVisibility() {
        isPasswordVisible = !isPasswordVisible
        val cursorPosition = binding.etPassword.text?.length ?: 0
        if (isPasswordVisible) {
<<<<<<< HEAD
            binding.etPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            binding.ivTogglePassword.setImageResource(R.drawable.ic_visibility_off)
        } else {
            binding.etPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
=======
            binding.etPassword.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            binding.ivTogglePassword.setImageResource(R.drawable.ic_visibility_off)
        } else {
            binding.etPassword.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
>>>>>>> d231b4d53e6f750ae3c0daf4f61493ce26eaf809
            binding.ivTogglePassword.setImageResource(R.drawable.ic_visibility)
        }
        binding.etPassword.setSelection(cursorPosition)
    }

    private fun setupCreateAccountLink() {
<<<<<<< HEAD
        val prompt = getString(R.string.no_account_prompt)
        val fullText = prompt + " " + getString(R.string.create_one_link)
        val spannable = SpannableString(fullText)
        val startIndex = prompt.length + 1
=======
        val fullText = "Don't have an account? Create one"
        val spannable = SpannableString(fullText)
        val startIndex = fullText.indexOf("Create one")

>>>>>>> d231b4d53e6f750ae3c0daf4f61493ce26eaf809
        spannable.setSpan(
            ForegroundColorSpan(ContextCompat.getColor(this, R.color.brand_blue)),
            startIndex, fullText.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
<<<<<<< HEAD
        spannable.setSpan(StyleSpan(Typeface.BOLD), startIndex, fullText.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
=======
        spannable.setSpan(
            StyleSpan(Typeface.BOLD),
            startIndex, fullText.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
>>>>>>> d231b4d53e6f750ae3c0daf4f61493ce26eaf809
        spannable.setSpan(object : ClickableSpan() {
            override fun onClick(widget: View) {
                startActivity(Intent(this@LoginActivity, RegisterActivity::class.java))
            }
        }, startIndex, fullText.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
<<<<<<< HEAD
        binding.tvCreateAccountLink.text = spannable
        binding.tvCreateAccountLink.movementMethod = LinkMovementMethod.getInstance()
    }
}
=======

        binding.tvCreateAccountLink.text = spannable
        binding.tvCreateAccountLink.movementMethod = LinkMovementMethod.getInstance()
    }
}
>>>>>>> d231b4d53e6f750ae3c0daf4f61493ce26eaf809
