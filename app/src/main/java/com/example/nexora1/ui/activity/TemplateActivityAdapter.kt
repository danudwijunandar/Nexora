package com.example.nexora1.ui.activity

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.nexora1.databinding.ItemTemplateActivityBinding

data class ActivityTemplate(
    val title: String,
    val iconRes: Int,
    val category: String
)

class TemplateActivityAdapter(
    private val templates: List<ActivityTemplate>,
    private val onItemClick: (ActivityTemplate) -> Unit
) : RecyclerView.Adapter<TemplateActivityAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: ItemTemplateActivityBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(template: ActivityTemplate) {
            binding.tvTitle.text = template.title
            binding.ivIcon.setImageResource(template.iconRes)
            itemView.setOnClickListener { onItemClick(template) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTemplateActivityBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(templates[position])
    }

    override fun getItemCount(): Int = templates.size
}