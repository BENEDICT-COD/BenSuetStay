package com.mansuetdev.bensuetstay

import android.app.Activity
import android.text.InputType
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

/**
 * The working Settings sections shared by the student and landlord/admin Settings screens:
 * sign-in & security, notifications, home screen defaults, offline & data, help, privacy.
 * Rebuilt in onResume so values (sync count, notification permission) are always current.
 */
object SettingsSections {

    fun buildForStudent(activity: Activity, container: LinearLayout) {
        val b = SettingsBuilder(activity, container)
        securitySection(activity, b)
        notificationsSection(activity, b)

        b.section(R.string.section_home)
        b.switchRow(R.drawable.ic_place, activity.getString(R.string.home_near_campus_title),
            activity.getString(R.string.home_near_campus_subtitle), AppSettings.homeNearCampus(activity)) {
            AppSettings.setHomeNearCampus(activity, it)
        }
        b.switchRow(R.drawable.ic_home, activity.getString(R.string.home_own_funding_title),
            activity.getString(R.string.home_own_funding_subtitle), AppSettings.homeOwnFunding(activity)) {
            AppSettings.setHomeOwnFunding(activity, it)
        }

        b.section(R.string.section_offline)
        val uid = Session.uid.orEmpty()
        val queued = if (uid.isEmpty()) 0 else SyncManager.queuedCount(activity, uid)
        b.row(R.drawable.ic_upload, activity.getString(R.string.waiting_to_sync),
            if (queued > 0) activity.getString(R.string.sync_pending_count, queued) else activity.getString(R.string.sync_all_sent)) {
            when {
                queued == 0 -> activity.toast(activity.getString(R.string.sync_all_sent))
                !SyncManager.isOnline(activity) -> activity.toast(activity.getString(R.string.you_are_offline), long = true)
                else -> { SyncManager.syncNow(activity); activity.toast(activity.getString(R.string.sending)) }
            }
        }
        b.row(R.drawable.ic_delete, activity.getString(R.string.clear_location_title),
            activity.getString(R.string.clear_location_subtitle)) {
            GeocodingApi.clearCache(activity)
            activity.toast(activity.getString(R.string.location_cache_cleared))
        }

        helpSection(activity, b, InfoActivity.PAGE_FAQ_STUDENT)

        b.section(R.string.section_privacy)
        b.row(R.drawable.ic_security, activity.getString(R.string.privacy_policy)) { activity.openInfo(InfoActivity.PAGE_PRIVACY) }
        b.row(R.drawable.ic_delete, activity.getString(R.string.delete_account),
            activity.getString(R.string.delete_account_subtitle), danger = true) { confirmDeleteAccount(activity) }
    }

    fun buildForStaff(activity: Activity, container: LinearLayout) {
        val b = SettingsBuilder(activity, container)
        notificationsSection(activity, b)
        helpSection(activity, b, InfoActivity.PAGE_FAQ_LANDLORD, includeFaq = false)
        b.section(R.string.section_privacy)
        b.row(R.drawable.ic_security, activity.getString(R.string.privacy_policy)) { activity.openInfo(InfoActivity.PAGE_PRIVACY) }
    }

    // ---------- shared sections ----------

    private fun securitySection(activity: Activity, b: SettingsBuilder) {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        val usesPassword = user.providerData.any { it.providerId == EmailAuthProvider.PROVIDER_ID }
        b.section(R.string.section_security)
        b.row(R.drawable.ic_lock, activity.getString(R.string.change_password),
            activity.getString(if (usesPassword) R.string.signed_in_with_email else R.string.signed_in_with_google)) {
            if (usesPassword) {
                PasswordDialog.show(activity, forced = false) {}
            } else {
                AlertDialog.Builder(activity)
                    .setTitle(R.string.change_password)
                    .setMessage(R.string.password_managed_by_google)
                    .setPositiveButton(android.R.string.ok, null)
                    .show()
            }
        }
    }

    private fun notificationsSection(activity: Activity, b: SettingsBuilder) {
        b.section(R.string.section_notifications)
        b.switchRow(R.drawable.ic_notification, activity.getString(R.string.alerts_title),
            activity.getString(R.string.alerts_subtitle), AppSettings.alertsEnabled(activity)) { on ->
            AppSettings.setAlertsEnabled(activity, on)
            if (on) NotificationCenter.start(activity) else NotificationCenter.stop()
        }
        b.switchRow(R.drawable.ic_notification, activity.getString(R.string.announcements_title),
            activity.getString(R.string.announcements_subtitle), AppSettings.announcementsEnabled(activity)) { on ->
            AppSettings.setAnnouncementsEnabled(activity, on)
            Session.role?.let { NotificationCenter.subscribeTopics(activity, it) }
        }
        val allowed = Notifier.canNotify(activity)
        b.row(R.drawable.ic_settings, activity.getString(R.string.system_notifications_title),
            activity.getString(if (allowed) R.string.notifications_allowed else R.string.notifications_blocked)) {
            activity.openNotificationSettings()
        }
    }

    private fun helpSection(activity: Activity, b: SettingsBuilder, faqPage: String, includeFaq: Boolean = true) {
        b.section(R.string.section_help)
        if (includeFaq) b.row(R.drawable.ic_help_outline, activity.getString(R.string.help_faq)) { activity.openInfo(faqPage) }
        b.row(R.drawable.ic_description, activity.getString(R.string.contact_support),
            activity.getString(R.string.contact_support_subtitle)) {
            if (!activity.emailSupport(activity.getString(R.string.support_email_subject))) {
                activity.toast(activity.getString(R.string.no_email_app))
            }
        }
        b.row(R.drawable.ic_apartment, activity.getString(R.string.about_app)) { activity.openInfo(InfoActivity.PAGE_ABOUT) }
    }

    // ---------- account deletion (POPIA right to erasure; also a Google Play requirement) ----------

    private fun confirmDeleteAccount(activity: Activity) {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        val usesPassword = user.providerData.any { it.providerId == EmailAuthProvider.PROVIDER_ID }
        if (usesPassword) {
            val input = EditText(activity)
            input.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            input.hint = activity.getString(R.string.enter_your_password)
            val pad = (20 * activity.resources.displayMetrics.density).toInt()
            val wrapper = LinearLayout(activity).apply { setPadding(pad, pad / 2, pad, 0); addView(input) }
            AlertDialog.Builder(activity)
                .setTitle(R.string.delete_account_confirm_title)
                .setMessage(R.string.delete_account_confirm_body)
                .setView(wrapper)
                .setPositiveButton(R.string.delete) { _, _ ->
                    val password = input.text.toString().trim()
                    val email = user.email
                    if (password.isEmpty() || email == null) {
                        activity.toast(activity.getString(R.string.enter_your_password)); return@setPositiveButton
                    }
                    // Firebase only deletes accounts that signed in recently, so confirm the password first
                    user.reauthenticate(EmailAuthProvider.getCredential(email, password))
                        .addOnSuccessListener { deleteAccount(activity) }
                        .addOnFailureListener { e -> activity.toast(AuthErrors.message(activity, e), long = true) }
                }
                .setNegativeButton(R.string.cancel, null)
                .show()
        } else {
            activity.confirm(
                activity.getString(R.string.delete_account_confirm_title),
                activity.getString(R.string.delete_account_confirm_body),
                activity.getString(R.string.delete)
            ) {
                GoogleSignIn.requestIdToken(activity, onError = { msg -> if (msg != null) activity.toast(msg, long = true) }) { token ->
                    user.reauthenticate(GoogleAuthProvider.getCredential(token, null))
                        .addOnSuccessListener { deleteAccount(activity) }
                        .addOnFailureListener { e -> activity.toast(AuthErrors.message(activity, e), long = true) }
                }
            }
        }
    }

    private fun deleteAccount(activity: Activity) {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        val uid = user.uid
        StudentRepository.deleteProfile(uid)
            .continueWithTask { t ->
                if (!t.isSuccessful) throw t.exception ?: IllegalStateException("Couldn't delete profile")
                user.delete()
            }
            .addOnSuccessListener {
                OfflineStore.get(activity).clear(uid)
                activity.toast(activity.getString(R.string.account_deleted), long = true)
                Navigator.signOut(activity)
            }
            .addOnFailureListener { e -> activity.toast(AuthErrors.message(activity, e), long = true) }
    }
}
