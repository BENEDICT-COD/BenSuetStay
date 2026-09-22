package com.mansuetdev.bensuetstay

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.mansuetdev.bensuetstay.databinding.ItemApplicationBinding

class ApplicationAdapter(private var applications: List<StudentApplication>) :
    RecyclerView.Adapter<ApplicationAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemApplicationBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemApplicationBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val app = applications[position]
        val context = holder.itemView.context

        holder.binding.apply {
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
            }
        }
    }

    override fun getItemCount() = applications.size

    fun updateList(newList: List<StudentApplication>) {
        applications = newList
        notifyDataSetChanged()
    }
}