package com.example.nexora1.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.nexora1.data.local.room.NotificationEntity
import com.example.nexora1.databinding.ItemNotificationBinding
import java.text.SimpleDateFormat
import java.util.*

class NotificationAdapter : ListAdapter<NotificationEntity, NotificationAdapter.NotificationViewHolder>(DiffCallback) {

    inner class NotificationViewHolder(private val binding: ItemNotificationBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(notification: NotificationEntity) {
            binding.tvTitle.text = notification.title
            binding.tvMessage.text = notification.message
            val sdf = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())
            binding.tvTime.text = sdf.format(Date(notification.timestamp))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val binding = ItemNotificationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NotificationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object DiffCallback : DiffUtil.ItemCallback<NotificationEntity>() {
        override fun areItemsTheSame(oldItem: NotificationEntity, newItem: NotificationEntity): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: NotificationEntity, newItem: NotificationEntity): Boolean = oldItem == newItem
    }
}