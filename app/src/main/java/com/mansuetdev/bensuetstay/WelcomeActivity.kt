package com.mansuetdev.bensuetstay

import android.content.Intent
import android.os.Bundle
import android.view.animation.DecelerateInterpolator
<<<<<<< HEAD
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.google.firebase.auth.FirebaseAuth
import com.mansuetdev.bensuetstay.databinding.ActivityWelcomeBinding

/**
 * Entry point. If Firebase still has a signed-in user (app reopened, or Android killed the
 * process), the splash stays up while their role is loaded and they go straight to their
 * home screen. Otherwise the intro animation and buttons are shown.
 */
class WelcomeActivity : BaseActivity() {

    private lateinit var binding: ActivityWelcomeBinding
    private var restoringSession = false

    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        super.onCreate(savedInstanceState)
        splash.setKeepOnScreenCondition { restoringSession }
        binding = ActivityWelcomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupClickListeners()

        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            runIntroSequence()
            return
        }

        restoringSession = true
        Session.load(
            user.uid,
            onLoaded = { role ->
                restoringSession = false
                if (role != null) {
                    Navigator.openHome(this, role)
                } else {
                    Session.signOut()
                    runIntroSequence()
                }
            },
            onFailed = { e ->
                restoringSession = false
                runIntroSequence()
                toast(getString(R.string.couldn_t_restore_your_session, AuthErrors.message(this, e)), long = true)
            }
        )
    }

    private fun runIntroSequence() {
=======
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.mansuetdev.bensuetstay.databinding.ActivityWelcomeBinding

class WelcomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWelcomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        binding = ActivityWelcomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        runIntroSequence()
        setupClickListeners()
    }

    private fun runIntroSequence() {
        // Frame 1 -> 2: logo group fades in
>>>>>>> d231b4d53e6f750ae3c0daf4f61493ce26eaf809
        binding.logoGroup.animate()
            .alpha(1f)
            .setStartDelay(400)
            .setDuration(600)
            .setInterpolator(DecelerateInterpolator())
            .start()

<<<<<<< HEAD
=======
        // Frame 2 -> 3: button group rises and fades in
>>>>>>> d231b4d53e6f750ae3c0daf4f61493ce26eaf809
        binding.buttonGroup.animate()
            .alpha(1f)
            .translationY(0f)
            .setStartDelay(1200)
            .setDuration(500)
            .setInterpolator(DecelerateInterpolator())
            .start()
    }

    private fun setupClickListeners() {
<<<<<<< HEAD
        binding.tvLanguage.text = LanguagePicker.currentName()
        binding.tvLanguage.setOnClickListener { LanguagePicker.show(this) }
        binding.btnGetStarted.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
        binding.btnLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
=======
        binding.btnGetStarted.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        binding.btnLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

>>>>>>> d231b4d53e6f750ae3c0daf4f61493ce26eaf809
        binding.tvBrowse.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
        }
    }
<<<<<<< HEAD
}
=======
}
>>>>>>> d231b4d53e6f750ae3c0daf4f61493ce26eaf809
