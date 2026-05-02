package com.example.nexora1.ui.finance

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.nexora1.R
import com.example.nexora1.data.remote.response.FinanceData
import com.example.nexora1.databinding.ItemFinanceRecapHeaderBinding
import com.example.nexora1.databinding.ItemFinanceRecapItemBinding
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class FinanceRecapAdapter : ListAdapter<FinanceRecapItem, RecyclerView.ViewHolder>(DiffCallback) {

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is FinanceRecapItem.Header -> VIEW_TYPE_HEADER
            is FinanceRecapItem.Transaction -> VIEW_TYPE_ITEM
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_HEADER) {
            HeaderViewHolder(ItemFinanceRecapHeaderBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        } else {
            ItemViewHolder(ItemFinanceRecapItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        if (holder is HeaderViewHolder && item is FinanceRecapItem.Header) {
            holder.bind(item)
        } else if (holder is ItemViewHolder && item is FinanceRecapItem.Transaction) {
            holder.bind(item.data)
        }
    }

    inner class HeaderViewHolder(private val binding: ItemFinanceRecapHeaderBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(header: FinanceRecapItem.Header) {
            binding.tvMonthYear.text = header.monthYear
            
            val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
            format.maximumFractionDigits = 0
            
            binding.tvTotalIn.text = "Saldo masuk: " + format.format(header.totalIn).replace("Rp", "Rp ")
            binding.tvTotalOut.text = "Saldo keluar: " + format.format(header.totalOut).replace("Rp", "Rp ")
        }
    }

    inner class ItemViewHolder(private val binding: ItemFinanceRecapItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(finance: FinanceData) {
            val context = itemView.context
            binding.tvCategory.text = finance.category
            binding.tvNote.text = finance.note
            binding.tvTime.text = formatDateTime(finance.createdAt)

            val isPemasukan = finance.type.lowercase().contains("pemasukan") || 
                             finance.type.lowercase().contains("pemasukkan") ||
                             finance.category.lowercase().contains("pemasukan") ||
                             finance.category.lowercase().contains("pemasukkan")

            val color = if (isPemasukan) {
                ContextCompat.getColor(context, R.color.green)
            } else {
                ContextCompat.getColor(context, R.color.red)
            }

            val amount = finance.amount.replace(Regex("[^0-9]"), "").toLongOrNull() ?: 0L
            val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
            format.maximumFractionDigits = 0
            val formattedAmount = format.format(amount).replace("Rp", "Rp ")
            
            binding.tvAmount.text = if (isPemasukan) "+$formattedAmount" else "-$formattedAmount"
            binding.tvAmount.setTextColor(color)
            
            binding.ivIcon.setImageResource(if (isPemasukan) R.drawable.ic_income else R.drawable.ic_expense)
            binding.ivIcon.setColorFilter(color)
            
            val transparentColor = Color.argb(30, Color.red(color), Color.green(color), Color.blue(color))
            binding.ivIcon.setBackgroundTintList(ColorStateList.valueOf(transparentColor))
        }

        private fun formatDateTime(dateTime: String): String {
            val formats = arrayOf("yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", "yyyy-MM-dd'T'HH:mm:ss")
            var parsedDate: java.util.Date? = null
            for (format in formats) {
                try {
                    val sdf = SimpleDateFormat(format, Locale.getDefault())
                    if (format.contains("Z") || format.contains("T")) sdf.timeZone = TimeZone.getTimeZone("UTC")
                    parsedDate = sdf.parse(dateTime)
                    if (parsedDate != null) break
                } catch (e: Exception) { continue }
            }
            val outputFormat = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale("id", "ID"))
            return if (parsedDate != null) outputFormat.format(parsedDate) else dateTime
        }
    }

    companion object {
        private const val VIEW_TYPE_HEADER = 0
        private const val VIEW_TYPE_ITEM = 1

        val DiffCallback = object : DiffUtil.ItemCallback<FinanceRecapItem>() {
            override fun areItemsTheSame(oldItem: FinanceRecapItem, newItem: FinanceRecapItem): Boolean {
                return if (oldItem is FinanceRecapItem.Header && newItem is FinanceRecapItem.Header) {
                    oldItem.monthYear == newItem.monthYear
                } else if (oldItem is FinanceRecapItem.Transaction && newItem is FinanceRecapItem.Transaction) {
                    oldItem.data.id == newItem.data.id
                } else false
            }

            override fun areContentsTheSame(oldItem: FinanceRecapItem, newItem: FinanceRecapItem): Boolean {
                return oldItem == newItem
            }
        }
    }
}

sealed class FinanceRecapItem {
    data class Header(val monthYear: String, val totalIn: Long, val totalOut: Long) : FinanceRecapItem()
    data class Transaction(val data: FinanceData) : FinanceRecapItem()
}
