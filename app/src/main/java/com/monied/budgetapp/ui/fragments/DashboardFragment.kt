package com.monied.budgetapp.ui.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.monied.budgetapp.R
import com.monied.budgetapp.databinding.FragmentDashboardBinding
import com.monied.budgetapp.ui.main.*
import com.monied.budgetapp.ui.dialog.BadgesDialogFragment
import com.monied.budgetapp.ui.dialog.AlertsDialogFragment
import com.monied.budgetapp.data.DatabaseHelper
import com.monied.budgetapp.models.Expense
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
        binding.ivTrophy.setOnClickListener {
            BadgesDialogFragment().show(parentFragmentManager, "BadgesDialog")
        }

        binding.ivProfileAvatar.setOnClickListener {
            (activity as? MainActivity)?.openProfile()
        }

        binding.cardBudgetSummary.setOnClickListener {
            startActivity(Intent(requireContext(), SpendingInsightsActivity::class.java))
        }

        binding.cardSavingsGoals.setOnClickListener {
            startActivity(Intent(requireContext(), SavingsGoalActivity::class.java))
        }

        binding.cardBudgetAlerts.setOnClickListener {
            AlertsDialogFragment().show(parentFragmentManager, "AlertsDialog")
        }

        // Quick Actions
        binding.actionAddCategory.setOnClickListener {
            startActivity(Intent(requireContext(), CategoryActivity::class.java))
        }

        binding.actionSetBudget.setOnClickListener {
            showSetBudgetDialog()
        }

        binding.actionSavings.setOnClickListener {
            startActivity(Intent(requireContext(), SavingsGoalActivity::class.java))
        }

        binding.actionReports.setOnClickListener {
            startActivity(Intent(requireContext(), SpendingReportActivity::class.java))
        }

        binding.tvViewAll.setOnClickListener {
            startActivity(Intent(requireContext(), ViewExpensesActivity::class.java))
        }
    }

    private fun loadData() {
        if (userId == -1) return

        val calendar = Calendar.getInstance()
        // Use Locale.US for database consistency
        val currentMonth = SimpleDateFormat("yyyy-MM", Locale.US).format(calendar.time)

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

        // Load Recent Expenses into the container - passing empty range to get all recent
        val expenses = dbHelper.getExpensesByDateRange("", "", userId)
        binding.expensesContainer.removeAllViews()
        
        if (expenses.isNotEmpty()) {
            binding.tvNoExpenses.visibility = View.GONE
            val displayCount = if (expenses.size > 5) 5 else expenses.size
            val inflater = LayoutInflater.from(requireContext())
            
            for (i in 0 until displayCount) {
                val expense = expenses[i]
                val itemView = inflater.inflate(R.layout.item_recent_transaction, binding.expensesContainer, false)
                
                itemView.findViewById<TextView>(R.id.tvDescription).text = expense.description
                itemView.findViewById<TextView>(R.id.tvCategoryDate).text = "${expense.categoryName} • ${expense.date}"
                itemView.findViewById<TextView>(R.id.tvAmount).text = "- R ${String.format(Locale.getDefault(), "%.2f", expense.amount)}"
                
                binding.expensesContainer.addView(itemView)
            }
        } else {
            binding.tvNoExpenses.visibility = View.VISIBLE
        }
    }

    private fun showSetBudgetDialog() {
        val calendar = Calendar.getInstance()
        val currentMonth = SimpleDateFormat("yyyy-MM", Locale.US).format(calendar.time)
        val budgetGoal = dbHelper.getBudgetGoal(currentMonth, userId)

        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_savings_goal, null)
        val etMin = dialogView.findViewById<TextInputEditText>(R.id.etTargetAmount)
        val etMax = dialogView.findViewById<TextInputEditText>(R.id.etCurrentAmount)
        val etMonth = dialogView.findViewById<TextInputEditText>(R.id.etGoalName)
        val etDate = dialogView.findViewById<TextInputEditText>(R.id.etTargetDate)
        val tilMin = dialogView.findViewById<TextInputLayout>(R.id.tilTargetAmount)
        val tilMax = dialogView.findViewById<TextInputLayout>(R.id.tilCurrentAmount)

        tilMin.hint = "Minimum Goal (R)"
        tilMax.hint = "Maximum Budget (R)"
        etMonth.setText(SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(calendar.time))
        etMonth.isEnabled = false
        etDate.visibility = View.GONE

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