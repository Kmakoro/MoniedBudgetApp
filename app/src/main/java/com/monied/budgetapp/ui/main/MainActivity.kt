package com.monied.budgetapp.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.monied.budgetapp.R
import com.monied.budgetapp.databinding.ActivityMainBinding
import com.monied.budgetapp.ui.fragments.DashboardFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val bottomNav = binding.bottomNavigation
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_add -> {
                    startActivity(Intent(this, AddExpenseActivity::class.java))
                    true
                }
                R.id.navigation_history -> {
                    startActivity(Intent(this, ViewExpensesActivity::class.java))
                    true
                }
                R.id.navigation_category -> {
                    startActivity(Intent(this, CategoryActivity::class.java))
                    true
                }
                R.id.navigation_spending_goals -> {
                    startActivity(Intent(this, SavingsGoalActivity::class.java))
                    true
                }

                R.id.navigation_report -> {
                    val intent = android.content.Intent(this, com.monied.budgetapp.ui.main.SpendingReportActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }
        // Default fragment when MainActivity opens
        if (savedInstanceState == null) {
            replaceFragment(DashboardFragment())
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    fun openProfile() {
        replaceFragment(ProfileFragment())
    }
}