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
import com.mansuetdev.bensuetstay.databinding.ActivityRegisterBinding

/** Student self-registration. Landlords and admins never register here. */
class RegisterActivity : BaseActivity() {

    companion object {
        /** Signed in with Google but no profile yet: collect campus details only. */
        const val EXTRA_COMPLETE_GOOGLE = "complete_google_profile"
    }

    private var completingGoogle = false
    private var completed = false
=======
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mansuetdev.bensuetstay.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {
>>>>>>> d231b4d53e6f750ae3c0daf4f61493ce26eaf809

    private lateinit var binding: ActivityRegisterBinding
    private var isPasswordVisible = false
    private var isNsfasSelected = true
<<<<<<< HEAD
    private val auth = FirebaseAuth.getInstance()
=======

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
>>>>>>> d231b4d53e6f750ae3c0daf4f61493ce26eaf809

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

<<<<<<< HEAD
        binding.btnBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
        binding.ivTogglePassword.setOnClickListener { togglePasswordVisibility() }
        binding.btnNsfas.setOnClickListener { selectFundingType(nsfas = true) }
        binding.btnSelfFunded.setOnClickListener { selectFundingType(nsfas = false) }
        binding.btnCreateAccount.setOnClickListener { attemptRegistration() }
        binding.btnGoogle.setOnClickListener { signUpWithGoogle() }
        setupLoginLink()

        if (intent.getBooleanExtra(EXTRA_COMPLETE_GOOGLE, false)) {
            FirebaseAuth.getInstance().currentUser?.let { enterGoogleCompletion(it) } ?: finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Left the Google sign-up half-way: don't stay signed in without a profile
        if (completingGoogle && !completed && isFinishing) Session.signOut()
    }

    private fun signUpWithGoogle() {
        binding.btnGoogle.isEnabled = false
        GoogleSignIn.signIn(this,
            onSignedIn = { user ->
                GoogleSignIn.route(this, user,
                    onNewUser = { enterGoogleCompletion(user) },
                    onError = { message -> binding.btnGoogle.isEnabled = true; toast(message, long = true) })
            },
            onError = { message ->
                binding.btnGoogle.isEnabled = true
                if (message != null) toast(message, long = true)
            })
    }

    private fun enterGoogleCompletion(user: com.google.firebase.auth.FirebaseUser) {
        completingGoogle = true
        if (binding.etFullNames.text.isNullOrBlank()) binding.etFullNames.setText(user.displayName.orEmpty())
        binding.etEmailPhone.setText(user.email.orEmpty())
        listOf(binding.tvEmailLabel, binding.etEmailPhone, binding.tvPasswordLabel, binding.passwordContainer,
            binding.btnGoogle, binding.tvLoginLink).forEach { it.visibility = android.view.View.GONE }
        binding.btnCreateAccount.text = getString(R.string.finish_sign_up)
        toast(getString(R.string.google_complete_profile), long = true)
    }

    private fun finishGoogleSignUp(fullNames: String, address: String, institution: String) {
        val user = FirebaseAuth.getInstance().currentUser ?: run { Navigator.restart(this); return }
        val student = Student(
            id = user.uid,
            fullName = fullNames,
            email = user.email.orEmpty(),
            address = address,
            fundingType = if (isNsfasSelected) FundingType.NSFAS else FundingType.SELF_OR_BURSARY,
            institution = institution
        )
        setLoading(true)
        StudentRepository.createProfile(student)
            .addOnSuccessListener {
                completed = true
                Session.setStudent(student)
                toast(getString(R.string.welcome_to_bensuet_stay, fullNames.substringBefore(" ")))
                Navigator.openHome(this, Role.STUDENT)
            }
            .addOnFailureListener { e ->
                setLoading(false)
                toast(getString(R.string.couldn_t_save_your_profile, AuthErrors.message(this, e)), long = true)
            }
    }

=======
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

>>>>>>> d231b4d53e6f750ae3c0daf4f61493ce26eaf809
    private fun attemptRegistration() {
        val fullNames = binding.etFullNames.text.toString().trim()
        val address = binding.etAddress.text.toString().trim()
        val institution = binding.etInstitution.text.toString().trim()
        val email = binding.etEmailPhone.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

<<<<<<< HEAD
        if (completingGoogle) {
            if (fullNames.isEmpty() || address.isEmpty() || institution.isEmpty()) {
                toast(getString(R.string.please_fill_in_all_fields)); return
            }
            finishGoogleSignUp(fullNames, address, institution)
            return
        }
        if (fullNames.isEmpty() || address.isEmpty() || institution.isEmpty() || email.isEmpty() || password.isEmpty()) {
            toast(getString(R.string.please_fill_in_all_fields))
            return
        }
        if (!Validators.isValidEmail(email)) {
            binding.etEmailPhone.error = getString(R.string.enter_a_valid_email_address)
            return
        }
        if (password.length < Validators.MIN_PASSWORD_LENGTH) {
            binding.etPassword.error = getString(R.string.use_at_least_characters, Validators.MIN_PASSWORD_LENGTH)
            return
        }

        setLoading(true)
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val user = result.user
                if (user == null) {
                    setLoading(false)
                    toast(getString(R.string.registration_failed_please_try_again))
                    return@addOnSuccessListener
                }
                val student = Student(
                    id = user.uid,
                    fullName = fullNames,
                    email = email,
                    address = address,
                    fundingType = if (isNsfasSelected) FundingType.NSFAS else FundingType.SELF_OR_BURSARY,
                    institution = institution
                )
                StudentRepository.createProfile(student)
                    .addOnSuccessListener {
                        Session.setStudent(student)
                        toast(getString(R.string.welcome_to_bensuet_stay, fullNames.substringBefore(" ")))
                        Navigator.openHome(this, Role.STUDENT)
                    }
                    .addOnFailureListener { e ->
                        // Roll back so the email can be used again instead of being stuck without a profile
                        user.delete().addOnCompleteListener { Session.signOut() }
                        setLoading(false)
                        toast(getString(R.string.couldn_t_save_your_profile, AuthErrors.message(this, e)), long = true)
                    }
            }
            .addOnFailureListener { e ->
                setLoading(false)
                toast(AuthErrors.message(this, e), long = true)
            }
    }

    private fun setLoading(loading: Boolean) {
        binding.btnCreateAccount.isEnabled = !loading
        binding.btnCreateAccount.text = when {
            loading -> getString(R.string.creating_account)
            completingGoogle -> getString(R.string.finish_sign_up)
            else -> getString(R.string.create_account)
        }
=======
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

    private fun selectFundingType(nsfas: Boolean) {
        isNsfasSelected = nsfas
<<<<<<< HEAD
        val active = if (nsfas) binding.btnNsfas else binding.btnSelfFunded
        val inactive = if (nsfas) binding.btnSelfFunded else binding.btnNsfas
        active.setBackgroundResource(if (nsfas) R.drawable.bg_toggle_left_active else R.drawable.bg_toggle_right_active)
        active.setTextColor(ContextCompat.getColor(this, R.color.surface_white))
        inactive.setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent))
        inactive.setTextColor(ContextCompat.getColor(this, R.color.text_muted))
    }

    private fun setupLoginLink() {
        val prompt = getString(R.string.have_account_prompt)
        val fullText = prompt + " " + getString(R.string.log_in_link)
        val spannable = SpannableString(fullText)
        val startIndex = prompt.length + 1
=======
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
                startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
            }
        }, startIndex, fullText.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
<<<<<<< HEAD
        binding.tvLoginLink.text = spannable
        binding.tvLoginLink.movementMethod = LinkMovementMethod.getInstance()
    }
}
=======

        binding.tvLoginLink.text = spannable
        binding.tvLoginLink.movementMethod = LinkMovementMethod.getInstance()
    }
}
>>>>>>> d231b4d53e6f750ae3c0daf4f61493ce26eaf809
