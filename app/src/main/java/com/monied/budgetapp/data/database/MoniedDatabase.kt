package com.monied.budgetapp.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.monied.budgetapp.data.dao.*
import com.monied.budgetapp.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Date

/**
 * Main Room Database for Monied App
 */
@Database(
    entities = [
        Expense::class,
        Category::class,
        BudgetGoal::class,
        SavingsGoal::class,
        BudgetAlert::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class MoniedDatabase : RoomDatabase() {

    abstract fun expenseDao(): ExpenseDao
    abstract fun categoryDao(): CategoryDao
    abstract fun budgetGoalDao(): BudgetGoalDao
    abstract fun savingsGoalDao(): SavingsGoalDao
    abstract fun budgetAlertDao(): BudgetAlertDao

    companion object {
        @Volatile
        private var INSTANCE: MoniedDatabase? = null

        fun getDatabase(context: Context): MoniedDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MoniedDatabase::class.java,
                    "monied_database"
                )
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }

        /**
         * Callback to populate database with initial data
         */
        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateDatabase(database)
                    }
                }
            }
        }

        /**
         * Populate database with initial categories only
         */
        private suspend fun populateDatabase(database: MoniedDatabase) {
            val categoryDao = database.categoryDao()

            // Insert default categories
            val categories = listOf(
                Category(name = "Groceries", color = "#3B82F6", icon = "🛒"),
                Category(name = "Transport", color = "#A855F7", icon = "🚗"),
                Category(name = "Entertainment", color = "#EC4899", icon = "🎬"),
                Category(name = "Healthcare", color = "#EF4444", icon = "🏥"),
                Category(name = "Utilities", color = "#F97316", icon = "💡"),
                Category(name = "Personal", color = "#14B8A6", icon = "👤")
            )

            categories.forEach { category ->
                categoryDao.insertCategory(category)
            }
        }
    }
}
