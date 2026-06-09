package com.monied.budgetapp.ui.main

import android.content.Context
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

    private lateinit var rvCategories: RecyclerView
    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var btnBack: ImageButton
    private lateinit var btnAddNewCategory: Button
    private lateinit var btnHeaderAdd: ImageButton

    private lateinit var databaseHelper: DatabaseHelper
    private var userId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category)

        databaseHelper = DatabaseHelper(this)

        val prefs = getSharedPreferences("MoniedPrefs", Context.MODE_PRIVATE)
        userId = prefs.getInt("userId", -1)

        if (userId == -1) {
            Toast.makeText(this, "Session expired", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        rvCategories = findViewById(R.id.rvCategories)
        btnBack = findViewById(R.id.btnBack)
        btnAddNewCategory = findViewById(R.id.btnAddNewCategory)
        btnHeaderAdd = findViewById(R.id.btnHeaderAdd)

        btnBack.setOnClickListener { finish() }
        btnAddNewCategory.setOnClickListener { showAddCategoryDialog() }
        btnHeaderAdd.setOnClickListener { showAddCategoryDialog() }

        setupRecyclerView()
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
        val input = EditText(this)
        input.setText(category.name)
        input.setPadding(50, 50, 50, 50)

        AlertDialog.Builder(this)
            .setTitle("Edit Category")
            .setView(input)
            .setPositiveButton("Update") { _, _ ->
                val newName = input.text.toString().trim()
                if (newName.isNotEmpty() && newName != category.name) {
                    val success = databaseHelper.updateCategory(category.id, newName)
                    if (success) {
                        Toast.makeText(this, "Category updated!", Toast.LENGTH_SHORT).show()
                        loadCategories()
                    } else {
                        Toast.makeText(this, "Failed to update", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showDeleteConfirmationDialog(category: Category) {
        if (category.expenseCount > 0) {
            AlertDialog.Builder(this)
                .setTitle("Cannot Delete Category")
                .setMessage("You cannot delete '${category.name}' because it currently has ${category.expenseCount} expense(s) attached to it.")
                .setPositiveButton("OK", null)
                .show()
        } else {
            AlertDialog.Builder(this)
                .setTitle("Delete Category")
                .setMessage("Are you sure you want to delete '${category.name}'?")
                .setPositiveButton("Delete") { _, _ ->
                    val success = databaseHelper.deleteCategory(category.id)
                    if (success) {
                        Toast.makeText(this, "Category deleted!", Toast.LENGTH_SHORT).show()
                        loadCategories()
                    } else {
                        Toast.makeText(this, "Failed to delete", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun loadCategories() {
        val categoriesFromDb = databaseHelper.getAllCategories(userId)
        categoryAdapter.updateData(categoriesFromDb)
    }

    private fun showAddCategoryDialog() {
        val input = EditText(this)
        input.hint = "e.g., Shopping"
        input.setPadding(50, 50, 50, 50)

        AlertDialog.Builder(this)
            .setTitle("Add New Category")
            .setView(input)
            .setPositiveButton("Save") { _, _ ->
                val categoryName = input.text.toString().trim()
                if (categoryName.isNotEmpty()) {
                    val result = databaseHelper.addCategory(categoryName, userId)
                    if (result != -1L) {
                        Toast.makeText(this, "Category Saved!", Toast.LENGTH_SHORT).show()
                        loadCategories()
                    } else {
                        Toast.makeText(this, "Category already exists!", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
