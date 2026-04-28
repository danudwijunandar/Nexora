package com.example.nexora1

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import com.example.nexora1.data.local.prefs.SessionManager
import com.example.nexora1.databinding.ActivityMainBinding
import com.example.nexora1.ui.AddSelectionBottomSheet
import com.example.nexora1.ui.auth.AuthActivity
import androidx.work.*
import com.example.nexora1.utils.DailyActivityWorker
import java.util.*
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        if (!sessionManager.isLoggedIn()) {
            startActivity(Intent(this, AuthActivity::class.java))
            finish()
            return
        }

        scheduleDailyActivityWorker()

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_main) as NavHostFragment
        val navController = navHostFragment.navController

        val navOptions = androidx.navigation.NavOptions.Builder()
            .setLaunchSingleTop(true)
            .setPopUpTo(R.id.homeFragment, false)
            .build()

        binding.navHome.setOnClickListener {
            navController.navigate(R.id.homeFragment, null, navOptions)
        }
        binding.navActivity.setOnClickListener {
            navController.navigate(R.id.activityFragment, null, navOptions)
        }
        binding.navFinance.setOnClickListener {
            navController.navigate(R.id.financeFragment, null, navOptions)
        }
        binding.navProfile.setOnClickListener {
            navController.navigate(R.id.profileFragment, null, navOptions)
        }

        binding.fabAdd.setOnClickListener {
            val bottomSheet = AddSelectionBottomSheet { selectionType ->
                when (selectionType) {
                    AddSelectionBottomSheet.SelectionType.ACTIVITY -> {
                        navController.navigate(R.id.addActivityFragment)
                    }
                    AddSelectionBottomSheet.SelectionType.FINANCE -> {
                        navController.navigate(R.id.addFinanceFragment)
                    }
                }
            }
            bottomSheet.show(supportFragmentManager, "AddSelectionBottomSheet")
        }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            updateBottomNavUI(destination.id)
        }
    }

    private fun scheduleDailyActivityWorker() {
        val calendar = Calendar.getInstance()
        val now = calendar.timeInMillis
        
        calendar.set(Calendar.HOUR_OF_DAY, 5)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        
        if (calendar.timeInMillis <= now) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
        
        val delay = calendar.timeInMillis - now

        val dailyWorkRequest = PeriodicWorkRequestBuilder<DailyActivityWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .build()

        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            "DailyActivityWorker",
            ExistingPeriodicWorkPolicy.KEEP,
            dailyWorkRequest
        )
    }

    private fun updateBottomNavUI(destinationId: Int) {
        val primaryColor = getColor(R.color.colorPrimary)
        val grayColor = getColor(R.color.text_gray)

        binding.ivHome.setColorFilter(if (destinationId == R.id.homeFragment) primaryColor else grayColor)
        binding.tvHome.setTextColor(if (destinationId == R.id.homeFragment) primaryColor else grayColor)

        binding.ivActivity.setColorFilter(if (destinationId == R.id.activityFragment) primaryColor else grayColor)
        binding.tvActivity.setTextColor(if (destinationId == R.id.activityFragment) primaryColor else grayColor)

        binding.ivFinance.setColorFilter(if (destinationId == R.id.financeFragment) primaryColor else grayColor)
        binding.tvFinance.setTextColor(if (destinationId == R.id.financeFragment) primaryColor else grayColor)

        binding.ivProfile.setColorFilter(if (destinationId == R.id.profileFragment) primaryColor else grayColor)
        binding.tvProfile.setTextColor(if (destinationId == R.id.profileFragment) primaryColor else grayColor)
    }
}