package com.monied.budgetapp

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.monied.budgetapp.data.DatabaseHelper
import com.monied.budgetapp.data.BudgetAlertData
import com.monied.budgetapp.models.Category
import com.monied.budgetapp.models.Expense
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.text.SimpleDateFormat
import java.util.*

@RunWith(AndroidJUnit4::class)
class DatabaseHelperTest {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext<Context>()
        // Ensure database is closed and deleted before each test for total isolation
        context.deleteDatabase("MoniedProV4.db")
        dbHelper = DatabaseHelper(context)
    }

    @After
    fun tearDown() {
        if (this::dbHelper.isInitialized) {
            dbHelper.close()
        }
    }

    @Test
    fun testUserRegistrationAndLogin() {
        val timestamp = System.currentTimeMillis()
        val username = "testuser_$timestamp"
        val password = "password123"
        val fullName = "Test User"
        val email = "test_$timestamp@example.com"
        val phone = "1234567890"

        val result = dbHelper.registerUser(username, password, fullName, email, phone)
        assertTrue("Registration should be successful", result != -1L)

        val loginSuccess = dbHelper.checkUserCredentials(username, password)
        assertTrue("Login should be successful with correct credentials", loginSuccess)

        val wrongLogin = dbHelper.checkUserCredentials(username, "wrongpassword")
        assertFalse("Login should fail with wrong password", wrongLogin)
    }

    @Test
    fun testAddExpense() {
        val timestamp = System.currentTimeMillis()
        val userId = dbHelper.registerUser("user_$timestamp", "pass", "User", "u@t.com", "000").toInt()
        
        dbHelper.initializeUserCategories(userId)
        val categories = dbHelper.getAllCategories(userId)
        assertNotNull("Categories list should not be null", categories)
        assertTrue("Categories should be initialized", categories.isNotEmpty())
        
        val categoryId = categories[0].id
        val amount = 150.50
        val description = "Test Expense $timestamp"
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

        val expenseId = dbHelper.addExpense(amount, description, date, "14:00", "15:00", categoryId, userId)
        assertTrue("Expense should be added successfully", expenseId != -1L)

        val expenses = dbHelper.getExpensesByDateRange(date, date, userId)
        assertTrue("Added expense should be in the retrieved list", expenses.any { it.description == description })
    }

    @Test
    fun testBudgetAlerts() {
        val timestamp = System.currentTimeMillis()
        val userId = dbHelper.registerUser("alertuser_$timestamp", "pass", "Alert User", "al@t.com", "111").toInt()
        val currentMonth = SimpleDateFormat("yyyy-MM", Locale.US).format(Date())
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        
        // Set a low budget for the CURRENT month to trigger alerts
        dbHelper.updateBudgetGoal(currentMonth, 10.0, 50.0, userId)
        
        dbHelper.initializeUserCategories(userId)
        val categories = dbHelper.getAllCategories(userId)
        assertTrue("Should have categories", categories.isNotEmpty())
        val categoryId = categories[0].id
        
        // Add an expense for TODAY that exceeds the 50.0 limit
        dbHelper.addExpense(100.0, "High Spending", currentDate, "10:00", "11:00", categoryId, userId)
        
        val alerts = dbHelper.getAlerts(userId)
        assertTrue("Should have at least one alert generated", alerts.isNotEmpty())
        assertTrue("Alert should be 'Budget Exceeded'", alerts.any { it.title == "Budget Exceeded" })
    }
}