package com.mansuetdev.bensuetstay

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.mansuetdev.bensuetstay.databinding.ItemApplicationBinding

<<<<<<< HEAD
class ApplicationAdapter(
    private var applications: List<StudentApplication>,
    private val forStudent: Boolean = false
) : RecyclerView.Adapter<ApplicationAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemApplicationBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(ItemApplicationBinding.inflate(LayoutInflater.from(parent.context), parent, false))
=======
class ApplicationAdapter(private var applications: List<StudentApplication>) :
    RecyclerView.Adapter<ApplicationAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemApplicationBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemApplicationBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }
>>>>>>> d231b4d53e6f750ae3c0daf4f61493ce26eaf809

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val app = applications[position]
        val context = holder.itemView.context

        holder.binding.apply {
<<<<<<< HEAD
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
=======
            tvStudentName.text = app.studentName
            tvAccommodation.text = app.accommodationName
            tvDateApplied.text = "Applied ${app.dateApplied}"

            if (app.applicationType == ApplicationType.NSFAS_CONFIRMATION) {
                tvFundingBadge.text = "NSFAS"
                tvFundingBadge.setBackgroundResource(R.drawable.bg_badge_nsfas)
                tvFundingBadge.setTextColor(ContextCompat.getColor(context, R.color.brand_blue))
            } else {
                tvFundingBadge.text = "Self Funded"
                tvFundingBadge.setBackgroundResource(R.drawable.bg_badge_selffunded)
                tvFundingBadge.setTextColor(ContextCompat.getColor(context, R.color.accent_green))
            }

            when (app.status) {
                ApplicationStatus.SUBMITTED -> {
                    tvStatus.text = "Submitted"
                    tvStatus.setBackgroundResource(R.drawable.bg_badge_nsfas)
                    tvStatus.setTextColor(ContextCompat.getColor(context, R.color.accent_orange))
                }
                ApplicationStatus.VIEWED -> {
                    tvStatus.text = "Viewed"
                    tvStatus.setBackgroundResource(R.drawable.bg_badge_nsfas)
                    tvStatus.setTextColor(ContextCompat.getColor(context, R.color.brand_blue))
                }
                ApplicationStatus.APPROVED -> {
                    tvStatus.text = "Approved"
                    tvStatus.setBackgroundResource(R.drawable.bg_badge_selffunded)
                    tvStatus.setTextColor(ContextCompat.getColor(context, R.color.accent_green))
                }
                ApplicationStatus.DECLINED -> {
                    tvStatus.text = "Declined"
                    tvStatus.setBackgroundResource(R.drawable.bg_badge_declined)
                    tvStatus.setTextColor(ContextCompat.getColor(context, R.color.accent_red))
                }
            }

            root.setOnClickListener {
                val intent = Intent(context, ApplicationDetailActivity::class.java)
                intent.putExtra("application_id", app.id)
                context.startActivity(intent)
>>>>>>> d231b4d53e6f750ae3c0daf4f61493ce26eaf809
            }
        }
    }

    override fun getItemCount() = applications.size

    fun updateList(newList: List<StudentApplication>) {
<<<<<<< HEAD
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
=======
        applications = newList
        notifyDataSetChanged()
    }
}
>>>>>>> d231b4d53e6f750ae3c0daf4f61493ce26eaf809
