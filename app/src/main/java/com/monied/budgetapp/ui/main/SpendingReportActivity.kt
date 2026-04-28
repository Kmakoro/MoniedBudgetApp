package com.monied.budgetapp.ui.main

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.monied.budgetapp.R
import com.monied.budgetapp.adapters.ReportAdapter
import com.monied.budgetapp.data.DatabaseHelper
import java.util.Calendar
import java.util.Locale

class SpendingReportActivity : AppCompatActivity() {

    private lateinit var btnStartDate: Button
    private lateinit var btnEndDate: Button
    private lateinit var btnGenerateReport: Button
    private lateinit var rvReportList: RecyclerView

    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var reportAdapter: ReportAdapter

    // Variables to hold the selected dates in yyyy-MM-dd format (Crucial for SQLite!)
    private var selectedStartDate: String = ""
    private var selectedEndDate: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_spending_report)

        databaseHelper = DatabaseHelper(this)

        btnStartDate = findViewById(R.id.btnStartDate)
        btnEndDate = findViewById(R.id.btnEndDate)
        btnGenerateReport = findViewById(R.id.btnGenerateReport)
        rvReportList = findViewById(R.id.rvReportList)

        // Setup the Recycler View empty initially
        reportAdapter = ReportAdapter(emptyList())
        rvReportList.layoutManager = LinearLayoutManager(this)
        rvReportList.adapter = reportAdapter

        // Click Listeners
        btnStartDate.setOnClickListener { showDatePicker { date ->
            selectedStartDate = date
            btnStartDate.text = date
        }}

        btnEndDate.setOnClickListener { showDatePicker { date ->
            selectedEndDate = date
            btnEndDate.text = date
        }}

        btnGenerateReport.setOnClickListener {
            if (selectedStartDate.isEmpty() || selectedEndDate.isEmpty()) {
                Toast.makeText(this, "Please select both dates!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Ask your pre-existing Database function for the data!
            val reportData = databaseHelper.getCategorySpendingForDateRange(selectedStartDate, selectedEndDate)

            if (reportData.isEmpty()) {
                Toast.makeText(this, "No expenses found in this date range.", Toast.LENGTH_SHORT).show()
            }

            // Update the screen
            reportAdapter.updateData(reportData)
        }
    }

    // A reusable DatePicker function that returns the formatted string
    private fun showDatePicker(onDateSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            // SQLite needs dates exactly like 2026-04-27 to compare them properly
            val formattedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
            onDateSelected(formattedDate)
        }, year, month, day).show()
    }
}