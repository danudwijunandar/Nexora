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
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.nexora1.R
import com.example.nexora1.data.Result
import com.example.nexora1.data.local.prefs.SessionManager
import com.example.nexora1.data.local.room.UserEntity
import com.example.nexora1.data.remote.response.ActivityData
import com.example.nexora1.databinding.FragmentHomeBinding
import com.example.nexora1.ui.ViewModelFactory
import com.example.nexora1.ui.activity.ActivityAdapter
import com.example.nexora1.ui.activity.ActivityViewModel
import com.example.nexora1.ui.auth.AuthActivity
import com.example.nexora1.ui.profile.ProfileViewModel
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager
    private lateinit var adapter: ActivityAdapter
    private var selectedCalendar = Calendar.getInstance()
    private var fullList: List<ActivityData> = emptyList()
    private var currentUser: UserEntity? = null

    private val activityViewModel: ActivityViewModel by viewModels {
        ViewModelFactory.getInstance(requireContext())
    }

    private val profileViewModel: ProfileViewModel by viewModels {
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
        setupSwipeRefresh()
        
        binding.tvGreeting.text = "Halo, ${sessionManager.getUsername()}!"
        
        binding.ivNotification.setOnClickListener {
            findNavController().navigate(R.id.notificationFragment)
        }

        binding.ivProfile.setOnClickListener {
            showEnlargedImage()
        }

        binding.layoutEnlargeImage.setOnClickListener {
            binding.layoutEnlargeImage.visibility = View.GONE
        }

        binding.btnInfoProfile.setOnClickListener {
            binding.layoutEnlargeImage.visibility = View.GONE
            findNavController().navigate(R.id.profileFragment)
        }
        
        checkNotificationPermission()
        highlightSelectedDay()
        setupDayClickListeners()
        setupTimeFilterListeners()
        observeViewModel()
        
        syncData()
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
            activityViewModel.syncActivities(token)
        } else {
            handleUnauthorized()
        }
    }

    private fun showEnlargedImage() {
        binding.layoutEnlargeImage.visibility = View.VISIBLE
        currentUser?.profileImagePath?.let {
            Glide.with(this)
                .load(it)
                .placeholder(R.drawable.ic_logo)
                .into(binding.ivEnlarged)
        } ?: run {
            binding.ivEnlarged.setImageResource(R.drawable.ic_logo)
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
        var dayIndex = cal.get(Calendar.DAY_OF_WEEK) - 2
        if (dayIndex < 0) dayIndex = 6

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
                
                binding.chipToday.isChecked = true
                highlightSelectedDay()
                applyFilters()
            }
        }
    }

    private fun setupTimeFilterListeners() {
        binding.cgTimeFilter.setOnCheckedChangeListener { _, _ ->
            applyFilters()
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
                showStatusConfirmation(activity, isChecked)
            }
        )
        binding.rvTodayTask.layoutManager = LinearLayoutManager(context)
        binding.rvTodayTask.adapter = adapter
    }

    private fun showStatusConfirmation(activity: ActivityData, isChecked: Boolean) {
        if (isChecked) {
            AlertDialog.Builder(requireContext())
                .setTitle("Konfirmasi")
                .setMessage("Apakah anda sudah menyelesaikan aktivitas ini?")
                .setPositiveButton("Ya") { _, _ ->
                    updateStatus(activity, true)
                }
                .setNegativeButton("Tidak") { _, _ ->
                    syncData()
                }
                .setOnCancelListener {
                    syncData()
                }
                .show()
        } else {
            updateStatus(activity, false)
        }
    }

    private fun updateStatus(activity: ActivityData, isDone: Boolean) {
        val token = sessionManager.getToken() ?: ""
        val status = if (isDone) "3" else "1"
        activityViewModel.updateActivityStatus(token, activity.id, activity.title, status)
    }

    private fun observeViewModel() {
        activityViewModel.getActivities().observe(viewLifecycleOwner) { activities ->
            fullList = activities
            applyFilters()
        }

        activityViewModel.syncResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Result.Loading -> binding.swipeRefresh.isRefreshing = true
                is Result.Success -> binding.swipeRefresh.isRefreshing = false
                is Result.Error -> {
                    binding.swipeRefresh.isRefreshing = false
                    Toast.makeText(requireContext(), result.error, Toast.LENGTH_SHORT).show()
                }
            }
        }

        activityViewModel.updateStatusResult.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { result ->
                if (result is Result.Success) {
                    Toast.makeText(requireContext(), "Aktivitas '${result.data}' diperbarui", Toast.LENGTH_SHORT).show()
                } else if (result is Result.Error) {
                    Toast.makeText(requireContext(), result.error, Toast.LENGTH_SHORT).show()
                }
            }
        }

        val email = sessionManager.getEmail() ?: ""
        profileViewModel.getUserProfile(email).observe(viewLifecycleOwner) { user ->
            currentUser = user
            if (user != null) {
                binding.tvGreeting.text = "Halo, ${user.username}!"
                if (!user.profileImagePath.isNullOrEmpty()) {
                    Glide.with(this@HomeFragment)
                        .load(user.profileImagePath)
                        .placeholder(R.drawable.ic_logo)
                        .circleCrop()
                        .into(binding.ivProfile)
                } else {
                    binding.ivProfile.setImageResource(R.drawable.ic_logo)
                }
            }
        }
    }

    private fun applyFilters() {
        updateWeeklyChart(fullList)
        
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val filteredList: List<ActivityData>

        when (binding.cgTimeFilter.checkedChipId) {
            R.id.chipWeek -> {
                binding.tvListTitle.text = getString(R.string.this_week)
                val cal = Calendar.getInstance()
                val currentDayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
                val daysToSubtract = if (currentDayOfWeek == Calendar.SUNDAY) 6 else currentDayOfWeek - Calendar.MONDAY
                cal.add(Calendar.DAY_OF_YEAR, -daysToSubtract)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                val startOfWeek = cal.timeInMillis
                cal.add(Calendar.DAY_OF_YEAR, 7)
                val endOfWeek = cal.timeInMillis

                filteredList = fullList.filter {
                    try {
                        val dateSource = it.date ?: it.createdAt
                        val actDate = sdf.parse(dateSource.take(10))
                        actDate != null && actDate.time >= startOfWeek && actDate.time < endOfWeek
                    } catch (e: Exception) { false }
                }.sortedWith(compareBy<ActivityData> { 
                    if (it.status == "selesai" || it.status == "3") 1 else 0
                }.thenByDescending { it.date ?: it.createdAt })
            }
            R.id.chipMonth -> {
                binding.tvListTitle.text = getString(R.string.this_mounth)
                val currentMonth = Calendar.getInstance().get(Calendar.MONTH)
                val currentYear = Calendar.getInstance().get(Calendar.YEAR)
                filteredList = fullList.filter {
                    try {
                        val dateSource = it.date ?: it.createdAt
                        val actDate = sdf.parse(dateSource.take(10))
                        val calAct = Calendar.getInstance()
                        if (actDate != null) calAct.time = actDate
                        actDate != null && calAct.get(Calendar.MONTH) == currentMonth && calAct.get(Calendar.YEAR) == currentYear
                    } catch (e: Exception) { false }
                }.sortedWith(compareBy<ActivityData> { 
                    if (it.status == "selesai" || it.status == "3") 1 else 0
                }.thenByDescending { it.date ?: it.createdAt })
            }
            else -> {
                binding.tvListTitle.text = getString(R.string.today)
                val selectedDateStr = sdf.format(selectedCalendar.time)
                filteredList = fullList.filter { 
                    val dateSource = it.date ?: it.createdAt
                    dateSource.startsWith(selectedDateStr) 
                }.sortedWith(compareBy<ActivityData> { 
                    if (it.status == "selesai" || it.status == "3") 1 else 0
                }.thenByDescending { it.date ?: it.createdAt })
            }
        }

        val completed = filteredList.count { it.status == "selesai" || it.status == "3" }
        val percent = if (filteredList.isNotEmpty()) (completed * 100) / filteredList.size else 0

        binding.tvNumberActivity.text = filteredList.size.toString()
        binding.tvPercent.text = "$percent%"
        binding.pbActivity.progress = percent
        binding.pbProductivity.progress = percent

        adapter.submitList(filteredList)
        binding.tvEmptyState.visibility = if (filteredList.isEmpty()) View.VISIBLE else View.GONE
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
                val dateSource = activity.date ?: activity.createdAt
                val dateStr = dateSource.take(10)
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val actDate = sdf.parse(dateStr)
                if (actDate != null) {
                    val actTime = actDate.time
                    if (actTime >= startOfWeek && actTime < endOfWeek) {
                        val actCal = Calendar.getInstance()
                        actCal.time = dateSource.let { sdf.parse(it.take(10)) } ?: Date()
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
