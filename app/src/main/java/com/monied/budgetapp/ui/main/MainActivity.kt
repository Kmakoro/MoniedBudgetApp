package com.monied.budgetapp.ui.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.monied.budgetapp.R
import com.monied.budgetapp.databinding.ActivityMainBinding
import com.monied.budgetapp.ui.fragments.DashboardFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    replaceFragment(DashboardFragment())
                    true
                }
                R.id.navigation_history -> {
                    replaceFragment(HistoryFragment())
                    true
                }
                R.id.navigation_add -> {
                    // Navigate to Add Expense - using Fragment now to keep Nav Bar
                    replaceFragment(AddExpenseFragment()) 
                    true
                }
                R.id.navigation_analytics -> {
                    replaceFragment(AnalyticsFragment())
                    true
                }
                R.id.navigation_profile -> {
                    replaceFragment(ProfileFragment())
                    true
                }
                else -> false
            }
        }

        // Default fragment when MainActivity opens
        if (savedInstanceState == null) {
            binding.bottomNavigation.selectedItemId = R.id.navigation_home
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    fun openProfile() {
        binding.bottomNavigation.selectedItemId = R.id.navigation_profile
    }
}