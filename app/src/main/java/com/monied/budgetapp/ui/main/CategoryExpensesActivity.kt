package com.monied.budgetapp.ui.main

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.monied.budgetapp.R
import com.monied.budgetapp.adapters.ExpenseAdapter
import com.monied.budgetapp.data.DatabaseHelper

class CategoryExpensesActivity : AppCompatActivity() {

    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var rvCategoryExpenses: RecyclerView
    private lateinit var tvCategoryTitle: TextView
    private lateinit var btnBack: ImageButton
    private lateinit var tvEmptyState: TextView
    private var userId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category_expenses)

        databaseHelper = DatabaseHelper(this)

        val prefs = getSharedPreferences("MoniedPrefs", Context.MODE_PRIVATE)
        userId = prefs.getInt("userId", -1)

        // 1. Find views
        rvCategoryExpenses = findViewById(R.id.rvCategoryExpenses)
        tvCategoryTitle = findViewById(R.id.tvCategoryTitle)
        btnBack = findViewById(R.id.btnBack)
        tvEmptyState = findViewById(R.id.tvEmptyState)

        // 2. Get the data passed from the Intent
        val categoryId = intent.getIntExtra("CATEGORY_ID", -1)
        val categoryName = intent.getStringExtra("CATEGORY_NAME") ?: "Unknown Category"

        // 3. Update UI Header
        tvCategoryTitle.text = "$categoryName Expenses"
        btnBack.setOnClickListener { finish() }

        // 4. Load the data
        if (userId != -1) {
            loadExpenses(categoryId)
        } else {
            Toast.makeText(this, "User session error", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun loadExpenses(categoryId: Int) {
        // Ask the database for expenses matching this specific ID and user
        val expenses = databaseHelper.getExpensesByCategory(categoryId, userId)

        if (expenses.isEmpty()) {
            // Show the "No expenses found" text and hide the list
            tvEmptyState.visibility = View.VISIBLE
            rvCategoryExpenses.visibility = View.GONE
        } else {
            // Hide the text, show the list
            tvEmptyState.visibility = View.GONE
            rvCategoryExpenses.visibility = View.VISIBLE

            val adapter = ExpenseAdapter(
                expenses = expenses,
                onItemClick = { clickedExpense ->
                    // Navigation to details or show a bottom sheet could be added here
                    Toast.makeText(this, "Details for: ${clickedExpense.description}", Toast.LENGTH_SHORT).show()
                },
                onDeleteClick = { clickedExpense ->
                    // Add delete logic here
                    databaseHelper.deleteExpense(clickedExpense.id)
                    loadExpenses(categoryId)
                    Toast.makeText(this, "Expense deleted", Toast.LENGTH_SHORT).show()
                }
            )

            rvCategoryExpenses.layoutManager = LinearLayoutManager(this)
            rvCategoryExpenses.adapter = adapter
        }
    }
}