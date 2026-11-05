package com.mansuetdev.bensuetstay

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.mansuetdev.bensuetstay.databinding.ItemApplicationBinding

class ApplicationAdapter(
    private var applications: List<StudentApplication>,
    private val forStudent: Boolean = false
) : RecyclerView.Adapter<ApplicationAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemApplicationBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(ItemApplicationBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val app = applications[position]
        val context = holder.itemView.context

        holder.binding.apply {
            tvStudentName.text = if (forStudent) app.accommodationName else app.studentName
            tvAccommodation.text = if (forStudent) Labels.roomType(context, app.roomType)
                else "${app.accommodationName} \u00b7 ${Labels.roomType(context, app.roomType)}"
            tvDateApplied.text = context.getString(R.string.applied, app.dateApplied)

            val isNsfas = app.applicationType == ApplicationType.NSFAS_CONFIRMATION
            tvFundingBadge.text = Labels.funding(context, app.roomFundingType)
            tvFundingBadge.setBackgroundResource(if (isNsfas) R.drawable.bg_badge_nsfas else R.drawable.bg_badge_selffunded)
            tvFundingBadge.setTextColor(ContextCompat.getColor(context, if (isNsfas) R.color.brand_blue else R.color.accent_green))

            val (label, bg, color) = statusStyle(context, app.status)
            tvStatus.text = label
            tvStatus.setBackgroundResource(bg)
            tvStatus.setTextColor(ContextCompat.getColor(context, color))

            if (app.pendingSync) {
                tvStatus.text = context.getString(R.string.waiting_to_sync)
                tvStatus.setTextColor(ContextCompat.getColor(context, R.color.text_muted))
            }

            root.setOnClickListener {
                if (app.pendingSync) {
                    context.toast(context.getString(R.string.pending_sync_tap))
                    return@setOnClickListener
                }
                context.startActivity(
                    Intent(context, ApplicationDetailActivity::class.java)
                        .putExtra(ApplicationDetailActivity.EXTRA_APPLICATION_ID, app.id)
                )
            }
        }
    }

    override fun getItemCount() = applications.size

    fun updateList(newList: List<StudentApplication>) {
        applications = newList.toList()
        notifyDataSetChanged()
    }

    companion object {
        fun statusStyle(context: android.content.Context, status: ApplicationStatus): Triple<String, Int, Int> = when (status) {
            ApplicationStatus.SUBMITTED -> Triple(context.getString(R.string.submitted), R.drawable.bg_badge_nsfas, R.color.accent_orange)
            ApplicationStatus.VIEWED -> Triple(context.getString(R.string.viewed), R.drawable.bg_badge_nsfas, R.color.brand_blue)
            ApplicationStatus.APPROVED -> Triple(context.getString(R.string.approved), R.drawable.bg_badge_selffunded, R.color.accent_green)
            ApplicationStatus.DECLINED -> Triple(context.getString(R.string.declined), R.drawable.bg_badge_declined, R.color.accent_red)
        }
    }
}
