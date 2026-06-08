package com.monied.budgetapp

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import com.monied.budgetapp.data.database.MoniedDatabase

/**
 * Main Application class for Monied Budget App
 * Initializes database and other application-level dependencies
 */
class MoniedApplication : Application() {

    // Database instance (lazy initialization)
    val database: MoniedDatabase by lazy { MoniedDatabase.getDatabase(this) }

    override fun onCreate() {
        super.onCreate()

        // Apply Dark Mode preference on startup
        val prefs = getSharedPreferences("MoniedPrefs", Context.MODE_PRIVATE)
        val isDarkMode = prefs.getBoolean("isDarkMode", false)
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }
}
