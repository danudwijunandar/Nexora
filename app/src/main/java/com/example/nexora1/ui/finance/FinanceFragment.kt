package com.example.nexora1.ui.finance

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.NumberPicker
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nexora1.R
import com.example.nexora1.data.Result
import com.example.nexora1.data.local.prefs.SessionManager
import com.example.nexora1.data.remote.response.FinanceData
import com.example.nexora1.databinding.FragmentFinanceBinding
import com.example.nexora1.ui.ViewModelFactory
import com.example.nexora1.ui.auth.AuthActivity
import java.text.DateFormatSymbols
import java.text.NumberFormat
import java.text.SimpleDateFormat
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
    private var selectedCalendar = Calendar.getInstance()

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
        setupSwipeRefresh()
        setupSearch()
        setupFilters()
        setupActions()
        observeViewModel()
        
        updatePeriodDisplay()
        syncData()
    }

    private fun setupActions() {
        binding.btnRecap.setOnClickListener {
            findNavController().navigate(R.id.action_financeFragment_to_financeRecapFragment)
        }
        
        binding.llHeaderBalance.setOnClickListener {
            showMonthYearPicker()
        }
    }

    private fun showMonthYearPicker() {
        val view = layoutInflater.inflate(R.layout.dialog_month_year_picker, null)
        val monthPicker = view.findViewById<NumberPicker>(R.id.monthPicker)
        val yearPicker = view.findViewById<NumberPicker>(R.id.yearPicker)

        val months = DateFormatSymbols().months
        monthPicker.minValue = 0
        monthPicker.maxValue = 11
        monthPicker.displayedValues = months
        monthPicker.value = selectedCalendar.get(Calendar.MONTH)

        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        yearPicker.minValue = currentYear - 5
        yearPicker.maxValue = currentYear + 5
        yearPicker.value = selectedCalendar.get(Calendar.YEAR)

        AlertDialog.Builder(requireContext())
            .setTitle("Pilih Periode")
            .setView(view)
            .setPositiveButton("Pilih") { _, _ ->
                selectedCalendar.set(Calendar.MONTH, monthPicker.value)
                selectedCalendar.set(Calendar.YEAR, yearPicker.value)
                updatePeriodDisplay()
                applyFilters()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun updatePeriodDisplay() {
        val sdf = SimpleDateFormat("MMMM yyyy", Locale("id", "ID"))
        binding.tvBalancePeriod.text = sdf.format(selectedCalendar.time)
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            syncData()
        }
        binding.swipeRefresh.setColorSchemeResources(R.color.colorPrimary)
    }

    private fun syncData() {
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
            applyFilters()
        }

        viewModel.syncResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Result.Loading -> binding.swipeRefresh.isRefreshing = true
                is Result.Success -> binding.swipeRefresh.isRefreshing = false
                is Result.Error -> {
                    binding.swipeRefresh.isRefreshing = false
                    Toast.makeText(requireContext(), result.error, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun applyFilters() {
        val query = binding.searchFinance.query.toString().lowercase()
        val checkedChipId = binding.cgFilter.checkedChipId
        
        val selectedMonth = selectedCalendar.get(Calendar.MONTH)
        val selectedYear = selectedCalendar.get(Calendar.YEAR)

        var filteredList = fullList.filter { item ->
            try {
                val dateStr = item.date
                val sdf = if (dateStr.contains("T")) {
                    SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                } else {
                    SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                }
                val date = sdf.parse(dateStr)
                if (date != null) {
                    val cal = Calendar.getInstance()
                    cal.time = date
                    cal.get(Calendar.MONTH) == selectedMonth && cal.get(Calendar.YEAR) == selectedYear
                } else false
            } catch (e: Exception) {
                false
            }
        }

        updateSummary(filteredList)

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
        binding.tvEmptyState.visibility = if (filteredList.isEmpty()) View.VISIBLE else View.GONE
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
