package com.monied.budgetapp

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.monied.budgetapp.data.DatabaseHelper
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DatabaseHelperTest {

    private lateinit var dbHelper: DatabaseHelper

    @Before
    fun setUp() {
        dbHelper = DatabaseHelper(ApplicationProvider.getApplicationContext())
    }

    @After
    fun tearDown() {
        dbHelper.close()
    }

    @Test
    fun testUserRegistrationAndLogin() {
        val username = "testuser"
        val password = "password123"
        val fullName = "Test User"
        val email = "test@example.com"
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
        // Register a user first to get an ID
        val userId = dbHelper.registerUser("expenseuser", "pass", "Expense User", "ex@test.com", "000").toInt()
        
        // Ensure categories are initialized
        dbHelper.initializeUserCategories(userId)
        val categories = dbHelper.getAllCategories(userId)
        assertTrue("Categories should be initialized", categories.isNotEmpty())
        
        val categoryId = categories[0].id
        val amount = 100.0
        val description = "Lunch"
        val date = "2024-05-20"

        val expenseId = dbHelper.addExpense(amount, description, date, "12:00", "13:00", categoryId, userId)
        assertTrue("Expense should be added", expenseId != -1L)

        val expenses = dbHelper.getExpensesByDateRange(date, date, userId)
        assertTrue("Expense should be retrievable", expenses.any { it.description == description })
    }

    @Test
    fun testBudgetAlerts() {
        val userId = dbHelper.registerUser("alertuser", "pass", "Alert User", "al@test.com", "111").toInt()
        val month = "2024-05"
        
        // Set a low budget
        dbHelper.updateBudgetGoal(month, 0.0, 50.0, userId)
        
        dbHelper.initializeUserCategories(userId)
        val categoryId = dbHelper.getAllCategories(userId)[0].id
        
        // Add an expense that exceeds the budget
        dbHelper.addExpense(100.0, "Expensive Item", "2024-05-21", "10:00", "11:00", categoryId, userId)
        
        val alerts = dbHelper.getAlerts(userId)
        assertTrue("Should have a budget alert", alerts.any { it.title == "Budget Exceeded" })
    }
}
