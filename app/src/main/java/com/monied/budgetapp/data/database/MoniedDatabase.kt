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
         * Populate database with mock data
         */
        private suspend fun populateDatabase(database: MoniedDatabase) {
            val categoryDao = database.categoryDao()
            val expenseDao = database.expenseDao()
            val budgetGoalDao = database.budgetGoalDao()
            val savingsGoalDao = database.savingsGoalDao()
            val alertDao = database.budgetAlertDao()

            // Insert default categories
            val categories = listOf(
                Category(name = "Groceries", color = "#3B82F6", icon = "🛒"),
                Category(name = "Transport", color = "#A855F7", icon = "🚗"),
                Category(name = "Entertainment", color = "#EC4899", icon = "🎬"),
                Category(name = "Healthcare", color = "#EF4444", icon = "🏥"),
                Category(name = "Utilities", color = "#F97316", icon = "💡"),
                Category(name = "Personal", color = "#14B8A6", icon = "👤")
            )

            val categoryIds = mutableListOf<Long>()
            categories.forEach { category ->
                categoryIds.add(categoryDao.insertCategory(category))
            }

            // Insert mock expenses inside Database
            val now = Date()
            val mockExpenses = listOf(
                Expense(
                    amount = 450.0,
                    date = now,
                    startTime = "10:30",
                    categoryId = categoryIds[0],
                    description = "Groceries at Amalinda Spar"
                ),
                Expense(
                    amount = 120.0,
                    date = Date(now.time - 86400000), // Yesterday
                    startTime = "14:15",
                    categoryId = categoryIds[1],
                    description = "Taxi to work"
                ),
                Expense(
                    amount = 325.0,
                    date = Date(now.time - 172800000), // 2 days ago
                    startTime = "19:00",
                    categoryId = categoryIds[2],
                    description = "Movie tickets"
                )
            )

            mockExpenses.forEach { expense ->
                expenseDao.insertExpense(expense)
            }

            // Update category expense counts
            categoryIds.forEach { categoryId ->
                categoryDao.updateExpenseCount(categoryId)
            }

            // Insert budget goal for current month
            val currentMonth = "2026-03" // March 2026
            budgetGoalDao.insertBudgetGoal(
                BudgetGoal(
                    month = currentMonth,
                    minimumGoal = 500.0,
                    maximumGoal = 2000.0
                )
            )

            // Insert mock savings goals
            savingsGoalDao.insertSavingsGoal(
                SavingsGoal(
                    name = "Emergency Fund",
                    targetAmount = 10000.0,
                    currentAmount = 6500.0,
                    deadline = Date(System.currentTimeMillis() + 25056000000), // ~290 days
                    icon = "🛡️",
                    color = "#3B82F6"
                )
            )

            savingsGoalDao.insertSavingsGoal(
                SavingsGoal(
                    name = "Vacation to Cape Town",
                    targetAmount = 5000.0,
                    currentAmount = 2800.0,
                    deadline = Date(System.currentTimeMillis() + 9158400000), // ~106 days
                    icon = "✈️",
                    color = "#A855F7"
                )
            )

            savingsGoalDao.insertSavingsGoal(
                SavingsGoal(
                    name = "New Laptop",
                    targetAmount = 15000.0,
                    currentAmount = 12000.0,
                    deadline = Date(System.currentTimeMillis() + 2592000000), // ~30 days
                    icon = "💻",
                    color = "#EC4899"
                )
            )

            // Insert mock alerts
            alertDao.insertAlert(
                BudgetAlert(
                    title = "Approaching Maximum Budget",
                    message = "You've spent R 1,245 of your R 2,000 maximum budget (62%)",
                    type = AlertType.BUDGET_WARNING,
                    severity = AlertSeverity.WARNING,
                    category = "Overall Budget"
                )
            )

            alertDao.insertAlert(
                BudgetAlert(
                    title = "Category Budget Exceeded",
                    message = "Groceries spending (R 450) exceeded your R 400 category limit",
                    type = AlertType.CATEGORY_ALERT,
                    severity = AlertSeverity.CRITICAL,
                    category = "Groceries"
                )
            )
        }
    }
}
