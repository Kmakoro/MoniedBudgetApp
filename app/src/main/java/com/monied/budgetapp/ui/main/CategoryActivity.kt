package com.monied.budgetapp.ui.main

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.monied.budgetapp.R
import com.monied.budgetapp.adapters.CategoryAdapter
import com.monied.budgetapp.data.DatabaseHelper
import com.monied.budgetapp.models.Category

class CategoryActivity : AppCompatActivity() {

    // UI Variables
    private lateinit var rvCategories: RecyclerView
    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var btnBack: ImageButton
    private lateinit var btnAddNewCategory: Button
    private lateinit var btnHeaderAdd: ImageButton

    // Database Variable
    private lateinit var databaseHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category)

        //  Initialize the DatabaseHelper
        databaseHelper = DatabaseHelper(this)

        //  Find the views
        rvCategories = findViewById(R.id.rvCategories)
        btnBack = findViewById(R.id.btnBack)
        btnAddNewCategory = findViewById(R.id.btnAddNewCategory)
        btnHeaderAdd = findViewById(R.id.btnHeaderAdd)

        //  Setup Click Listeners
        btnBack.setOnClickListener { finish() }

        // Both the big button and the header '+' icon will open the same popup
        btnAddNewCategory.setOnClickListener { showAddCategoryDialog() }
        btnHeaderAdd.setOnClickListener { showAddCategoryDialog() }

        //  Setup RecyclerView (starts empty)
        setupRecyclerView()

        //  Fetch real data from SQLite
        loadCategories()
    }

    private fun setupRecyclerView() {
        categoryAdapter = CategoryAdapter(
            categoryList = emptyList(),
            onCategoryClick = { clickedCategory ->
                val intent = android.content.Intent(this, CategoryExpensesActivity::class.java)
                intent.putExtra("CATEGORY_ID", clickedCategory.id)
                intent.putExtra("CATEGORY_NAME", clickedCategory.name)
                startActivity(intent)
            },
            onEditClick = { clickedCategory ->
                showEditCategoryDialog(clickedCategory)
            },
            onDeleteClick = { clickedCategory ->
                showDeleteConfirmationDialog(clickedCategory)
            }
        )
        rvCategories.layoutManager = LinearLayoutManager(this)
        rvCategories.adapter = categoryAdapter
    }

    private fun showEditCategoryDialog(category: Category) {
        val input = android.widget.EditText(this)
        input.setText(category.name) // Pre-fill with the old name
        input.setPadding(50, 50, 50, 50)

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Edit Category")
            .setView(input)
            .setPositiveButton("Update") { _, _ ->
                val newName = input.text.toString().trim()
                if (newName.isNotEmpty() && newName != category.name) {
                    val success = databaseHelper.updateCategory(category.id, newName)
                    if (success) {
                        android.widget.Toast.makeText(this, "Category updated!", android.widget.Toast.LENGTH_SHORT).show()
                        loadCategories() // Refresh the list!
                    } else {
                        android.widget.Toast.makeText(this, "Failed to update", android.widget.Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showDeleteConfirmationDialog(category: Category) {

        // Check if the category has any expenses attached
        if (category.expenseCount > 0) {
            // Block the deletion and tell the user why
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Cannot Delete Category")
                .setMessage("You cannot delete '${category.name}' because it currently has ${category.expenseCount} expense(s) attached to it.\n\nPlease delete those expenses from your History first.")
                .setPositiveButton("OK", null) // Just an OK button to dismiss
                .show()
        } else {
            // If expenseCount is 0, allow them to delete it normally
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Delete Category")
                .setMessage("Are you sure you want to delete '${category.name}'?")
                .setPositiveButton("Delete") { _, _ ->
                    val success = databaseHelper.deleteCategory(category.id)
                    if (success) {
                        android.widget.Toast.makeText(this, "Category deleted!", android.widget.Toast.LENGTH_SHORT).show()
                        loadCategories() // Refresh the list!
                    } else {
                        android.widget.Toast.makeText(this, "Failed to delete", android.widget.Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }
    private fun loadCategories() {
        // Ask SQLite for all categories
        val categoriesFromDb = databaseHelper.getAllCategories()

        // Tell the adapter to refresh the screen with the new data
        categoryAdapter.updateData(categoriesFromDb)
    }

    private fun showAddCategoryDialog() {
        //  input field  for the popup
        val input = EditText(this)
        input.hint = "e.g., Shopping"
        input.setPadding(50, 50, 50, 50)

        // Build  popup Dialog
        AlertDialog.Builder(this)
            .setTitle("Add New Category")
            .setView(input)
            .setPositiveButton("Save") { _, _ ->
                // When they click "Save", get the text they typed
                val categoryName = input.text.toString().trim()

                if (categoryName.isNotEmpty()) {
                    val result = databaseHelper.addCategory(categoryName)

                    if (result != -1L) {
                        // Success! Show a little toast message and refresh the list
                        Toast.makeText(this, "Category Saved!", Toast.LENGTH_SHORT).show()
                        loadCategories()
                    } else {
                        // -1 means it failed (usually because of the UNIQUE rule we set in SQLite)
                        Toast.makeText(this, "Category already exists!", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null) // Do nothing if they cancel
            .show()
    }
}