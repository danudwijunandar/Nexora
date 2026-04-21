package com.example.nexora1.ui.finance

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.nexora1.R
import com.example.nexora1.data.remote.response.FinanceData
import com.example.nexora1.databinding.ItemFinanceBinding
import java.text.SimpleDateFormat
import java.util.Locale

class FinanceAdapter(private val onItemClick: (FinanceData) -> Unit) : ListAdapter<FinanceData, FinanceAdapter.FinanceViewHolder>(DiffCallback) {

    inner class FinanceViewHolder(private val binding: ItemFinanceBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(finance: FinanceData) {
            binding.tvNote.text = finance.note
            binding.tvCategory.text = finance.category
            
            // Format date if needed, assuming finance.date is "yyyy-MM-dd" or similar
            // For now displaying as is or trying to parse
            try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val outputFormat = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))
                val date = inputFormat.parse(finance.date)
                binding.tvDate.text = if (date != null) outputFormat.format(date) else finance.date
            } catch (e: Exception) {
                binding.tvDate.text = finance.date
            }

            binding.tvAmount.text = "Rp ${finance.amount}"
            
            val isPemasukan = finance.type.lowercase() == "pemasukan"
            
            val color = if (isPemasukan) {
                ContextCompat.getColor(itemView.context, R.color.green)
            } else {
                ContextCompat.getColor(itemView.context, R.color.red)
            }
            
            binding.tvAmount.setTextColor(color)
            
            val iconRes = if (isPemasukan) {
                R.drawable.ic_income
            } else {
                R.drawable.ic_expense
            }
            binding.ivFinanceType.setImageResource(iconRes)
            binding.ivFinanceType.setColorFilter(color)

            itemView.setOnClickListener { onItemClick(finance) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FinanceViewHolder {
        val binding = ItemFinanceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FinanceViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FinanceViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object DiffCallback : DiffUtil.ItemCallback<FinanceData>() {
        override fun areItemsTheSame(oldItem: FinanceData, newItem: FinanceData): Boolean {
            return oldItem.id == newItem.id
        }
        override fun areContentsTheSame(oldItem: FinanceData, newItem: FinanceData): Boolean {
            return oldItem == newItem
        }
    }
}