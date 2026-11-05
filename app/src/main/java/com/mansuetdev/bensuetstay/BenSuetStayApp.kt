package com.mansuetdev.bensuetstay

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate

class BenSuetStayApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // The colour palette is light-only for now; stop system dark mode producing a half-dark UI.
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        // Listings are public, so start syncing them straight away (guests browse them too).
        ListingRepository.start()
        Notifier.createChannel(this)
        // Offline mode: send queued bookings/applications whenever the internet comes back
        SyncManager.start(this)
    }
}
