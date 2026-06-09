package com.monied.budgetapp.ui.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.monied.budgetapp.databinding.FragmentDashboardBinding
import com.monied.budgetapp.ui.main.SpendingInsightsActivity
import com.monied.budgetapp.ui.main.ViewExpensesActivity
import com.monied.budgetapp.ui.dialog.BadgesDialogFragment
import com.monied.budgetapp.ui.main.MainActivity
import com.monied.budgetapp.ui.main.SavingsGoalActivity
import com.monied.budgetapp.data.DatabaseHelper
import java.text.SimpleDateFormat
import java.util.*

/**
 * Dashboard Fragment - Home screen showing budget overview and quick actions
 */
class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private lateinit var dbHelper: DatabaseHelper
    private var userId: Int = -1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dbHelper = DatabaseHelper(requireContext())

        val prefs = requireContext().getSharedPreferences("MoniedPrefs", Context.MODE_PRIVATE)
        userId = prefs.getInt("userId", -1)

        setupUI()
        loadData()
    }

    override fun onResume() {
        super.onResume()
        loadData()
    }

    private fun setupUI() {
        // Trophy button for gamification
        binding.ivTrophy.setOnClickListener {
            BadgesDialogFragment().show(parentFragmentManager, "BadgesDialog")
        }

        // Profile icon click
        binding.ivProfileAvatar.setOnClickListener {
            (activity as? MainActivity)?.openProfile()
        }

        // Budget Summary card click to view insights
        binding.cardBudgetSummary.setOnClickListener {
            startActivity(Intent(requireContext(), SpendingInsightsActivity::class.java))
        }

        // Savings Goals card click
        binding.cardSavingsGoals.setOnClickListener {
            startActivity(Intent(requireContext(), SavingsGoalActivity::class.java))
        }

        // Budget Alerts card click
        binding.cardBudgetAlerts.setOnClickListener {
            // Show alerts in a dialog or navigate to history
            showAlertsDialog()
        }

        // Budget Goals card click
        binding.cardBudgetGoals.setOnClickListener {
            showSetBudgetDialog()
        }

        // View All expenses click
        binding.tvViewAll.setOnClickListener {
            startActivity(Intent(requireContext(), ViewExpensesActivity::class.java))
        }
    }

    private fun loadData() {
        if (userId == -1) return

        val calendar = Calendar.getInstance()
        val currentMonth = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(calendar.time)

        // Load Budget Goals
        val budgetGoal = dbHelper.getBudgetGoal(currentMonth, userId)
        val minGoal = budgetGoal?.minGoal ?: 0.0
        val maxGoal = budgetGoal?.maxGoal ?: 0.0

        binding.tvMinGoal.text = "Min: R ${String.format(Locale.getDefault(), "%.0f", minGoal)}"
        binding.tvMaxGoal.text = "Max: R ${String.format(Locale.getDefault(), "%.0f", maxGoal)}"

        // Load Current Spending
        val totalSpent = dbHelper.getMonthlyTotalSpent(userId, currentMonth)
        binding.tvSpendingAmount.text = "R ${String.format(Locale.getDefault(), "%.2f", totalSpent)}"

        // Update Progress Bar
        val progress = if (maxGoal > 0) {
            ((totalSpent / maxGoal) * 100).toInt()
        } else 0
        binding.progressBudget.progress = progress.coerceIn(0, 100)

        // Load Savings
        val savingsGoals = dbHelper.getSavingsGoals(userId)
        val totalSaved = savingsGoals.sumOf { it.currentAmount }
        binding.tvSavingsAmount.text = "R ${String.format(Locale.getDefault(), "%.0f", totalSaved)}"

        // Alerts count
        val alerts = dbHelper.getAlerts(userId)
        binding.tvAlertCount.text = "${alerts.size} New"

        // Recent Expenses
        val expenses = dbHelper.getExpensesByDateRange("", "", userId) // get latest
        if (expenses.isNotEmpty()) {
            binding.tvNoExpenses.visibility = View.GONE
            val latest = expenses.first()
            binding.tvNoExpenses.text = "Latest: ${latest.description} - R${String.format(Locale.getDefault(), "%.2f", latest.amount)}"
            binding.tvNoExpenses.visibility = View.VISIBLE
        } else {
            binding.tvNoExpenses.text = "No recent expenses"
            binding.tvNoExpenses.visibility = View.VISIBLE
        }
    }

    private fun showAlertsDialog() {
        val alerts = dbHelper.getAlerts(userId)
        if (alerts.isEmpty()) {
            android.widget.Toast.makeText(context, "No new alerts", android.widget.Toast.LENGTH_SHORT).show()
            return
        }

        val alertMessages = alerts.map { "${it.title}: ${it.message}\n(${it.date})" }.toTypedArray()
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Budget Alerts")
            .setItems(alertMessages, null)
            .setPositiveButton("Close", null)
            .show()
    }

    private fun showSetBudgetDialog() {
        val calendar = Calendar.getInstance()
        val currentMonth = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(calendar.time)
        val budgetGoal = dbHelper.getBudgetGoal(currentMonth, userId)

        val dialogView = LayoutInflater.from(requireContext()).inflate(com.monied.budgetapp.R.layout.dialog_savings_goal, null)
        val etMin = dialogView.findViewById<android.widget.EditText>(com.monied.budgetapp.R.id.etTargetAmount)
        val etMax = dialogView.findViewById<android.widget.EditText>(com.monied.budgetapp.R.id.etCurrentAmount)
        val etMonth = dialogView.findViewById<android.widget.EditText>(com.monied.budgetapp.R.id.etGoalName)
        val etDate = dialogView.findViewById<android.widget.EditText>(com.monied.budgetapp.R.id.etTargetDate)

        dialogView.findViewById<com.google.android.material.textfield.TextInputLayout>(com.monied.budgetapp.R.id.tilCurrentAmount).hint = "Maximum Budget (R)"
        etMonth.setText(SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(calendar.time))
        etMonth.isEnabled = false
        etDate.visibility = View.GONE

        val tilMin = etMin.parent.parent as com.google.android.material.textfield.TextInputLayout
        tilMin.hint = "Minimum Goal (R)"

        etMin.setText((budgetGoal?.minGoal ?: 500.0).toString())
        etMax.setText((budgetGoal?.maxGoal ?: 2000.0).toString())

        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Set Monthly Budget")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val min = etMin.text.toString().toDoubleOrNull() ?: 0.0
                val max = etMax.text.toString().toDoubleOrNull() ?: 0.0
                dbHelper.updateBudgetGoal(currentMonth, min, max, userId)
                loadData()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
