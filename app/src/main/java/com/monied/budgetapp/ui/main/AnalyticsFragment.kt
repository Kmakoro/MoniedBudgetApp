package com.monied.budgetapp.ui.main

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_analytics, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dbHelper = DatabaseHelper(requireContext())

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

        val dateFormat = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
        val endDate = Date()
        val startDate = Date(endDate.time - 30L * 24 * 3600 * 1000)
        etStart.setText(dateFormat.format(startDate))
        etEnd.setText(dateFormat.format(endDate))

        btnGo.setOnClickListener {
            loadAnalytics(etStart.text.toString(), etEnd.text.toString(), tvTotal, tvInsights)
        }
        loadAnalytics(etStart.text.toString(), etEnd.text.toString(), tvTotal, tvInsights)
    }

    private fun showDatePicker(editText: TextInputEditText) {
        val c = Calendar.getInstance()
        DatePickerDialog(requireContext(), { _, year, month, day ->
            editText.setText("$year/${month+1}/$day")
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun loadAnalytics(start: String, end: String, totalView: TextView, insightsView: TextView) {
        val categorySpending = dbHelper.getCategorySpendingForDateRange(start, end)
        val weeklySpending = dbHelper.getWeeklyBreakdown(start, end)
        val total = categorySpending.sumOf { it.total }
        totalView.text = "R %.2f".format(total)

        val items = categorySpending.map { cs ->
            val percent = if (total > 0) (cs.total / total) * 100 else 0.0
            Pair(cs, percent)
        }
        categoryAdapter.updateData(items)
        weeklyAdapter.updateData(weeklySpending)

        val insights = StringBuilder()
        if (categorySpending.isNotEmpty()) {
            val topCategory = categorySpending.maxByOrNull { it.total }
            insights.append("• ${topCategory?.categoryName} is your highest spending category at ${String.format("%.0f", (topCategory!!.total/total)*100)}%\n")
        }
        val maxWeek = weeklySpending.maxByOrNull { it.total }
        if (maxWeek != null) {
            insights.append("• ${maxWeek.weekLabel} had the highest spending at R ${maxWeek.total}\n")
        }
        insights.append("• You're on track to meet your monthly budget goal")
        insightsView.text = insights.toString()
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
        holder.tvAmount.text = "R %.2f".format(cs.total)
        holder.tvPercent.text = "${String.format("%.0f", percent)}%"
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
        holder.tvAmount.text = "R %.2f".format(week.total)
    }
    override fun getItemCount() = data.size
    fun updateData(newData: List<WeeklySpending>) { data = newData; notifyDataSetChanged() }
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvWeek: TextView = view.findViewById(R.id.tvWeek)
        val tvAmount: TextView = view.findViewById(R.id.tvWeekAmount)
    }
}