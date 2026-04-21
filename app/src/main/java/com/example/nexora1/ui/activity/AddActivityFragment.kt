package com.example.nexora1.ui.activity

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.nexora1.R
import com.example.nexora1.data.Result
import com.example.nexora1.data.local.prefs.SessionManager
import com.example.nexora1.databinding.FragmentAddActivityBinding
import com.example.nexora1.ui.ViewModelFactory
import com.example.nexora1.utils.NotificationHelper
import java.text.SimpleDateFormat
import java.util.*

class AddActivityFragment : Fragment() {
    private var _binding: FragmentAddActivityBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager
    private var activityId: Int = -1
    private val calendar = Calendar.getInstance()

    private val viewModel: ActivityViewModel by viewModels {
        ViewModelFactory.getInstance(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddActivityBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionManager = SessionManager(requireContext())

        setupSpinner()
        updateReminderLabel()
        observeViewModel()

        arguments?.let {
            activityId = it.getInt("activityId", -1)
            
            if (activityId != -1) {
                binding.tvTitle.text = "Edit Aktivitas"
                binding.btnDelete.visibility = View.VISIBLE
                binding.layoutEditStatus.visibility = View.VISIBLE
                
                binding.tilTitleActivity.editText?.setText(it.getString("title"))
                binding.tilDescription.editText?.setText(it.getString("description"))
                
                val category = it.getString("category")
                if (category != null) {
                    val adapter = binding.spinnerKategori.adapter as ArrayAdapter<String>
                    val position = adapter.getPosition(category)
                    binding.spinnerKategori.setSelection(position)
                }

                binding.tilTitleActivity.isEnabled = false
                binding.tilDescription.isEnabled = false
                binding.spinnerKategori.isEnabled = false
                binding.btnPickDate.isEnabled = false
                binding.btnPickTime.isEnabled = false
                binding.btnTemplate.visibility = View.GONE

                val status = it.getString("status")
                when (status) {
                    "3", "selesai" -> binding.chipDone.isChecked = true
                    "2", "sedang dikerjakan" -> binding.chipWorking.isChecked = true
                    else -> binding.chipPending.isChecked = true
                }
            } else {
                it.getString("title")?.let { title -> binding.tilTitleActivity.editText?.setText(title) }
                it.getString("category")?.let { category ->
                    val adapter = binding.spinnerKategori.adapter as ArrayAdapter<String>
                    val position = adapter.getPosition(category)
                    binding.spinnerKategori.setSelection(position)
                }
                
                it.getString("selectedDate")?.let { dateStr ->
                    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    try {
                        val date = sdf.parse(dateStr)
                        if (date != null) {
                            val time = calendar.time
                            calendar.time = date
                            val calTime = Calendar.getInstance()
                            calTime.time = time
                            calendar.set(Calendar.HOUR_OF_DAY, calTime.get(Calendar.HOUR_OF_DAY))
                            calendar.set(Calendar.MINUTE, calTime.get(Calendar.MINUTE))
                            updateReminderLabel()
                        }
                    } catch (e: Exception) {
                        Log.e("AddActivityFragment", "Parse date error: ${e.message}")
                    }
                }
            }
        }

        binding.btnPickDate.setOnClickListener { showDatePicker() }
        binding.btnPickTime.setOnClickListener { showTimePicker() }
        binding.btnTemplate.setOnClickListener { findNavController().navigate(R.id.templateActivityFragment) }
        binding.btnBack.setOnClickListener { findNavController().navigateUp() }
        binding.btnSave.setOnClickListener { saveActivity() }
        // Note: For full structured code, delete should also be in ViewModel.
    }

    private fun observeViewModel() {
        viewModel.addActivityResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Result.Loading -> {
                    binding.btnSave.isEnabled = false
                }
                is Result.Success -> {
                    Toast.makeText(context, "Berhasil menyimpan aktivitas", Toast.LENGTH_SHORT).show()
                    // Schedule notification if needed (Simplified: logic usually inside VM or Repository)
                    findNavController().navigateUp()
                }
                is Result.Error -> {
                    binding.btnSave.isEnabled = true
                    Toast.makeText(context, result.error, Toast.LENGTH_SHORT).show()
                }
            }
        }
        
        viewModel.updateStatusResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Result.Loading -> {}
                is Result.Success -> {
                    Toast.makeText(context, "Status berhasil diperbarui", Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                }
                is Result.Error -> {
                    Toast.makeText(context, result.error, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showDatePicker() {
        DatePickerDialog(requireContext(), { _, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateReminderLabel()
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun showTimePicker() {
        TimePickerDialog(requireContext(), { _, hourOfDay, minute ->
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
            calendar.set(Calendar.MINUTE, minute)
            updateReminderLabel()
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
    }

    private fun updateReminderLabel() {
        val format = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
        binding.tvReminderInfo.text = "Pengingat: ${format.format(calendar.time)}"
    }

    private fun setupSpinner() {
        val categories = arrayOf("produktif","kesehatan")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerKategori.adapter = adapter
    }

    private fun saveActivity() {
        val title = binding.tilTitleActivity.editText?.text.toString()
        val description = binding.tilDescription.editText?.text.toString()
        val category = binding.spinnerKategori.selectedItem.toString()
        val token = sessionManager.getToken() ?: ""

        if (title.isEmpty()) {
            Toast.makeText(context, "Harap isi judul aktivitas", Toast.LENGTH_SHORT).show()
            return
        }

        if (activityId == -1) {
            val sdfDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val dateStr = sdfDate.format(calendar.time)
            viewModel.addActivity(token, title, description, category, dateStr)
        } else {
            val selectedChipId = binding.cgStatus.checkedChipId
            val status = when (selectedChipId) {
                R.id.chipDone -> "3"
                R.id.chipWorking -> "2"
                else -> "1"
            }
            viewModel.updateActivityStatus(token, activityId, title, status)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
