package com.monied.budgetapp.ui.main

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category_expenses)

        databaseHelper = DatabaseHelper(this)

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
        loadExpenses(categoryId)
    }

    private fun loadExpenses(categoryId: Int) {
        // Ask the database for expenses matching this specific ID
        val expenses = databaseHelper.getExpensesByCategory(categoryId)

        if (expenses.isEmpty()) {
            // Show the "No expenses found" text and hide the list
            tvEmptyState.visibility = View.VISIBLE
            rvCategoryExpenses.visibility = View.GONE
        } else {
            // Hide the text, show the list
            tvEmptyState.visibility = View.GONE
            rvCategoryExpenses.visibility = View.VISIBLE

            // --- USE YOUR CUSTOM ADAPTER HERE ---
            val adapter = ExpenseAdapter(
                expenses = expenses,
                onItemClick = { clickedExpense ->
                    // For now, let's just show a Toast when they click the card
                    android.widget.Toast.makeText(this, "Clicked: ${clickedExpense.description}", android.widget.Toast.LENGTH_SHORT).show()
                },
                onDeleteClick = { clickedExpense ->
                    // If you uncomment the delete button in your adapter later,
                    // you can add the database delete logic here!
                }
            )

            rvCategoryExpenses.layoutManager = LinearLayoutManager(this)
            rvCategoryExpenses.adapter = adapter
        }
    }
}