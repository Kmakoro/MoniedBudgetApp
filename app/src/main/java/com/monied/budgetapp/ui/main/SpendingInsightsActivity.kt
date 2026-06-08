package com.monied.budgetapp.ui.main

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.monied.budgetapp.R
import com.monied.budgetapp.data.DatabaseHelper
import com.monied.budgetapp.data.BudgetGoalData
import com.monied.budgetapp.databinding.ActivitySpendingInsightsBinding
import com.monied.budgetapp.ui.dialog.BadgesDialogFragment
import com.monied.budgetapp.utils.GamificationManager
import java.text.SimpleDateFormat
import java.util.*

class SpendingInsightsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySpendingInsightsBinding
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var gamificationManager: GamificationManager
    private var userId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySpendingInsightsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = DatabaseHelper(this)
        gamificationManager = GamificationManager(this)

        val prefs = getSharedPreferences("MoniedPrefs", Context.MODE_PRIVATE)
        userId = prefs.getInt("userId", -1)

        setupToolbar()

        binding.btnViewBadges.setOnClickListener {
            BadgesDialogFragment().show(supportFragmentManager, "BadgesDialog")
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh data every time the user views the screen
        loadDataAndSetupChart()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun loadDataAndSetupChart() {
        if (userId == -1) return

        val calendar = Calendar.getInstance()
        // Use Locale.US for database keys to avoid inconsistencies
        val currentMonth = SimpleDateFormat("yyyy-MM", Locale.US).format(calendar.time)

        // Calculate date range for current month for the category graph
        val rangeCal = Calendar.getInstance()
        rangeCal.set(Calendar.DAY_OF_MONTH, 1)
        val startDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(rangeCal.time)

        rangeCal.set(Calendar.DAY_OF_MONTH, rangeCal.getActualMaximum(Calendar.DAY_OF_MONTH))
        val endDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(rangeCal.time)

        // Fetch user-specific data from SQLite
        val budgetGoal = dbHelper.getBudgetGoal(currentMonth, userId)
        val spendingData = dbHelper.getCategorySpendingForDateRange(startDate, endDate, userId)
        val totalSpent = dbHelper.getMonthlyTotalSpent(userId, currentMonth)

        val barEntries = mutableListOf<BarEntry>()
        val labels = mutableListOf<String>()

        // The graph will be blank if spendingData is empty (no expenses yet)
        spendingData.forEachIndexed { index, data ->
            barEntries.add(BarEntry(index.toFloat(), data.total.toFloat()))
            labels.add(data.categoryName)
        }

        setupBarChart(barEntries, labels, budgetGoal)
        updatePerformanceCard(totalSpent, budgetGoal)

        // Award badges based on real budget performance
        budgetGoal?.let {
            gamificationManager.checkAndAwardBadges(userId, totalSpent, it.maxGoal)
        }
    }

    private fun setupBarChart(entries: List<BarEntry>, labels: List<String>, budgetGoal: BudgetGoalData?) {
        if (entries.isEmpty()) {
            binding.barChart.clear()
            binding.barChart.setNoDataText("No spending recorded for ${SimpleDateFormat("MMMM", Locale.getDefault()).format(Date())}.")
            binding.barChart.setNoDataTextColor(Color.GRAY)
            binding.barChart.invalidate()
            return
        }

        val dataSet = BarDataSet(entries, "Spending by Category")
        // Use defined colors or fallback
        val chartColors = mutableListOf<Int>()
        val baseColors = intArrayOf(
            Color.parseColor("#8E24AA"), // Purple
            Color.parseColor("#00897B"), // Teal
            Color.parseColor("#F4511E"), // Deep Orange
            Color.parseColor("#3949AB"), // Indigo
            Color.parseColor("#D81B60"), // Pink
            Color.parseColor("#43A047")  // Green
        )
        for (i in 0 until entries.size) {
            chartColors.add(baseColors[i % baseColors.size])
        }
        dataSet.colors = chartColors
        dataSet.valueTextColor = Color.DKGRAY
        dataSet.valueTextSize = 10f

        val barData = BarData(dataSet)
        barData.barWidth = 0.6f
        binding.barChart.data = barData

        val xAxis = binding.barChart.xAxis
        xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 1f
        xAxis.setDrawGridLines(false)
        xAxis.labelRotationAngle = -25f
        xAxis.labelCount = labels.size

        val leftAxis = binding.barChart.axisLeft
        leftAxis.removeAllLimitLines()
        leftAxis.axisMinimum = 0f

        // Add limit lines for visual budget tracking on the graph
        budgetGoal?.let {
            if (it.maxGoal > 0) {
                val maxLine = LimitLine(it.maxGoal.toFloat(), "Max Budget")
                maxLine.lineColor = Color.RED
                maxLine.lineWidth = 2f
                maxLine.enableDashedLine(10f, 10f, 0f)
                maxLine.labelPosition = LimitLine.LimitLabelPosition.RIGHT_TOP
                maxLine.textColor = Color.RED
                leftAxis.addLimitLine(maxLine)

                // Ensure the graph scale can show the budget line
                if (it.maxGoal > leftAxis.axisMaximum) {
                    leftAxis.axisMaximum = (it.maxGoal * 1.1).toFloat()
                }
            }
        }

        binding.barChart.axisRight.isEnabled = false
        binding.barChart.description.isEnabled = false
        binding.barChart.legend.isEnabled = false
        binding.barChart.animateY(1000)
        binding.barChart.invalidate()
    }

    private fun updatePerformanceCard(totalSpent: Double, budgetGoal: BudgetGoalData?) {
        if (budgetGoal == null) {
            binding.tvPerformanceStatus.text = "No Budget Set"
            binding.tvPerformanceDetail.text = "Set your monthly budget on the home dashboard."
            binding.tvPerformanceStatus.setTextColor(Color.GRAY)
            return
        }

        if (totalSpent == 0.0) {
            binding.tvPerformanceStatus.text = "No Spending Yet"
            binding.tvPerformanceDetail.text = "Your budget is R${String.format("%.0f", budgetGoal.maxGoal)}. Start logging expenses to track performance."
            binding.tvPerformanceStatus.setTextColor(ContextCompat.getColor(this, R.color.purple_500))
            return
        }

        val status: String
        val detail: String
        val color: Int

        when {
            totalSpent > budgetGoal.maxGoal -> {
                status = "Over Budget"
                detail = "Exceeded your limit by R${String.format("%.2f", totalSpent - budgetGoal.maxGoal)}"
                color = Color.RED
            }
            totalSpent >= budgetGoal.maxGoal * 0.9 -> {
                status = "Near Limit"
                detail = "You've used ${String.format("%.0f", (totalSpent / budgetGoal.maxGoal) * 100)}% of your R${budgetGoal.maxGoal} budget."
                color = Color.parseColor("#F57C00") // Orange
            }
            else -> {
                status = "On Track"
                detail = "You are currently within your R${budgetGoal.maxGoal} budget boundaries."
                color = Color.parseColor("#388E3C") // Dark Green
            }
        }

        binding.tvPerformanceStatus.text = status
        binding.tvPerformanceStatus.setTextColor(color)
        binding.tvPerformanceDetail.text = detail
    }
}
