package com.monied.budgetapp.ui.main

import android.app.DatePickerDialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.android.material.textfield.TextInputEditText
import com.monied.budgetapp.R
import com.monied.budgetapp.data.DatabaseHelper
import com.monied.budgetapp.data.CategorySpending
import com.monied.budgetapp.data.WeeklySpending
import java.text.SimpleDateFormat
import java.util.*

class AnalyticsFragment : Fragment() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var categoryAdapter: CategorySpendingAdapter
    private lateinit var weeklyAdapter: WeeklySpendingAdapter
    private lateinit var pieChart: PieChart
    private lateinit var barChart: BarChart

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_analytics, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dbHelper = DatabaseHelper(requireContext())

        pieChart = view.findViewById(R.id.pieChartCategories)
        barChart = view.findViewById(R.id.barChartWeekly)

        val rvCategories = view.findViewById<RecyclerView>(R.id.rvCategories)
        rvCategories.layoutManager = LinearLayoutManager(requireContext())
        categoryAdapter = CategorySpendingAdapter(mutableListOf())
        rvCategories.adapter = categoryAdapter

        val rvWeekly = view.findViewById<RecyclerView>(R.id.rvWeekly)
        rvWeekly.layoutManager = LinearLayoutManager(requireContext())
        weeklyAdapter = WeeklySpendingAdapter(mutableListOf())
        rvWeekly.adapter = weeklyAdapter

        val etStart = view.findViewById<TextInputEditText>(R.id.etAnalyticsStart)
        val etEnd = view.findViewById<TextInputEditText>(R.id.etAnalyticsEnd)
        val btnGo = view.findViewById<Button>(R.id.btnAnalyticsGo)
        val tvTotal = view.findViewById<TextView>(R.id.tvAnalyticsTotal)
        val tvInsights = view.findViewById<TextView>(R.id.tvInsights)

        etStart.setOnClickListener { showDatePicker(etStart) }
        etEnd.setOnClickListener { showDatePicker(etEnd) }

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val calendar = Calendar.getInstance()
        val endDateStr = dateFormat.format(calendar.time)
        calendar.add(Calendar.DAY_OF_YEAR, -30)
        val startDateStr = dateFormat.format(calendar.time)

        etStart.setText(startDateStr)
        etEnd.setText(endDateStr)

        btnGo.setOnClickListener {
            loadAnalytics(etStart.text.toString(), etEnd.text.toString(), tvTotal, tvInsights)
        }
        loadAnalytics(etStart.text.toString(), etEnd.text.toString(), tvTotal, tvInsights)
    }

    private fun showDatePicker(editText: TextInputEditText) {
        val c = Calendar.getInstance()
        DatePickerDialog(requireContext(), { _, year, month, day ->
            val selectedDate = Calendar.getInstance()
            selectedDate.set(year, month, day)
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            editText.setText(dateFormat.format(selectedDate.time))
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun loadAnalytics(start: String, end: String, totalView: TextView, insightsView: TextView) {
        val prefs = requireContext().getSharedPreferences("MoniedPrefs", Context.MODE_PRIVATE)
        val currentUser = prefs.getString("loggedInUser", "cyril") ?: "cyril"
        val user = dbHelper.getUser(currentUser) ?: return
        val userId = user.id

        val categorySpending = dbHelper.getCategorySpendingForDateRange(start, end, userId)
        val weeklySpending = dbHelper.getWeeklyBreakdown(start, end, userId)
        val total = categorySpending.sumOf { it.total }
        totalView.text = String.format(Locale.getDefault(), "R %.2f", total)

        val items = categorySpending.map { cs ->
            val percent = if (total > 0) (cs.total / total) * 100 else 0.0
            Pair(cs, percent)
        }
        categoryAdapter.updateData(items)
        weeklyAdapter.updateData(weeklySpending)

        setupPieChart(categorySpending)
        setupBarChart(weeklySpending)
        generateInsights(categorySpending, weeklySpending, total, insightsView, userId)
    }

    private fun setupPieChart(data: List<CategorySpending>) {
        val entries = data.map { PieEntry(it.total.toFloat(), it.categoryName) }
        val dataSet = PieDataSet(entries, "")
        dataSet.colors = ColorTemplate.MATERIAL_COLORS.toList()
        dataSet.valueTextColor = Color.WHITE
        dataSet.valueTextSize = 14f

        val pieData = PieData(dataSet)
        pieChart.data = pieData
        pieChart.description.isEnabled = false
        pieChart.isDrawHoleEnabled = true
        pieChart.setHoleColor(Color.TRANSPARENT)
        pieChart.legend.isEnabled = false
        pieChart.animateY(1000)
        pieChart.invalidate()
    }

    private fun setupBarChart(data: List<WeeklySpending>) {
        val entries = data.mapIndexed { index, weeklySpending -> BarEntry(index.toFloat(), weeklySpending.total.toFloat()) }
        val dataSet = BarDataSet(entries, "Weekly Spending")
        dataSet.color = ContextCompat.getColor(requireContext(), R.color.emerald_600)

        val barData = BarData(dataSet)
        barChart.data = barData

        val xAxis = barChart.xAxis
        xAxis.valueFormatter = IndexAxisValueFormatter(data.map { it.weekLabel })
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 1f
        xAxis.setDrawGridLines(false)

        barChart.axisRight.isEnabled = false
        barChart.description.isEnabled = false
        barChart.animateY(1000)
        barChart.invalidate()
    }

    private fun generateInsights(categorySpending: List<CategorySpending>, weeklySpending: List<WeeklySpending>, total: Double, insightsView: TextView, userId: Int) {
        val insights = StringBuilder()
        val currentMonth = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())
        val budgetGoal = dbHelper.getBudgetGoal(currentMonth, userId)

        if (categorySpending.isEmpty()) {
            insightsView.text = "Add some expenses to see your performance insights!"
            return
        }

        // Performance Insight 1: Top Category
        val topCategory = categorySpending.maxByOrNull { it.total }
        topCategory?.let {
            val percentage = (it.total / total) * 100
            insights.append("• <b>Performance Note:</b> You spend ${String.format(Locale.getDefault(), "%.1f", percentage)}% of your money on <b>${it.categoryName}</b>. Consider if this aligns with your goals.\n\n")
        }

        // Performance Insight 2: Budget Adherence
        if (budgetGoal != null) {
            if (total > budgetGoal.maxGoal) {
                insights.append("• <font color='#EF4444'><b>Warning:</b></font> Your total spending of R ${String.format(Locale.getDefault(), "%.2f", total)} has exceeded your maximum budget goal of R ${String.format(Locale.getDefault(), "%.2f", budgetGoal.maxGoal)}.\n\n")
            } else {
                insights.append("• <font color='#059669'><b>Great Job!</b></font> You are currently within your budget. Keep this pace to earn the 'Budget Master' badge!\n\n")
            }
        } else {
            insights.append("• <b>Tip:</b> Set a monthly budget goal in the Dashboard to track your performance more accurately.\n\n")
        }

        // Performance Insight 3: Trends
        if (weeklySpending.size >= 2) {
            val lastWeek = weeklySpending.last().total
            val prevWeek = weeklySpending[weeklySpending.size - 2].total
            if (lastWeek < prevWeek) {
                insights.append("• <b>Trend:</b> Your spending decreased by R ${String.format(Locale.getDefault(), "%.2f", prevWeek - lastWeek)} compared to last week. Excellent discipline!\n")
            } else if (lastWeek > prevWeek) {
                insights.append("• <b>Trend:</b> Your spending increased this week. Try to identify what caused the R ${String.format(Locale.getDefault(), "%.2f", lastWeek - prevWeek)} spike.\n")
            }
        }

        insightsView.setText(android.text.Html.fromHtml(insights.toString(), android.text.Html.FROM_HTML_MODE_COMPACT))
    }
}

class CategorySpendingAdapter(private var data: List<Pair<CategorySpending, Double>>) :
    RecyclerView.Adapter<CategorySpendingAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_category_spending, parent, false)
        return ViewHolder(view)
    }
    override fun onBindViewHolder(holder: ViewHolder, pos: Int) {
        val (cs, percent) = data[pos]
        holder.tvName.text = cs.categoryName
        holder.tvAmount.text = String.format(Locale.getDefault(), "R %.2f", cs.total)
        holder.tvPercent.text = String.format(Locale.getDefault(), "%.0f%%", percent)
    }
    override fun getItemCount() = data.size
    fun updateData(newData: List<Pair<CategorySpending, Double>>) { data = newData; notifyDataSetChanged() }
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvCatName)
        val tvAmount: TextView = view.findViewById(R.id.tvCatAmount)
        val tvPercent: TextView = view.findViewById(R.id.tvCatPercent)
    }
}

class WeeklySpendingAdapter(private var data: List<WeeklySpending>) :
    RecyclerView.Adapter<WeeklySpendingAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_weekly_spending, parent, false)
        return ViewHolder(view)
    }
    override fun onBindViewHolder(holder: ViewHolder, pos: Int) {
        val week = data[pos]
        holder.tvWeek.text = week.weekLabel
        holder.tvAmount.text = String.format(Locale.getDefault(), "R %.2f", week.total)
    }
    override fun getItemCount() = data.size
    fun updateData(newData: List<WeeklySpending>) { data = newData; notifyDataSetChanged() }
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvWeek: TextView = view.findViewById(R.id.tvWeek)
        val tvAmount: TextView = view.findViewById(R.id.tvWeekAmount)
    }
}
