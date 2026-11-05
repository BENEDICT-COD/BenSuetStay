package com.mansuetdev.bensuetstay

import android.app.Activity
import android.view.View
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.mansuetdev.bensuetstay.databinding.DialogChangePasswordBinding

/**
 * Change password through Firebase Auth. Firebase requires a recent sign-in to change a
 * password, so the user re-enters their current one and is re-authenticated first.
 */
object PasswordDialog {

    /**
     * @param forced first login with an admin-issued temporary password: the dialog can't
     *               be dismissed, and its secondary button logs out instead.
     */
    fun show(activity: Activity, forced: Boolean, onChanged: () -> Unit) {
        val b = DialogChangePasswordBinding.inflate(activity.layoutInflater)
        if (forced) {
            b.tvPasswordIntro.visibility = View.VISIBLE
            b.tvPasswordIntro.text = activity.getString(R.string.you_re_using_a_temporary_password)
            b.tvCurrentPasswordLabel.text = activity.getString(R.string.temporary_password)
            b.etCurrentPassword.hint = activity.getString(R.string.the_password_the_admin_gave_you)
        }

        activity.showFormDialog(
            title = if (forced) activity.getString(R.string.set_your_password) else activity.getString(R.string.change_password),
            view = b.root,
            positive = activity.getString(R.string.update),
            negative = if (forced) activity.getString(R.string.log_out_2) else activity.getString(R.string.cancel),
            cancelable = !forced,
            onNegative = if (forced) ({ Navigator.signOut(activity) }) else null
        ) { dialog ->
            val current = b.etCurrentPassword.text.toString()
            val newPassword = b.etNewPassword.text.toString().trim()
            val confirm = b.etConfirmPassword.text.toString().trim()
            when {
                current.isEmpty() -> b.etCurrentPassword.error = activity.getString(R.string.enter_your_current_password)
                newPassword.length < Validators.MIN_PASSWORD_LENGTH ->
                    b.etNewPassword.error = activity.getString(R.string.use_at_least_characters, Validators.MIN_PASSWORD_LENGTH)
                newPassword != confirm -> b.etConfirmPassword.error = activity.getString(R.string.passwords_don_t_match)
                newPassword == current.trim() -> b.etNewPassword.error = activity.getString(R.string.choose_a_different_password)
                else -> {
                    val user = FirebaseAuth.getInstance().currentUser
                    val email = user?.email
                    if (user == null || email == null) {
                        dialog.dismiss()
                        Navigator.signOut(activity)
                        return@showFormDialog
                    }
                    b.progressPassword.visibility = View.VISIBLE
                    dialog.getButton(android.content.DialogInterface.BUTTON_POSITIVE).isEnabled = false
                    user.reauthenticate(EmailAuthProvider.getCredential(email, current.trim()))
                        .continueWithTask { auth ->
                            if (!auth.isSuccessful) throw auth.exception ?: IllegalStateException("Re-authentication failed")
                            user.updatePassword(newPassword)
                        }
                        .addOnSuccessListener {
                            dialog.dismiss()
                            activity.toast(activity.getString(R.string.password_updated))
                            onChanged()
                        }
                        .addOnFailureListener { e ->
                            b.progressPassword.visibility = View.GONE
                            dialog.getButton(android.content.DialogInterface.BUTTON_POSITIVE).isEnabled = true
                            activity.toast(AuthErrors.message(activity, e), long = true)
                        }
                }
            }
        }
    }
}
