package com.mansuetdev.bensuetstay

import android.app.Activity
import android.view.View
import android.widget.LinearLayout
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import com.mansuetdev.bensuetstay.databinding.ItemSettingsRowBinding
import com.mansuetdev.bensuetstay.databinding.ItemSettingsSectionBinding

/** Builds titled cards of settings rows (same look as the hand-written rows) into a container. */
class SettingsBuilder(private val activity: Activity, private val container: LinearLayout) {

    private var rows: LinearLayout? = null

    init {
        container.removeAllViews()
    }

    fun section(@StringRes title: Int): SettingsBuilder {
        val section = ItemSettingsSectionBinding.inflate(activity.layoutInflater, container, true)
        section.tvSectionTitle.text = activity.getString(title).uppercase()
        rows = section.rowsContainer
        return this
    }

    fun row(
        @DrawableRes icon: Int,
        title: String,
        subtitle: String? = null,
        danger: Boolean = false,
        onClick: () -> Unit
    ): ItemSettingsRowBinding {
        val row = addRow(icon, title, subtitle)
        if (danger) {
            val red = ContextCompat.getColor(activity, R.color.accent_red)
            row.tvRowTitle.setTextColor(red)
            row.ivRowIcon.setColorFilter(red)
        }
        row.rowRoot.setOnClickListener { onClick() }
        return row
    }

    fun switchRow(
        @DrawableRes icon: Int,
        title: String,
        subtitle: String?,
        checked: Boolean,
        onChange: (Boolean) -> Unit
    ): ItemSettingsRowBinding {
        val row = addRow(icon, title, subtitle)
        row.ivRowChevron.visibility = View.GONE
        row.switchRow.visibility = View.VISIBLE
        row.switchRow.isChecked = checked
        row.rowRoot.setOnClickListener {
            row.switchRow.isChecked = !row.switchRow.isChecked
            onChange(row.switchRow.isChecked)
        }
        return row
    }

    private fun addRow(@DrawableRes icon: Int, title: String, subtitle: String?): ItemSettingsRowBinding {
        val target = rows ?: throw IllegalStateException("Call section() first")
        if (target.childCount > 0) {
            val divider = View(activity)
            divider.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1).apply {
                marginStart = (56 * activity.resources.displayMetrics.density).toInt()
            }
            divider.setBackgroundColor(ContextCompat.getColor(activity, R.color.bg_card_outline))
            target.addView(divider)
        }
        val row = ItemSettingsRowBinding.inflate(activity.layoutInflater, target, true)
        row.ivRowIcon.setImageResource(icon)
        row.tvRowTitle.text = title
        if (!subtitle.isNullOrBlank()) {
            row.tvRowSubtitle.text = subtitle
            row.tvRowSubtitle.visibility = View.VISIBLE
        }
        return row
    }
}
