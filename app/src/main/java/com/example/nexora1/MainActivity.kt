package com.example.nexora1

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.nexora1.data.local.prefs.SessionManager
import com.example.nexora1.databinding.ActivityMainBinding
import com.example.nexora1.ui.AddSelectionBottomSheet
import com.example.nexora1.ui.auth.AuthActivity
import com.example.nexora1.utils.NotificationHelper

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

        // Schedule daily notification at 8 PM
        NotificationHelper.scheduleDailyReminder(this)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_main) as NavHostFragment
        val navController = navHostFragment.navController

        binding.navHome.setOnClickListener { navController.navigate(R.id.homeFragment) }
        binding.navActivity.setOnClickListener { navController.navigate(R.id.activityFragment) }
        binding.navFinance.setOnClickListener { navController.navigate(R.id.financeFragment) }
        binding.navProfile.setOnClickListener { navController.navigate(R.id.profileFragment) }
        
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