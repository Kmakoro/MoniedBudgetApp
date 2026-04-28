package com.monied.budgetapp.ui.main



import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.monied.budgetapp.R
import com.monied.budgetapp.adapters.ExpenseAdapter
import com.monied.budgetapp.data.DatabaseHelper
//import com.monied.budgetapp.database.ExpenseSummary
import com.monied.budgetapp.dialog.DateRangePickerDialog
import com.monied.budgetapp.models.Expense


import android.app.Dialog

import com.bumptech.glide.Glide


class ViewExpensesActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ExpenseAdapter
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var btnSelectDate: Button
    private lateinit var tvDateRange: TextView
    private lateinit var tvTotalAmount: TextView
    private lateinit var tvTotalCount: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvNoData: TextView

    private val expensesList = mutableListOf<Expense>()
    private var currentStartDate = ""
    private var currentEndDate = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_expenses)

        dbHelper = DatabaseHelper(this)
        initViews()
        setupRecyclerView()

        // Load default period (last 30 days)
        loadDefaultPeriod()

        btnSelectDate.setOnClickListener {
            showDateRangePicker()
        }
    }

    private fun initViews() {
        recyclerView = findViewById(R.id.recyclerViewExpenses)
        btnSelectDate = findViewById(R.id.btnSelectDate)
        tvDateRange = findViewById(R.id.tvDateRange)
        tvTotalAmount = findViewById(R.id.tvTotalAmount)
        tvTotalCount = findViewById(R.id.tvTotalCount)
        progressBar = findViewById(R.id.progressBar)
        tvNoData = findViewById(R.id.tvNoData)
    }

    private fun setupRecyclerView() {
        adapter = ExpenseAdapter(
            expenses = expensesList,
            onItemClick = { expense -> showExpenseDetails(expense) },
            onDeleteClick = { expense -> confirmDeleteExpense(expense) }
        )
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun loadDefaultPeriod() {
        val calendar = java.util.Calendar.getInstance()
        val endDate = String.format("%04d-%02d-%02d",
            calendar.get(java.util.Calendar.YEAR),
            calendar.get(java.util.Calendar.MONTH) + 1,
            calendar.get(java.util.Calendar.DAY_OF_MONTH)
        )

        calendar.add(java.util.Calendar.MONTH, -1)
        val startDate = String.format("%04d-%02d-%02d",
            calendar.get(java.util.Calendar.YEAR),
            calendar.get(java.util.Calendar.MONTH) + 1,
            calendar.get(java.util.Calendar.DAY_OF_MONTH)
        )

        loadExpenses(startDate, endDate)
    }

    private fun showDateRangePicker() {
        val dialog = DateRangePickerDialog.newInstance { startDate, endDate ->
            loadExpenses(startDate, endDate)
        }
        dialog.show(supportFragmentManager, "DateRangePicker")
    }

    private fun loadExpenses(startDate: String, endDate: String) {
        currentStartDate = startDate
        currentEndDate = endDate

        showLoading(true)

        // Update date range display
        tvDateRange.text = formatDateRange(startDate, endDate)

        // Load expenses from database
        val expenses = dbHelper.getExpensesByDateRange(startDate, endDate)
        expensesList.clear()
        expensesList.addAll(expenses)
        adapter.updateData(expensesList)

        // Update summary
        updateSummary(expenses)

        showLoading(false)

        // Show/hide no data message
        tvNoData.visibility = if (expensesList.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun updateSummary(expenses: List<Expense>) {
        val totalCount = expenses.size
        val totalAmount = expenses.sumOf { it.amount }

        tvTotalCount.text = "Total Expenses: $totalCount"
        tvTotalAmount.text = String.format("Total Amount: R%.2f", totalAmount)
    }

    private fun showExpenseDetails(expense: Expense) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_expense_details)

        // Initialize views
        val tvAmount = dialog.findViewById<TextView>(R.id.tvDetailAmount)
        val tvDescription = dialog.findViewById<TextView>(R.id.tvDetailDescription)
        val tvCategory = dialog.findViewById<TextView>(R.id.tvDetailCategory)
        val tvDate = dialog.findViewById<TextView>(R.id.tvDetailDate)
        val tvTime = dialog.findViewById<TextView>(R.id.tvDetailTime)
        val tvDuration = dialog.findViewById<TextView>(R.id.tvDetailDuration)
        val ivPhoto = dialog.findViewById<ImageView>(R.id.ivDetailPhoto)
        val btnDelete = dialog.findViewById<Button>(R.id.btnDetailDelete)
        val btnClose = dialog.findViewById<Button>(R.id.btnDetailClose)

        // Set data
        tvAmount.text = expense.formattedAmount
        tvDescription.text = expense.description
        tvCategory.text = expense.categoryName
        tvDate.text = formatDisplayDate(expense.date)
        tvTime.text = "${expense.startTime} - ${expense.endTime}"
        tvDuration.text = calculateDuration(expense.startTime, expense.endTime)

        // Load image if exists
        if (!expense.photoUri.isNullOrEmpty()) {
            Glide.with(this)
                .load(expense.photoUri)
                .fitCenter()
                .error(R.drawable.ic_image_error)
                .into(ivPhoto)
            ivPhoto.visibility = View.VISIBLE
            // Make image clickable to view fullscreen
            ivPhoto.setOnClickListener {
                showFullscreenImage(expense.photoUri)
            }
        } else {
            ivPhoto.visibility = View.GONE
            // Show placeholder
            val tvNoImage = dialog.findViewById<TextView>(R.id.tvNoImage)
            tvNoImage.visibility = View.VISIBLE
        }

        btnDelete.setOnClickListener {
            dialog.dismiss()
            confirmDeleteExpense(expense)
        }

        btnClose.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showFullscreenImage(imageUri: String?) {
        if (imageUri.isNullOrEmpty()) return

        val dialog = Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        dialog.setContentView(R.layout.dialog_fullscreen_image)

        val ivFullscreen = dialog.findViewById<ImageView>(R.id.ivFullscreenImage)
        val btnClose = dialog.findViewById<Button>(R.id.btnCloseFullscreen)

        Glide.with(this)
            .load(imageUri)
            .fitCenter()
            .into(ivFullscreen)

        btnClose.setOnClickListener {
            dialog.dismiss()
        }

        // Close on click
        ivFullscreen.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun confirmDeleteExpense(expense: Expense) {
        AlertDialog.Builder(this)
            .setTitle("Delete Expense")
            .setMessage("Delete ${expense.description} - ${expense.formattedAmount}?")
            .setPositiveButton("Delete") { _, _ ->
                dbHelper.deleteExpense(expense.id)
                loadExpenses(currentStartDate, currentEndDate)
                Toast.makeText(this, "Expense deleted", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun formatDateRange(startDate: String, endDate: String): String {
        return try {
            val inputFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            val outputFormat = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
            val start = inputFormat.parse(startDate)
            val end = inputFormat.parse(endDate)
            "${outputFormat.format(start)} - ${outputFormat.format(end)}"
        } catch (e: Exception) {
            "$startDate to $endDate"
        }
    }

    private fun formatDisplayDate(dateString: String): String {
        return try {
            val inputFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            val outputFormat = java.text.SimpleDateFormat("EEEE, MMM dd, yyyy", java.util.Locale.getDefault())
            val date = inputFormat.parse(dateString)
            outputFormat.format(date ?: java.util.Date())
        } catch (e: Exception) {
            dateString
        }
    }

    private fun calculateDuration(startTime: String, endTime: String): String {
        return try {
            val timeFormat = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
            val start = timeFormat.parse(startTime)
            val end = timeFormat.parse(endTime)
            val durationMillis = end.time - start.time
            val hours = durationMillis / (1000 * 60 * 60)
            val minutes = (durationMillis % (1000 * 60 * 60)) / (1000 * 60)
            when {
                hours > 0 && minutes > 0 -> "${hours}h ${minutes}m"
                hours > 0 -> "${hours}h"
                minutes > 0 -> "${minutes}m"
                else -> "0m"
            }
        } catch (e: Exception) {
            ""
        }
    }

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
        recyclerView.visibility = if (show) View.GONE else View.VISIBLE
        tvNoData.visibility = View.GONE
    }
}