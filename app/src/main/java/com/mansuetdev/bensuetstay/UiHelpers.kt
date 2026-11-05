package com.mansuetdev.bensuetstay

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AlertDialog
import coil.Coil
import coil.request.ImageRequest
import coil.transform.CircleCropTransformation

fun Context.toast(message: String, long: Boolean = false) {
    Toast.makeText(this, message, if (long) Toast.LENGTH_LONG else Toast.LENGTH_SHORT).show()
}

/** Opens a link/file in whatever app can handle it. Returns false when nothing can. */
fun Context.openUrl(url: String): Boolean = try {
    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    true
} catch (e: ActivityNotFoundException) {
    false
}

fun Context.dial(phone: String): Boolean = try {
    startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone")))
    true
} catch (e: ActivityNotFoundException) {
    false
}

/** Opens the email app addressed to BenSuet Stay support. Returns false when there's no email app. */
fun Context.emailSupport(subject: String): Boolean = try {
    startActivity(
        Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:"))
            .putExtra(Intent.EXTRA_EMAIL, arrayOf(getString(R.string.support_email)))
            .putExtra(Intent.EXTRA_SUBJECT, subject)
    )
    true
} catch (e: ActivityNotFoundException) {
    false
}

/** This app's page in the phone's notification settings. */
fun Context.openNotificationSettings() {
    try {
        startActivity(
            Intent(android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                .putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, packageName)
        )
    } catch (e: ActivityNotFoundException) {
        // Very old or unusual launcher: nothing to open
    }
}

fun Context.openInfo(page: String) {
    startActivity(Intent(this, InfoActivity::class.java).putExtra(InfoActivity.EXTRA_PAGE, page))
}

/** The file name the user sees for a picked document, e.g. "nsfas_letter.pdf". */
fun Context.displayName(uri: Uri): String {
    try {
        contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { c ->
            if (c.moveToFirst()) {
                val name = c.getString(0)
                if (!name.isNullOrBlank()) return name
            }
        }
    } catch (e: Exception) {
        // fall through to the URI's last segment
    }
    return uri.lastPathSegment ?: "document"
}

object Images {
    /** Loads a remote image with Coil, falling back to [placeholder] when there is no URL or it fails. */
    fun load(view: ImageView, url: String?, @DrawableRes placeholder: Int, circle: Boolean = false) {
        if (url.isNullOrBlank()) {
            view.setImageResource(placeholder)
            return
        }
        val request = ImageRequest.Builder(view.context)
            .data(url)
            .target(view)
            .crossfade(true)
            .placeholder(placeholder)
            .error(placeholder)
        if (circle) request.transformations(CircleCropTransformation())
        Coil.imageLoader(view.context).enqueue(request.build())
    }
}

/**
 * An AlertDialog whose positive button does NOT close it automatically, so validation
 * errors can be shown without the user losing what they typed. Call dialog.dismiss() in
 * [onSubmit] once the input is valid (or the async work has finished).
 */
fun Activity.showFormDialog(
    title: String,
    view: View,
    positive: String,
    negative: String = getString(R.string.cancel),
    cancelable: Boolean = true,
    onNegative: (() -> Unit)? = null,
    onSubmit: (AlertDialog) -> Unit
): AlertDialog {
    val dialog = AlertDialog.Builder(this)
        .setTitle(title)
        .setView(view)
        .setCancelable(cancelable)
        .setPositiveButton(positive, null)
        .setNegativeButton(negative) { _, _ -> onNegative?.invoke() }
        .create()
    dialog.setOnShowListener {
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener { onSubmit(dialog) }
    }
    dialog.show()
    return dialog
}

fun Activity.confirm(title: String, message: String, positive: String, onConfirm: () -> Unit) {
    AlertDialog.Builder(this)
        .setTitle(title)
        .setMessage(message)
        .setPositiveButton(positive) { _, _ -> onConfirm() }
        .setNegativeButton(getString(R.string.cancel), null)
        .show()
}
