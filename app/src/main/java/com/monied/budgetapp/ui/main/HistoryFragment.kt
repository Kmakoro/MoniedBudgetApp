package com.monied.budgetapp.ui.main

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.monied.budgetapp.R
import com.monied.budgetapp.adapters.ExpenseAdapter
import com.monied.budgetapp.data.DatabaseHelper
import com.monied.budgetapp.dialog.DateRangePickerDialog
import com.monied.budgetapp.models.Expense
import java.text.SimpleDateFormat
import java.util.*

class HistoryFragment : Fragment() {

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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dbHelper = DatabaseHelper(requireContext())
        initViews(view)
        setupRecyclerView()

        // Load default period (last 30 days)
        loadDefaultPeriod()

        btnSelectDate.setOnClickListener {
            showDateRangePicker()
        }
    }

    private fun initViews(view: View) {
        recyclerView = view.findViewById(R.id.recyclerViewExpenses)
        btnSelectDate = view.findViewById(R.id.btnSelectDate)
        tvDateRange = view.findViewById(R.id.tvDateRange)
        tvTotalAmount = view.findViewById(R.id.tvTotalAmount)
        tvTotalCount = view.findViewById(R.id.tvTotalCount)
        progressBar = view.findViewById(R.id.progressBar)
        tvNoData = view.findViewById(R.id.tvNoData)
    }

    private fun setupRecyclerView() {
        adapter = ExpenseAdapter(
            expenses = expensesList,
            onItemClick = { expense -> showExpenseDetails(expense) },
            onDeleteClick = { expense -> confirmDeleteExpense(expense) }
        )
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
    }

    private fun loadDefaultPeriod() {
        val calendar = Calendar.getInstance()
        val endDate = String.format(Locale.getDefault(), "%04d-%02d-%02d",
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH) + 1,
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        calendar.add(Calendar.MONTH, -1)
        val startDate = String.format(Locale.getDefault(), "%04d-%02d-%02d",
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH) + 1,
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        loadExpenses(startDate, endDate)
    }

    private fun showDateRangePicker() {
        val dialog = DateRangePickerDialog.newInstance { startDate, endDate ->
            loadExpenses(startDate, endDate)
        }
        dialog.show(parentFragmentManager, "DateRangePicker")
    }

    private fun loadExpenses(startDate: String, endDate: String) {
        val context = context ?: return
        val prefs = context.getSharedPreferences("MoniedPrefs", Context.MODE_PRIVATE)
        val userId = prefs.getInt("userId", -1)

        if (userId == -1) {
            Toast.makeText(context, "Please login again", Toast.LENGTH_SHORT).show()
            return
        }

        currentStartDate = startDate
        currentEndDate = endDate

        showLoading(true)

        // Update date range display
        tvDateRange.text = formatDateRange(startDate, endDate)

        // Load expenses from database
        val expenses = dbHelper.getExpensesByDateRange(startDate, endDate, userId)
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

        tvTotalCount.text = String.format(Locale.getDefault(), "Total Expenses: %d", totalCount)
        tvTotalAmount.text = String.format(Locale.getDefault(), "Total Amount: R%.2f", totalAmount)
    }

    private fun showExpenseDetails(expense: Expense) {
        val context = context ?: return
        val dialog = Dialog(context)
        dialog.setContentView(R.layout.dialog_expense_details)

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        val tvAmount = dialog.findViewById<TextView>(R.id.tvDetailAmount)
        val tvDescription = dialog.findViewById<TextView>(R.id.tvDetailDescription)
        val tvCategory = dialog.findViewById<TextView>(R.id.tvDetailCategory)
        val tvDate = dialog.findViewById<TextView>(R.id.tvDetailDate)
        val tvTime = dialog.findViewById<TextView>(R.id.tvDetailTime)
        val tvDuration = dialog.findViewById<TextView>(R.id.tvDetailDuration)
        val ivPhoto = dialog.findViewById<ImageView>(R.id.ivDetailPhoto)
        val btnDelete = dialog.findViewById<View>(R.id.btnDetailDelete)
        val btnClose = dialog.findViewById<View>(R.id.btnDetailClose)
        val emptyPhotoView = dialog.findViewById<View>(R.id.tvNoImage)

        tvAmount.text = expense.formattedAmount
        tvDescription.text = expense.description
        tvCategory.text = expense.categoryName
        tvDate.text = formatDisplayDate(expense.date)
        tvTime.text = String.format(Locale.getDefault(), "%s - %s", expense.startTime, expense.endTime)
        tvDuration.text = calculateDuration(expense.startTime, expense.endTime)

        if (!expense.photoUri.isNullOrEmpty()) {
            Glide.with(this)
                .load(expense.photoUri)
                .fitCenter()
                .error(R.drawable.ic_image_error)
                .into(ivPhoto)
            ivPhoto.visibility = View.VISIBLE
            emptyPhotoView?.visibility = View.GONE

            ivPhoto.setOnClickListener {
                showFullscreenImage(expense.photoUri)
            }
        } else {
            ivPhoto.visibility = View.GONE
            emptyPhotoView?.visibility = View.VISIBLE
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
        val context = context ?: return
        if (imageUri.isNullOrEmpty()) return

        val dialog = Dialog(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
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

        ivFullscreen.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun confirmDeleteExpense(expense: Expense) {
        val context = context ?: return
        AlertDialog.Builder(context)
            .setTitle("Delete Expense")
            .setMessage(String.format(Locale.getDefault(), "Delete %s - %s?", expense.description, expense.formattedAmount))
            .setPositiveButton("Delete") { _, _ ->
                dbHelper.deleteExpense(expense.id)
                loadExpenses(currentStartDate, currentEndDate)
                Toast.makeText(context, "Expense deleted", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun formatDateRange(startDate: String, endDate: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val outputFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            val start = inputFormat.parse(startDate)
            val end = inputFormat.parse(endDate)
            if (start != null && end != null) {
                String.format(Locale.getDefault(), "%s - %s", outputFormat.format(start), outputFormat.format(end))
            } else {
                String.format(Locale.getDefault(), "%s to %s", startDate, endDate)
            }
        } catch (e: Exception) {
            String.format(Locale.getDefault(), "%s to %s", startDate, endDate)
        }
    }

    private fun formatDisplayDate(dateString: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val outputFormat = SimpleDateFormat("EEEE, MMM dd, yyyy", Locale.getDefault())
            val date = inputFormat.parse(dateString)
            if (date != null) outputFormat.format(date) else dateString
        } catch (e: Exception) {
            dateString
        }
    }

    private fun calculateDuration(startTime: String, endTime: String): String {
        return try {
            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            val start = timeFormat.parse(startTime)
            val end = timeFormat.parse(endTime)
            if (start != null && end != null) {
                val durationMillis = end.time - start.time
                val hours = durationMillis / (1000 * 60 * 60)
                val minutes = (durationMillis % (1000 * 60 * 60)) / (1000 * 60)
                when {
                    hours > 0 && minutes > 0 -> String.format(Locale.getDefault(), "%dh %dm", hours, minutes)
                    hours > 0 -> String.format(Locale.getDefault(), "%dh", hours)
                    minutes > 0 -> String.format(Locale.getDefault(), "%dm", minutes)
                    else -> "0m"
                }
            } else ""
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