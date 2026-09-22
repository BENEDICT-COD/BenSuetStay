package com.mansuetdev.bensuetstay

import android.content.Intent
import android.os.Bundle
import android.view.animation.DecelerateInterpolator
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
        binding.logoGroup.animate()
            .alpha(1f)
            .setStartDelay(400)
            .setDuration(600)
            .setInterpolator(DecelerateInterpolator())
            .start()

        // Frame 2 -> 3: button group rises and fades in
        binding.buttonGroup.animate()
            .alpha(1f)
            .translationY(0f)
            .setStartDelay(1200)
            .setDuration(500)
            .setInterpolator(DecelerateInterpolator())
            .start()
    }

    private fun setupClickListeners() {
        binding.btnGetStarted.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        binding.btnLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        binding.tvBrowse.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
        }
    }
}