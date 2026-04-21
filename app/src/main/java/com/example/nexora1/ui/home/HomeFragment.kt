package com.example.nexora1.ui.home

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nexora1.R
import com.example.nexora1.data.Result
import com.example.nexora1.data.local.prefs.SessionManager
import com.example.nexora1.data.remote.response.ActivityData
import com.example.nexora1.databinding.FragmentHomeBinding
import com.example.nexora1.ui.ViewModelFactory
import com.example.nexora1.ui.activity.ActivityAdapter
import com.example.nexora1.ui.activity.ActivityViewModel
import com.example.nexora1.ui.auth.AuthActivity
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager
    private lateinit var adapter: ActivityAdapter
    private var selectedCalendar = Calendar.getInstance()
    private var fullList: List<ActivityData> = emptyList()

    private val viewModel: ActivityViewModel by viewModels {
        ViewModelFactory.getInstance(requireContext())
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                Toast.makeText(requireContext(), "Notifikasi diizinkan", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionManager = SessionManager(requireContext())
        
        setupRecyclerView()
        binding.tvGreeting.text = "Halo, ${sessionManager.getUsername()}!"
        
        binding.ivNotification.setOnClickListener {
            findNavController().navigate(R.id.notificationFragment)
        }
        
        checkNotificationPermission()
        highlightSelectedDay()
        setupDayClickListeners()
        observeViewModel()
        
        val token = sessionManager.getToken() ?: ""
        if (token.isNotEmpty()) {
            viewModel.syncActivities(token)
        } else {
            handleUnauthorized()
        }
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun highlightSelectedDay() {
        val daysViews = listOf(
            binding.tvSenin, binding.tvSelasa, binding.tvRabu,
            binding.tvKamis, binding.tvJumat, binding.tvSabtu, binding.tvMinggu
        )

        val cal = Calendar.getInstance()
        cal.time = selectedCalendar.time
        var dayIndex = cal.get(Calendar.DAY_OF_WEEK) - 2 // Mon=0
        if (dayIndex < 0) dayIndex = 6 // Sun=6

        daysViews.forEachIndexed { index, textView ->
            if (index == dayIndex) {
                textView.setBackgroundResource(R.drawable.bg_solid_card)
                textView.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.colorPrimary)
                textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                textView.setPadding(0, 8, 0, 8)
                binding.weeklyChart.setSelectedDay(index)
            } else {
                textView.background = null
                textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_gray))
            }
        }
    }

    private fun setupDayClickListeners() {
        val daysViews = listOf(
            binding.tvSenin, binding.tvSelasa, binding.tvRabu,
            binding.tvKamis, binding.tvJumat, binding.tvSabtu, binding.tvMinggu
        )
        
        daysViews.forEachIndexed { index, textView ->
            textView.setOnClickListener {
                val cal = Calendar.getInstance()
                val currentDayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
                var currentDayIndex = currentDayOfWeek - 2
                if (currentDayIndex < 0) currentDayIndex = 6
                
                cal.add(Calendar.DAY_OF_YEAR, index - currentDayIndex)
                selectedCalendar = cal
                
                highlightSelectedDay()
                applyFilters()
            }
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
        binding.rvTodayTask.layoutManager = LinearLayoutManager(context)
        binding.rvTodayTask.adapter = adapter
    }

    private fun observeViewModel() {
        viewModel.getActivities().observe(viewLifecycleOwner) { activities ->
            fullList = activities
            applyFilters()
        }

        viewModel.updateStatusResult.observe(viewLifecycleOwner) { result ->
            if (result is Result.Success) {
                Toast.makeText(requireContext(), "Aktivitas '${result.data}' diperbarui", Toast.LENGTH_SHORT).show()
            } else if (result is Result.Error) {
                Toast.makeText(requireContext(), result.error, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun applyFilters() {
        updateWeeklyChart(fullList)

        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val selectedDateStr = sdf.format(selectedCalendar.time)
        val displayActivities = fullList.filter { it.createdAt.startsWith(selectedDateStr) }
        
        val completed = displayActivities.count { it.status == "selesai" || it.status == "3" }
        val percent = if (displayActivities.isNotEmpty()) (completed * 100) / displayActivities.size else 0

        binding.tvNumberActivity.text = displayActivities.size.toString()
        binding.tvPercent.text = "$percent%"
        binding.pbActivity.progress = percent
        binding.pbProductivity.progress = percent

        adapter.submitList(displayActivities)
    }

    private fun updateWeeklyChart(activities: List<ActivityData>) {
        val cal = Calendar.getInstance()
        val currentDayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
        var daysToSubtract = if (currentDayOfWeek == Calendar.SUNDAY) 6 else currentDayOfWeek - Calendar.MONDAY
        
        cal.add(Calendar.DAY_OF_YEAR, -daysToSubtract)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val startOfWeek = cal.timeInMillis
        
        cal.add(Calendar.DAY_OF_YEAR, 7)
        val endOfWeek = cal.timeInMillis
        
        val activityCounts = MutableList(7) { 0 }
        
        activities.forEach { activity ->
            try {
                val dateStr = activity.createdAt.take(10)
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val actDate = sdf.parse(dateStr)
                if (actDate != null) {
                    val actTime = actDate.time
                    if (actTime >= startOfWeek && actTime < endOfWeek) {
                        val actCal = Calendar.getInstance()
                        actCal.time = actDate
                        var idx = actCal.get(Calendar.DAY_OF_WEEK) - 2
                        if (idx < 0) idx = 6
                        if (idx in 0..6) {
                            activityCounts[idx]++
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("HomeFragment", "Chart parse error: ${e.message}")
            }
        }

        val maxActivities = activityCounts.maxOrNull()?.toFloat() ?: 0f
        val chartValues = activityCounts.map { count ->
            if (maxActivities == 0f || count == 0) {
                0.95f 
            } else {
                0.95f - (count.toFloat() / maxActivities) * 0.8f
            }
        }

        binding.weeklyChart.setData(chartValues)
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
