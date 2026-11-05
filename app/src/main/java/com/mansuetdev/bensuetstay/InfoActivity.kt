package com.mansuetdev.bensuetstay

import android.graphics.Typeface
import android.os.Bundle
import android.util.TypedValue
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.mansuetdev.bensuetstay.databinding.ActivityInfoBinding

/** Help & FAQ (student or landlord version), Privacy policy and About. All text is translated. */
class InfoActivity : BaseActivity() {

    companion object {
        const val EXTRA_PAGE = "page"
        const val PAGE_FAQ_STUDENT = "faq_student"
        const val PAGE_FAQ_LANDLORD = "faq_landlord"
        const val PAGE_PRIVACY = "privacy"
        const val PAGE_ABOUT = "about"
    }

    private lateinit var binding: ActivityInfoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.btnBack.setOnClickListener { finish() }

        when (intent.getStringExtra(EXTRA_PAGE)) {
            PAGE_FAQ_STUDENT -> faq(
                R.string.faq_s_q1 to R.string.faq_s_a1,
                R.string.faq_s_q2 to R.string.faq_s_a2,
                R.string.faq_s_q3 to R.string.faq_s_a3,
                R.string.faq_s_q4 to R.string.faq_s_a4
            )
            PAGE_FAQ_LANDLORD -> faq(
                R.string.faq_l_q1 to R.string.faq_l_a1,
                R.string.faq_l_q2 to R.string.faq_l_a2,
                R.string.faq_l_q3 to R.string.faq_l_a3
            )
            PAGE_PRIVACY -> {
                binding.tvTitle.setText(R.string.privacy_policy)
                paragraph(getString(R.string.privacy_policy_body))
            }
            else -> {
                binding.tvTitle.setText(R.string.about_app)
                heading(getString(R.string.app_name))
                paragraph(getString(R.string.version_1_0_0))
                paragraph(getString(R.string.about_body))
                paragraph(getString(R.string.osm_attribution))
            }
        }
    }

    private fun faq(vararg items: Pair<Int, Int>) {
        binding.tvTitle.setText(R.string.help_faq)
        items.forEach { (q, a) ->
            heading(getString(q))
            paragraph(getString(a))
        }
    }

    private fun heading(text: String) = addText(text, 16f, bold = true, topMarginDp = 16)

    private fun paragraph(text: String) = addText(text, 14f, bold = false, topMarginDp = 6)

    private fun addText(text: String, sizeSp: Float, bold: Boolean, topMarginDp: Int) {
        val view = TextView(this)
        view.text = text
        view.setTextSize(TypedValue.COMPLEX_UNIT_SP, sizeSp)
        view.setTextColor(ContextCompat.getColor(this, if (bold) R.color.text_primary else R.color.text_muted))
        view.setLineSpacing(0f, 1.25f)
        if (bold) view.setTypeface(view.typeface, Typeface.BOLD)
        view.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply { topMargin = (topMarginDp * resources.displayMetrics.density).toInt() }
        binding.contentContainer.addView(view)
    }
}
