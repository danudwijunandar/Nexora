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
import com.example.nexora1.databinding.ItemFinanceBinding
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class FinanceAdapter(private val onItemClick: (FinanceData) -> Unit) : ListAdapter<FinanceData, FinanceAdapter.FinanceViewHolder>(DiffCallback) {

    inner class FinanceViewHolder(private val binding: ItemFinanceBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(finance: FinanceData) {
            val context = itemView.context
            binding.tvNote.text = finance.note
            binding.tvCategory.text = finance.category

            val type = finance.type
            if (type == "pemasukan"){
                binding.tvType.text = "Pemasukan"
            } else {
                binding.tvType.text = "Pengeluaran"
            }

            binding.tvDate.text = formatDateTime(finance.createdAt)

            val isPemasukan = checkIfIncome(finance)
            
            val color = if (isPemasukan) {
                ContextCompat.getColor(context, R.color.green)
            } else {
                ContextCompat.getColor(context, R.color.red)
            }

            setTagStyle(color)
            setAmountStyle(finance.amount, isPemasukan, color)
            setIconStyle(isPemasukan, color)

            itemView.setOnClickListener { onItemClick(finance) }
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

        private fun checkIfIncome(finance: FinanceData): Boolean {
            val type = finance.type.lowercase()
            val category = finance.category.lowercase()
            return type.contains("pemasukan") || type.contains("pemasukkan") ||
                   category.contains("pemasukan") || category.contains("pemasukkan")
        }

        private fun setTagStyle(color: Int) {
            val transparentColor = Color.argb(40, Color.red(color), Color.green(color), Color.blue(color))
            binding.tvType.backgroundTintList = ColorStateList.valueOf(transparentColor)
            binding.tvType.setTextColor(color)
        }

        private fun setAmountStyle(amount: String, isIncome: Boolean, color: Int) {
            val prefix = if (isIncome) "+" else "-"
            binding.tvAmount.text = itemView.context.getString(R.string.finance_amount_format, prefix, amount)
            binding.tvAmount.setTextColor(color)
        }

        private fun setIconStyle(isIncome: Boolean, color: Int) {
            val iconRes = if (isIncome) R.drawable.ic_income else R.drawable.ic_expense
            binding.ivFinanceType.setImageResource(iconRes)
            binding.ivFinanceType.setColorFilter(color)
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