package com.example.nexora1.ui.finance

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.nexora1.data.Result
import com.example.nexora1.data.local.prefs.SessionManager
import com.example.nexora1.databinding.FragmentAddFinanceBinding
import com.example.nexora1.ui.ViewModelFactory
import com.google.android.material.chip.Chip
import java.util.*

class AddFinanceFragment : Fragment() {
    private var _binding: FragmentAddFinanceBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager
    private var financeId: Int = -1

    private val viewModel: FinanceViewModel by viewModels {
        ViewModelFactory.getInstance(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddFinanceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionManager = SessionManager(requireContext())

        arguments?.let {
            financeId = it.getInt("financeId", -1)
            if (financeId != -1) {
                binding.tvTitle.text = "Edit Keuangan"
                binding.btnDelete.visibility = View.VISIBLE
                binding.tilCategory.editText?.setText(it.getString("category"))
                binding.tilAmount.editText?.setText(it.getString("amount"))
                binding.tilDate.editText?.setText(it.getString("date"))
                binding.tilNote.editText?.setText(it.getString("note"))
                val type = it.getString("type")
                if (type == "pemasukan") binding.chipPemasukan.isChecked = true
                else if (type == "pengeluaran") binding.chipPengeluaran.isChecked = true
                
                binding.tilNote.isEnabled = false
            }
        }

        binding.btnBack.setOnClickListener { findNavController().navigateUp() }
        binding.tilDate.editText?.setOnClickListener { 
            if (financeId == -1 || binding.tilDate.isEnabled) showDatePicker() 
        }
        binding.btnSave.setOnClickListener { saveFinance() }
        binding.btnDelete.setOnClickListener { 
            val token = sessionManager.getToken() ?: ""
            viewModel.deleteFinance(token, financeId)
        }

        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.financeResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Result.Loading -> {
                    binding.btnSave.isEnabled = false
                }
                is Result.Success -> {
                    Toast.makeText(context, "Data berhasil diproses", Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                }
                is Result.Error -> {
                    binding.btnSave.isEnabled = true
                    Toast.makeText(context, result.error, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        DatePickerDialog(requireContext(), { _, year, month, dayOfMonth ->
            val date = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
            binding.tilDate.editText?.setText(date)
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun saveFinance() {
        val selectedChipId = binding.cgType.checkedChipId
        if (selectedChipId == View.NO_ID) {
            Toast.makeText(context, "Pilih tipe (Pemasukan/Pengeluaran)", Toast.LENGTH_SHORT).show()
            return
        }
        
        val type = binding.root.findViewById<Chip>(selectedChipId).text.toString().lowercase()
        val category = binding.tilCategory.editText?.text.toString().trim()
        val amountRaw = binding.tilAmount.editText?.text.toString().trim()
        val date = binding.tilDate.editText?.text.toString().trim()
        val note = binding.tilNote.editText?.text.toString().trim()
        val token = sessionManager.getToken() ?: ""

        if (category.isEmpty() || amountRaw.isEmpty() || date.isEmpty()) {
            Toast.makeText(context, "Harap isi semua field", Toast.LENGTH_SHORT).show()
            return
        }

        val amountClean = amountRaw.replace(Regex("[^0-9]"), "")
        
        if (financeId == -1) {
            viewModel.addFinance(token, type, category, amountClean, date, note)
        } else {
            viewModel.updateFinance(token, financeId, type, category, amountClean, date)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
