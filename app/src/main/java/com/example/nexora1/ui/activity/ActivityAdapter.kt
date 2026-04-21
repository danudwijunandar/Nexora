package com.example.nexora1.ui.activity

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

class ActivityAdapter(
    private val onItemClick: (ActivityData) -> Unit,
    private val onStatusChange: (ActivityData, Boolean) -> Unit
) : ListAdapter<ActivityData, ActivityAdapter.ActivityViewHolder>(DiffCallback) {

    inner class ActivityViewHolder(private val binding: ItemListBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(activity: ActivityData) {
            binding.tvTitle.text = activity.title
            binding.tvDescription.text = activity.description
            binding.tvTag.text = activity.categories
            
            // Status logic: if status is empty, null or 1 it is unchecked
            val isDone = activity.status == "selesai" || activity.status == "3"
            binding.cbStatus.setOnCheckedChangeListener(null) // Reset listener
            binding.cbStatus.isChecked = isDone
            
            // Format date
            try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val outputFormat = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))
                val date = inputFormat.parse(activity.createdAt)
                binding.tvDate.text = if (date != null) outputFormat.format(date) else activity.createdAt
            } catch (e: Exception) {
                binding.tvDate.text = activity.createdAt
            }
            
            val context = itemView.context
            binding.tvTag.setBackgroundResource(R.drawable.bg_tag_gray)
            binding.tvTag.setTextColor(ContextCompat.getColor(context, R.color.text_gray))

            binding.cbStatus.setOnCheckedChangeListener { _, isChecked ->
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