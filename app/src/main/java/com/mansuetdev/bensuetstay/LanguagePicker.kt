package com.mansuetdev.bensuetstay

import android.app.Activity
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import java.util.Locale

/**
 * In-app language switcher for the 11 written official languages of South Africa.
 * Uses AppCompat's per-app language API: the choice is remembered across restarts and
 * open screens are recreated in the new language automatically.
 */
object LanguagePicker {

    /** Language tag -> the language's name in that language. Order matches res/xml/locales_config.xml. */
    val LANGUAGES = listOf(
        "en" to "English",
        "af" to "Afrikaans",
        "zu" to "isiZulu",
        "xh" to "isiXhosa",
        "nso" to "Sepedi",
        "tn" to "Setswana",
        "st" to "Sesotho",
        "ts" to "Xitsonga",
        "ss" to "siSwati",
        "ve" to "Tshiven\u1e13a",
        "nr" to "isiNdebele"
    )

    /** The tag in use: the in-app choice, else the phone's language if we support it, else English. */
    fun currentTag(): String {
        val chosen = AppCompatDelegate.getApplicationLocales()
        val language = if (chosen.isEmpty) Locale.getDefault().language else chosen[0]?.language
        return LANGUAGES.firstOrNull { it.first == language }?.first ?: "en"
    }

    fun currentName(): String = LANGUAGES.first { it.first == currentTag() }.second

    fun show(activity: Activity) {
        val current = LANGUAGES.indexOfFirst { it.first == currentTag() }
        AlertDialog.Builder(activity)
            .setTitle(R.string.choose_language)
            .setSingleChoiceItems(LANGUAGES.map { it.second }.toTypedArray(), current) { dialog, which ->
                dialog.dismiss()
                if (which != current) {
                    AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(LANGUAGES[which].first))
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }
}
