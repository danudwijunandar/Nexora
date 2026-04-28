package com.example.nexora1.ui.finance

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.nexora1.R
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
    private var originalDate: String? = null

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
                
                originalDate = it.getString("date")
                binding.tilDate.editText?.setText(originalDate?.substringBefore("T"))
                
                binding.tilNote.editText?.setText(it.getString("note"))
                
                val type = it.getString("type")
                if (type?.lowercase()?.contains("pemasukan") == true || type?.lowercase()?.contains("pemasukkan") == true) {
                    binding.chipPemasukan.isChecked = true
                } else {
                    binding.chipPengeluaran.isChecked = true
                }
                
                binding.tilNote.isEnabled = false
                binding.tilNote.alpha = 0.5f
                binding.tilNote.helperText = "Catatan tidak dapat diubah"

                binding.tilDate.isEnabled = false
                binding.tilDate.alpha = 0.5f
                binding.tilDate.helperText = "Tanggal tidak dapat diubah"
            }
        }

        binding.btnBack.setOnClickListener { findNavController().navigateUp() }
        binding.tilDate.editText?.setOnClickListener { 
            if (financeId == -1) {
                showDatePicker()
            }
        }
        binding.btnSave.setOnClickListener { saveFinance() }
        binding.btnDelete.setOnClickListener { showDeleteConfirmation() }

        observeViewModel()
    }

    private fun showDeleteConfirmation() {
        AlertDialog.Builder(requireContext())
            .setTitle("Hapus Keuangan")
            .setMessage("Apakah anda ingin menghapus ini?")
            .setPositiveButton("Ya") { _, _ ->
                val token = sessionManager.getToken() ?: ""
                viewModel.deleteFinance(token, financeId)
            }
            .setNegativeButton("Tidak", null)
            .show()
    }

    private fun observeViewModel() {
        viewModel.financeResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Result.Loading -> {
                    binding.btnSave.isEnabled = false
                    binding.progressBar.visibility = View.VISIBLE
                    binding.btnSave.text = ""
                }
                is Result.Success -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(context, "Data berhasil diproses", Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                }
                is Result.Error -> {
                    binding.btnSave.isEnabled = true
                    binding.progressBar.visibility = View.GONE
                    binding.btnSave.text = "Simpan"
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
            originalDate = date 
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun saveFinance() {
        val selectedChipId = binding.cgType.checkedChipId
        val category = binding.tilCategory.editText?.text.toString().trim()
        val amountRaw = binding.tilAmount.editText?.text.toString().trim()
        val dateText = binding.tilDate.editText?.text.toString().trim()
        val note = binding.tilNote.editText?.text.toString().trim()
        val token = sessionManager.getToken() ?: ""

        binding.tilCategory.error = null
        binding.tilAmount.error = null
        binding.tilDate.error = null
        binding.tilNote.error = null

        var isValid = true

        if (selectedChipId == View.NO_ID) {
            Toast.makeText(context, "Pilih tipe (Pemasukan/Pengeluaran)", Toast.LENGTH_SHORT).show()
            isValid = false
        }
        
        if (category.isEmpty()) {
            binding.tilCategory.error = "Kategori harus diisi"
            isValid = false
        }
        
        if (amountRaw.isEmpty()) {
            binding.tilAmount.error = "Jumlah harus diisi"
            isValid = false
        }
        
        if (dateText.isEmpty()) {
            binding.tilDate.error = "Tanggal harus diisi"
            isValid = false
        }

        if (financeId == -1 && note.isEmpty()) {
            binding.tilNote.error = "Catatan harus diisi"
            isValid = false
        }

        if (!isValid) return

        val type = binding.root.findViewById<Chip>(selectedChipId).text.toString().lowercase()
        val amountClean = amountRaw.replace(Regex("[^0-9]"), "")

        if (financeId == -1) {
            viewModel.addFinance(token, type, category, amountClean, dateText, note)
        } else {
            viewModel.updateFinance(token, financeId, type, category, amountClean)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
