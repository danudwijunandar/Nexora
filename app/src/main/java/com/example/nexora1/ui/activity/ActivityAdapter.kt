package com.example.nexora1.ui.activity

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.nexora1.R
import com.example.nexora1.data.remote.response.ActivityData
import com.example.nexora1.databinding.ItemListBinding
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class ActivityAdapter(
    private val onItemClick: (ActivityData) -> Unit,
    private val onStatusChange: (ActivityData, Boolean) -> Unit
) : ListAdapter<ActivityData, ActivityAdapter.ActivityViewHolder>(DiffCallback) {

    inner class ActivityViewHolder(private val binding: ItemListBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(activity: ActivityData) {
            val context = itemView.context
            binding.tvTitle.text = activity.title
            binding.tvDescription.text = activity.description

            val tag = activity.categories
            if (tag == "kesehatan"){
                binding.tvTag.text = "Kesehatan"
            } else {
                binding.tvTag.text = "Produktif"
            }


            val isDone = activity.status == "selesai" || activity.status == "3"
            binding.cbStatus.setOnClickListener(null)
            binding.cbStatus.isChecked = isDone

            binding.mainCard.setCardBackgroundColor(ContextCompat.getColor(context, R.color.white))
            binding.tvTitle.setTextColor(ContextCompat.getColor(context, R.color.black))
            binding.tvDescription.setTextColor(ContextCompat.getColor(context, R.color.text_gray))
            binding.tvDate.setTextColor(ContextCompat.getColor(context, R.color.text_gray))

            val category = activity.categories.lowercase()
            if (category.contains("produktif")) {
                val productiveColor = Color.parseColor("#00E2FB")
                val transparentProductive = Color.argb(40, Color.red(productiveColor), Color.green(productiveColor), Color.blue(productiveColor))
                binding.tvTag.backgroundTintList = ColorStateList.valueOf(transparentProductive)
                binding.tvTag.setTextColor(productiveColor)
            } else if (category.contains("kesehatan")) {
                val greenColor = ContextCompat.getColor(context, R.color.green)
                val transparentGreen = Color.argb(40, Color.red(greenColor), Color.green(greenColor), Color.blue(greenColor))
                binding.tvTag.backgroundTintList = ColorStateList.valueOf(transparentGreen)
                binding.tvTag.setTextColor(greenColor)
            } else {
                binding.tvTag.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.bg_color))
                binding.tvTag.setTextColor(ContextCompat.getColor(context, R.color.text_gray))
            }

            val timeSource = activity.date ?: activity.createdAt
            try {
                val formats = arrayOf("yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", "yyyy-MM-dd'T'HH:mm:ss")
                var parsedDate: java.util.Date? = null
                for (format in formats) {
                    try {
                        val sdf = SimpleDateFormat(format, Locale.getDefault())
                        if (format.contains("Z") || format.contains("T")) sdf.timeZone = TimeZone.getTimeZone("UTC")
                        parsedDate = sdf.parse(timeSource)
                        if (parsedDate != null) break
                    } catch (e: Exception) { continue }
                }
                val outputFormat = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale("id", "ID"))
                binding.tvDate.text = if (parsedDate != null) outputFormat.format(parsedDate) else timeSource
            } catch (e: Exception) {
                binding.tvDate.text = timeSource
            }

            binding.cbStatus.setOnClickListener {
                val isChecked = binding.cbStatus.isChecked
                binding.cbStatus.isChecked = !isChecked
                onStatusChange(activity, isChecked)
            }

            itemView.setOnClickListener { onItemClick(activity) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActivityViewHolder {
        val binding = ItemListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ActivityViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ActivityViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object DiffCallback : DiffUtil.ItemCallback<ActivityData>() {
        override fun areItemsTheSame(oldItem: ActivityData, newItem: ActivityData): Boolean {
            return oldItem.id == newItem.id
        }
        override fun areContentsTheSame(oldItem: ActivityData, newItem: ActivityData): Boolean {
            return oldItem == newItem
        }
    }
}