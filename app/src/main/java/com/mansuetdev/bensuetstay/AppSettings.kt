package com.mansuetdev.bensuetstay

import android.content.Context

/** User choices from the Settings screens, stored on the device. */
object AppSettings {
    private const val PREFS = "app_settings"
    private const val ALERTS = "alerts_enabled"
    private const val ANNOUNCEMENTS = "announcements_enabled"
    private const val HOME_NEAR_CAMPUS = "home_near_campus"
    private const val HOME_OWN_FUNDING = "home_own_funding"

    private fun prefs(c: Context) = c.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    /** Real-time booking/application alerts. */
    fun alertsEnabled(c: Context) = prefs(c).getBoolean(ALERTS, true)
    fun setAlertsEnabled(c: Context, value: Boolean) = prefs(c).edit().putBoolean(ALERTS, value).apply()

    /** Push announcements sent to FCM topics. */
    fun announcementsEnabled(c: Context) = prefs(c).getBoolean(ANNOUNCEMENTS, true)
    fun setAnnouncementsEnabled(c: Context, value: Boolean) = prefs(c).edit().putBoolean(ANNOUNCEMENTS, value).apply()

    /** Homepage starts with "Near my campus" on. */
    fun homeNearCampus(c: Context) = prefs(c).getBoolean(HOME_NEAR_CAMPUS, true)
    fun setHomeNearCampus(c: Context, value: Boolean) = prefs(c).edit().putBoolean(HOME_NEAR_CAMPUS, value).apply()

    /** Homepage starts filtered to the student's own funding type. */
    fun homeOwnFunding(c: Context) = prefs(c).getBoolean(HOME_OWN_FUNDING, true)
    fun setHomeOwnFunding(c: Context, value: Boolean) = prefs(c).edit().putBoolean(HOME_OWN_FUNDING, value).apply()
}
