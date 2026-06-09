package com.monied.budgetapp.ui.main

import android.content.Context
import android.graphics.Color
import android.os.Bundle
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
        loadDataAndSetupChart()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun loadDataAndSetupChart() {
        if (userId == -1) return

        val calendar = Calendar.getInstance()
        val currentMonth = SimpleDateFormat("yyyy-MM", Locale.US).format(calendar.time)

        val rangeCal = Calendar.getInstance()
        rangeCal.set(Calendar.DAY_OF_MONTH, 1)
        val startDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(rangeCal.time)

        rangeCal.set(Calendar.DAY_OF_MONTH, rangeCal.getActualMaximum(Calendar.DAY_OF_MONTH))
        val endDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(rangeCal.time)

        val budgetGoal = dbHelper.getBudgetGoal(currentMonth, userId)
        val spendingData = dbHelper.getCategorySpendingForDateRange(startDate, endDate, userId)
        val totalSpent = dbHelper.getMonthlyTotalSpent(userId, currentMonth)

        val barEntries = mutableListOf<BarEntry>()
        val labels = mutableListOf<String>()

        spendingData.forEachIndexed { index, data ->
            barEntries.add(BarEntry(index.toFloat(), data.total.toFloat()))
            labels.add(data.categoryName)
        }

        setupBarChart(barEntries, labels, budgetGoal)
        updatePerformanceCard(totalSpent, budgetGoal)

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
        val chartColors = mutableListOf<Int>()
        val baseColors = intArrayOf(
            Color.parseColor("#8E24AA"), Color.parseColor("#00897B"),
            Color.parseColor("#F4511E"), Color.parseColor("#3949AB"),
            Color.parseColor("#D81B60"), Color.parseColor("#43A047"),
            Color.parseColor("#FFB300"), Color.parseColor("#039BE5")
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
        xAxis.labelRotationAngle = -45f
        xAxis.setLabelCount(labels.size)

        val leftAxis = binding.barChart.axisLeft
        leftAxis.removeAllLimitLines()
        leftAxis.axisMinimum = 0f

        budgetGoal?.let {
            if (it.maxGoal > 0) {
                val maxLine = LimitLine(it.maxGoal.toFloat(), "Budget Limit")
                maxLine.lineColor = Color.RED
                maxLine.lineWidth = 2f
                maxLine.enableDashedLine(10f, 10f, 0f)
                maxLine.labelPosition = LimitLine.LimitLabelPosition.RIGHT_TOP
                maxLine.textColor = Color.RED
                leftAxis.addLimitLine(maxLine)

                // Ensure both bars and limit line are visible
                val maxSpending = entries.maxByOrNull { it.y }?.y ?: 0f
                val yMax = maxOf(it.maxGoal.toFloat(), maxSpending)
                leftAxis.axisMaximum = (yMax * 1.25f) 
            }
        }

        binding.barChart.axisRight.isEnabled = false
        binding.barChart.description.isEnabled = false
        binding.barChart.legend.isEnabled = false
        binding.barChart.setExtraOffsets(0f, 0f, 0f, 30f) // Extra padding for rotated labels
        
        // Allow horizontal scrolling if many categories
        if (labels.size > 5) {
            binding.barChart.setVisibleXRangeMaximum(5f)
            binding.barChart.moveViewToX(0f)
        }

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

        val status: String
        val detail: String
        val color: Int

        when {
            totalSpent > budgetGoal.maxGoal -> {
                status = "Over Budget"
                detail = "Exceeded your R${String.format("%.0f", budgetGoal.maxGoal)} limit by R${String.format("%.2f", totalSpent - budgetGoal.maxGoal)}"
                color = Color.RED
            }
            totalSpent >= budgetGoal.maxGoal * 0.9 -> {
                status = "Near Limit"
                detail = "You've used ${String.format("%.0f", (totalSpent / budgetGoal.maxGoal) * 100)}% of your R${budgetGoal.maxGoal} budget."
                color = Color.parseColor("#F57C00")
            }
            else -> {
                status = "On Track"
                detail = "You are currently within your R${budgetGoal.maxGoal} budget."
                color = Color.parseColor("#388E3C")
            }
        }

        binding.tvPerformanceStatus.text = status
        binding.tvPerformanceStatus.setTextColor(color)
        binding.tvPerformanceDetail.text = detail
    }
}