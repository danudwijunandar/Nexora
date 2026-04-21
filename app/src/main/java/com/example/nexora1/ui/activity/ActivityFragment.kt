package com.example.nexora1.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nexora1.R
import com.example.nexora1.data.Result
import com.example.nexora1.data.local.prefs.SessionManager
import com.example.nexora1.data.remote.response.ActivityData
import com.example.nexora1.databinding.FragmentActivityBinding
import com.example.nexora1.ui.ViewModelFactory
import com.example.nexora1.ui.auth.AuthActivity
import java.text.SimpleDateFormat
import java.util.*

class ActivityFragment : Fragment() {
    private var _binding: FragmentActivityBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: ActivityAdapter
    private lateinit var sessionManager: SessionManager
    
    private val viewModel: ActivityViewModel by viewModels {
        ViewModelFactory.getInstance(requireContext())
    }

    private var fullList: List<ActivityData> = emptyList()
    private var selectedDate: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentActivityBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionManager = SessionManager(requireContext())

        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        selectedDate = sdf.format(Date())

        setupRecyclerView()
        setupCalendar()
        observeViewModel()
        
        val token = sessionManager.getToken() ?: ""
        if (token.isNotEmpty()) {
            viewModel.syncActivities(token)
        } else {
            handleUnauthorized()
        }
    }

    private fun setupRecyclerView() {
        adapter = ActivityAdapter(
            onItemClick = { activity ->
                val bundle = Bundle().apply {
                    putInt("activityId", activity.id)
                    putString("title", activity.title)
                    putString("description", activity.description)
                    putString("category", activity.categories)
                    putString("status", activity.status)
                    putString("moodRating", activity.moodRating)
                }
                findNavController().navigate(R.id.addActivityFragment, bundle)
            },
            onStatusChange = { activity, isChecked ->
                val token = sessionManager.getToken() ?: ""
                val status = if (isChecked) "3" else "1"
                viewModel.updateActivityStatus(token, activity.id, activity.title, status)
            }
        )
        binding.rvTask.layoutManager = LinearLayoutManager(context)
        binding.rvTask.adapter = adapter
    }

    private fun setupCalendar() {
        binding.calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val cal = Calendar.getInstance()
            cal.set(year, month, dayOfMonth)
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            selectedDate = sdf.format(cal.time)
            applyFilters()
        }
    }

    private fun applyFilters() {
        val filteredList = fullList.filter {
            it.createdAt.startsWith(selectedDate)
        }
        adapter.submitList(filteredList)
        // Check if rvTask has a placeholder or if I should just rely on adapter's empty state
    }

    private fun observeViewModel() {
        viewModel.getActivities().observe(viewLifecycleOwner) { activities ->
            fullList = activities
            applyFilters()
        }

        viewModel.updateStatusResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Result.Loading -> {
                    // Show loading if needed
                }
                is Result.Success -> {
                    Toast.makeText(requireContext(), "Aktivitas '${result.data}' diperbarui", Toast.LENGTH_SHORT).show()
                }
                is Result.Error -> {
                    Toast.makeText(requireContext(), result.error, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun handleUnauthorized() {
        sessionManager.logout()
        startActivity(Intent(requireContext(), AuthActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
