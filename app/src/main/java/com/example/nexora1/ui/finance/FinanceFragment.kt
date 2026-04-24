package com.example.nexora1.ui.finance

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nexora1.R
import com.example.nexora1.data.local.prefs.SessionManager
import com.example.nexora1.data.remote.response.FinanceData
import com.example.nexora1.databinding.FragmentFinanceBinding
import com.example.nexora1.ui.ViewModelFactory
import com.example.nexora1.ui.auth.AuthActivity
import java.text.NumberFormat
import java.util.*

class FinanceFragment : Fragment() {
    private var _binding: FragmentFinanceBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: FinanceAdapter
    private lateinit var sessionManager: SessionManager
    
    private val viewModel: FinanceViewModel by viewModels {
        ViewModelFactory.getInstance(requireContext())
    }

    private var fullList: List<FinanceData> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFinanceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionManager = SessionManager(requireContext())

        setupRecyclerView()
        setupSearch()
        setupFilters()
        observeViewModel()
        
        val token = sessionManager.getToken() ?: ""
        if (token.isNotEmpty()) {
            viewModel.syncFinance(token)
        } else {
            handleUnauthorized()
        }
    }

    private fun setupRecyclerView() {
        adapter = FinanceAdapter { finance ->
            val bundle = Bundle().apply {
                putInt("financeId", finance.id)
                putString("type", finance.type)
                putString("category", finance.category)
                putString("amount", finance.amount)
                // Kirim tanggal lengkap tanpa dipotong agar formatnya tetap valid bagi server
                putString("date", finance.date)
                putString("note", finance.note)
            }
            findNavController().navigate(R.id.addFinanceFragment, bundle)
        }
        binding.rvFinance.layoutManager = LinearLayoutManager(context)
        binding.rvFinance.adapter = adapter
    }

    private fun setupSearch() {
        binding.searchFinance.setQueryHint("Cari aktivitas keuangan anda")
        binding.searchFinance.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false

            override fun onQueryTextChange(newText: String?): Boolean {
                applyFilters()
                return true
            }
        })
    }

    private fun setupFilters() {
        binding.cgFilter.setOnCheckedStateChangeListener { _, _ ->
            applyFilters()
        }
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
            }
            fullList = dataList
            updateSummary(fullList)
            applyFilters()
        }
    }

    private fun applyFilters() {
        val query = binding.searchFinance.query.toString().lowercase()
        val checkedChipId = binding.cgFilter.checkedChipId
        
        var filteredList = fullList

        if (checkedChipId != View.NO_ID) {
            when (checkedChipId) {
                R.id.chipIncome -> filteredList = filteredList.filter { 
                    it.type.lowercase().contains("pemasukan") || it.type.lowercase().contains("pemasukkan") ||
                    it.category.lowercase().contains("pemasukan") || it.category.lowercase().contains("pemasukkan")
                }
                R.id.chipExpense -> filteredList = filteredList.filter { 
                    it.type.lowercase().contains("pengeluaran") || it.category.lowercase().contains("pengeluaran")
                }
            }
        }

        if (query.isNotEmpty()) {
            filteredList = filteredList.filter {
                it.category.lowercase().contains(query) || it.note.lowercase().contains(query)
            }
        }

        adapter.submitList(filteredList)
    }

    private fun updateSummary(list: List<FinanceData>) {
        var totalIncome = 0L
        var totalExpense = 0L

        list.forEach {
            val amount = it.amount.replace(Regex("[^0-9]"), "").toLongOrNull() ?: 0L
            val isPemasukan = it.type.lowercase().contains("pemasukan") || 
                             it.type.lowercase().contains("pemasukkan") ||
                             it.category.lowercase().contains("pemasukan") ||
                             it.category.lowercase().contains("pemasukkan")

            if (isPemasukan) {
                totalIncome += amount
            } else {
                totalExpense += amount
            }
        }

        val totalBalance = totalIncome - totalExpense
        val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        format.maximumFractionDigits = 0

        val formattedBalance = format.format(totalBalance).replace("Rp", "Rp ")
        binding.tvTotalBalance.text = if (totalBalance >= 0) formattedBalance else formattedBalance.replace("Rp -", "-Rp ")
        binding.tvTotalIncome.text = format.format(totalIncome).replace("Rp", "Rp ")
        binding.tvTotalExpense.text = format.format(totalExpense).replace("Rp", "Rp ")
    }

    private fun handleUnauthorized() {
        sessionManager.logout()
        startActivity(Intent(requireContext(), AuthActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        requireActivity().finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
