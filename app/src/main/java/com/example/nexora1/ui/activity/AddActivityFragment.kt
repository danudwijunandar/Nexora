package com.example.nexora1.ui.activity

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
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
                binding.tvTitle.text = getString(R.string.edit_activity_title)
                binding.btnSave.text = getString(R.string.edit_activity_button)
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

                binding.btnTemplate.visibility = View.GONE

                val status = it.getString("status")
                if (status == "3" || status == "selesai") {
                    binding.chipDone.isChecked = true
                } else {
                    binding.chipPending.isChecked = true
                }
            } else {
                it.getString("title")?.let { title -> binding.tilTitleActivity.editText?.setText(title) }
                it.getString("category")?.let { category ->
                    val adapter = binding.spinnerKategori.adapter as ArrayAdapter<String>
                    val position = adapter.getPosition(category)
                    binding.spinnerKategori.setSelection(position)
                }
            }
        }

        binding.btnPickDate.setOnClickListener { showDatePicker() }
        binding.btnPickTime.setOnClickListener { showTimePicker() }
        binding.btnTemplate.setOnClickListener { findNavController().navigate(R.id.templateActivityFragment) }
        binding.btnBack.setOnClickListener { findNavController().navigateUp() }
        binding.btnSave.setOnClickListener { saveActivity() }
        binding.btnDelete.setOnClickListener { showDeleteConfirmation() }
    }

    private fun showDeleteConfirmation() {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.delete_activity_dialog_title))
            .setMessage(getString(R.string.delete_activity_dialog_message))
            .setPositiveButton(getString(R.string.yes)) { _, _ ->
                val token = sessionManager.getToken() ?: ""
                viewModel.deleteActivity(token, activityId)
            }
            .setNegativeButton(getString(R.string.no), null)
            .show()
    }

    private fun observeViewModel() {
        viewModel.addActivityResult.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { result ->
                when (result) {
                    is Result.Loading -> {
                        binding.btnSave.isEnabled = false
                        binding.progressBar.visibility = View.VISIBLE
                        binding.btnSave.text = ""
                    }
                    is Result.Success -> {
                        binding.progressBar.visibility = View.GONE
                        val delay = calendar.timeInMillis - System.currentTimeMillis()
                        if (delay > 0) {
                            NotificationHelper.scheduleActivityReminder(
                                requireContext(), 
                                result.data.id, 
                                result.data.title, 
                                delay
                            )
                        }
                        Toast.makeText(context, getString(R.string.activity_saved_success), Toast.LENGTH_SHORT).show()
                        findNavController().navigateUp()
                    }
                    is Result.Error -> {
                        binding.btnSave.isEnabled = true
                        binding.progressBar.visibility = View.GONE
                        binding.btnSave.text = if (activityId == -1) getString(R.string.insert_my_list) else getString(R.string.edit_activity_button)
                        Toast.makeText(context, result.error, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        
        viewModel.updateStatusResult.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { result ->
                when (result) {
                    is Result.Loading -> {
                        binding.btnSave.isEnabled = false
                        binding.progressBar.visibility = View.VISIBLE
                        binding.btnSave.text = ""
                    }
                    is Result.Success -> {
                        binding.progressBar.visibility = View.GONE
                        Toast.makeText(context, getString(R.string.activity_updated_success), Toast.LENGTH_SHORT).show()
                        findNavController().navigateUp()
                    }
                    is Result.Error -> {
                        binding.btnSave.isEnabled = true
                        binding.progressBar.visibility = View.GONE
                        binding.btnSave.text = getString(R.string.edit_activity_button)
                        Toast.makeText(context, result.error, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        viewModel.deleteActivityResult.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { result ->
                when (result) {
                    is Result.Loading -> binding.btnDelete.isEnabled = false
                    is Result.Success -> {
                        Toast.makeText(context, getString(R.string.activity_deleted_success), Toast.LENGTH_SHORT).show()
                        findNavController().navigateUp()
                    }
                    is Result.Error -> {
                        binding.btnDelete.isEnabled = true
                        Toast.makeText(context, result.error, Toast.LENGTH_SHORT).show()
                    }
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
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            updateReminderLabel()
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
    }

    private fun updateReminderLabel() {
        val format = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
        binding.tvReminderInfo.text = getString(R.string.reminder_label, format.format(calendar.time))
    }

    private fun setupSpinner() {
        val categories = arrayOf("Produktif", "Kesehatan")
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
            Toast.makeText(context, getString(R.string.title_empty_error), Toast.LENGTH_SHORT).show()
            return
        }

        if (activityId == -1) {
            val sdfFull = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val dateTimeStr = sdfFull.format(calendar.time)
            
            viewModel.addActivity(token, title, description, category, dateTimeStr)
        } else {
            val selectedChipId = binding.cgStatus.checkedChipId
            val status = if (selectedChipId == R.id.chipDone) "3" else "1"
            viewModel.updateActivityStatus(token, activityId, title, status)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
