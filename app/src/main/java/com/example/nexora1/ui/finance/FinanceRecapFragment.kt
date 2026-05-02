package com.example.nexora1.ui.finance

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nexora1.data.remote.response.FinanceData
import com.example.nexora1.databinding.FragmentFinanceRecapBinding
import com.example.nexora1.ui.ViewModelFactory
import java.text.SimpleDateFormat
import java.util.*

class FinanceRecapFragment : Fragment() {
    private var _binding: FragmentFinanceRecapBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: FinanceViewModel by viewModels {
        ViewModelFactory.getInstance(requireContext())
    }
    
    private lateinit var adapter: FinanceRecapAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFinanceRecapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupToolbar()
        setupRecyclerView()
        observeViewModel()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupRecyclerView() {
        adapter = FinanceRecapAdapter()
        binding.rvRecap.layoutManager = LinearLayoutManager(requireContext())
        binding.rvRecap.adapter = adapter
    }

    private fun observeViewModel() {
        viewModel.getFinance().observe(viewLifecycleOwner) { entities ->
            val dataList = entities.map { entity ->
                FinanceData(
                    id = entity.id ?: 0,
                    userId = entity.userId,
                    type = entity.type,
                    category = entity.category,
                    amount = entity.amount,
                    date = entity.date,
                    note = entity.note,
                    createdAt = entity.createdAt,
                    updatedAt = entity.updatedAt
                )
            }.sortedByDescending { it.date } // Urutkan berdasarkan tanggal pilihan user

            if (dataList.isEmpty()) {
                binding.tvEmpty.visibility = View.VISIBLE
                binding.rvRecap.visibility = View.GONE
            } else {
                binding.tvEmpty.visibility = View.GONE
                binding.rvRecap.visibility = View.VISIBLE
                val recapItems = processFinanceData(dataList)
                adapter.submitList(recapItems)
            }
        }
    }

    private fun processFinanceData(list: List<FinanceData>): List<FinanceRecapItem> {
        val groupedMap = mutableMapOf<String, MutableList<FinanceData>>()
        val sdfSource = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val sdfMonthYear = SimpleDateFormat("MMMM yyyy", Locale("id", "ID"))

        list.forEach { item ->
            try {
                // Gunakan item.date BUKAN item.createdAt
                val dateStr = item.date.split(" ")[0].split("T")[0]
                val date = sdfSource.parse(dateStr)
                val monthYear = if (date != null) sdfMonthYear.format(date) else "Lainnya"
                
                if (!groupedMap.containsKey(monthYear)) {
                    groupedMap[monthYear] = mutableListOf()
                }
                groupedMap[monthYear]?.add(item)
            } catch (e: Exception) {
                val key = "Lainnya"
                if (!groupedMap.containsKey(key)) groupedMap[key] = mutableListOf()
                groupedMap[key]?.add(item)
            }
        }

        val resultList = mutableListOf<FinanceRecapItem>()
        val processedMonthYears = mutableSetOf<String>()
        
        list.forEach { item ->
            val dateStr = item.date.split(" ")[0].split("T")[0]
            val date = try { sdfSource.parse(dateStr) } catch(e: Exception) { null }
            val monthYear = if (date != null) sdfMonthYear.format(date) else "Lainnya"
            
            if (monthYear !in processedMonthYears) {
                processedMonthYears.add(monthYear)
                val transactions = groupedMap[monthYear] ?: emptyList()
                
                var totalIn = 0L
                var totalOut = 0L
                
                transactions.forEach { trans ->
                    val amount = trans.amount.replace(Regex("[^0-9]"), "").toLongOrNull() ?: 0L
                    val isIncome = trans.type.lowercase().contains("pemasukan") || 
                                 trans.type.lowercase().contains("pemasukkan") ||
                                 trans.category.lowercase().contains("pemasukan") ||
                                 trans.category.lowercase().contains("pemasukkan")
                    
                    if (isIncome) totalIn += amount else totalOut += amount
                }
                
                resultList.add(FinanceRecapItem.Header(monthYear, totalIn, totalOut))
                transactions.forEach { trans ->
                    resultList.add(FinanceRecapItem.Transaction(trans))
                }
            }
        }

        return resultList
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
