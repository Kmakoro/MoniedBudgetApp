package com.monied.budgetapp

import android.app.Application
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
        // Initialize any application-level components here
    }
}
