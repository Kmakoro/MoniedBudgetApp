package com.monied.budgetapp.ui.main


import android.app.DatePickerDialog
import android.os.Bundle
import android.text.InputType
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputLayout
import java.text.SimpleDateFormat
import java.util.*
import com.monied.budgetapp.data.SavingsGoal
import com.monied.budgetapp.data.DatabaseHelper
import com.monied.budgetapp.R
import com.monied.budgetapp.adapters.SavingsGoalAdapter
class SavingsGoalActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SavingsGoalAdapter
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var btnAddGoal: Button
    private lateinit var tvTotalGoals: TextView
    private lateinit var tvTotalSaved: TextView
    private lateinit var tvTotalTarget: TextView
    private lateinit var tvOverallProgress: TextView
    private lateinit var progressBarOverall: ProgressBar

    private val goalsList = mutableListOf<SavingsGoal>()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_savings_goal)

        dbHelper = DatabaseHelper(this)
        initViews()
        setupRecyclerView()
        loadSavingsGoals()

        btnAddGoal.setOnClickListener {
            showAddEditGoalDialog(null)
        }
    }

    private fun initViews() {
        recyclerView = findViewById(R.id.recyclerViewSavingsGoals)
        btnAddGoal = findViewById(R.id.btnAddSavingsGoal)
        tvTotalGoals = findViewById(R.id.tvTotalGoals)
        tvTotalSaved = findViewById(R.id.tvTotalSaved)
        tvTotalTarget = findViewById(R.id.tvTotalTarget)
        tvOverallProgress = findViewById(R.id.tvOverallProgress)
        progressBarOverall = findViewById(R.id.progressBarOverall)
    }

    private fun setupRecyclerView() {
        adapter = SavingsGoalAdapter(
            goals = goalsList,
            onItemClick = { goal -> showGoalDetails(goal) },
            onEditClick = { goal -> showAddEditGoalDialog(goal) },
            onDeleteClick = { goal -> confirmDeleteGoal(goal) },
            onAddMoneyClick = { goal -> showAddMoneyDialog(goal) }
        )
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun loadSavingsGoals() {
        goalsList.clear()
        goalsList.addAll(dbHelper.getSavingsGoals())
        adapter.notifyDataSetChanged()
        updateSummary()
    }

    private fun updateSummary() {
        val totalTarget = goalsList.sumOf { it.targetAmount }
        val totalSaved = goalsList.sumOf { it.currentAmount }
        val progressPercent = if (totalTarget > 0) (totalSaved / totalTarget) * 100 else 0.0

        tvTotalGoals.text = "Total Goals: ${goalsList.size}"
        tvTotalSaved.text = String.format("Total Saved: R%.2f", totalSaved)
        tvTotalTarget.text = String.format("Total Target: R%.2f", totalTarget)
        tvOverallProgress.text = String.format("Overall Progress: %.1f%%", progressPercent)
        progressBarOverall.progress = progressPercent.toInt()
    }

    private fun showAddEditGoalDialog(goal: SavingsGoal?) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_savings_goal, null)
        val etName = dialogView.findViewById<EditText>(R.id.etGoalName)
        val etTargetAmount = dialogView.findViewById<EditText>(R.id.etTargetAmount)
        val etCurrentAmount = dialogView.findViewById<EditText>(R.id.etCurrentAmount)
        val etTargetDate = dialogView.findViewById<EditText>(R.id.etTargetDate)
        val tilCurrentAmount = dialogView.findViewById<TextInputLayout>(R.id.tilCurrentAmount)

        val isEditing = goal != null

        if (isEditing) {
            etName.setText(goal.name)
            etTargetAmount.setText(goal.targetAmount.toString())
            etCurrentAmount.setText(goal.currentAmount.toString())
            etTargetDate.setText(goal.targetDate)
        } else {
            // For new goals, current amount is optional, default 0
            tilCurrentAmount.hint = "Current Amount (R) (Optional)"
        }

        // Date picker for target date
        etTargetDate.setOnClickListener {
            showDatePickerDialog { date ->
                etTargetDate.setText(date)
            }
        }
        etTargetDate.inputType = InputType.TYPE_NULL
        etTargetDate.isFocusable = false
        etTargetDate.isClickable = true

        AlertDialog.Builder(this)
            .setTitle(if (isEditing) "Edit Savings Goal" else "Add Savings Goal")
            .setView(dialogView)
            .setPositiveButton(if (isEditing) "Update" else "Add") { _, _ ->
                val name = etName.text.toString().trim()
                val targetAmount = etTargetAmount.text.toString().toDoubleOrNull()
                val targetDate = etTargetDate.text.toString().trim()

                if (name.isNotEmpty() && targetAmount != null && targetAmount > 0 && targetDate.isNotEmpty()) {
                    if (isEditing && goal != null) {
                        val currentAmount = etCurrentAmount.text.toString().toDoubleOrNull() ?: goal.currentAmount
                        dbHelper.updateSavingsGoal(goal.id, name, targetAmount, currentAmount, targetDate)
                        Toast.makeText(this, "Goal updated", Toast.LENGTH_SHORT).show()
                    } else {
                        val currentAmount = etCurrentAmount.text.toString().toDoubleOrNull() ?: 0.0
                        dbHelper.addSavingsGoal(name, targetAmount, currentAmount, targetDate)
                        Toast.makeText(this, "Goal added", Toast.LENGTH_SHORT).show()
                    }
                    loadSavingsGoals()
                } else {
                    Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showGoalDetails(goal: SavingsGoal) {
        val progress = if (goal.targetAmount > 0) {
            (goal.currentAmount / goal.targetAmount * 100).toInt()
        } else 0
        val remaining = goal.targetAmount - goal.currentAmount
        val daysLeft = calculateDaysLeft(goal.targetDate)

        val message = """
            Goal: ${goal.name}
            
            Target: R${String.format("%.2f", goal.targetAmount)}
            Saved: R${String.format("%.2f", goal.currentAmount)}
            Remaining: R${String.format("%.2f", remaining)}
            Progress: $progress%
            
            Target Date: ${formatDate(goal.targetDate)}
            Days Left: $daysLeft days
        """.trimIndent()

        AlertDialog.Builder(this)
            .setTitle("Goal Details")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .setNeutralButton("Add Money") { _, _ ->
                showAddMoneyDialog(goal)
            }
            .show()
    }

    private fun showAddMoneyDialog(goal: SavingsGoal) {
        val input = EditText(this)
        input.hint = "Enter amount to add"
        input.inputType = InputType.TYPE_NUMBER_FLAG_DECIMAL

        AlertDialog.Builder(this)
            .setTitle("Add Money to ${goal.name}")
            .setMessage(String.format("Current: R%.2f", goal.currentAmount))
            .setView(input)
            .setPositiveButton("Add") { _, _ ->
                val amount = input.text.toString().toDoubleOrNull()
                if (amount != null && amount > 0) {
                    val newAmount = goal.currentAmount + amount
                    dbHelper.updateSavingsGoalAmount(goal.id, newAmount)
                    loadSavingsGoals()
                    Toast.makeText(this, String.format("Added R%.2f", amount), Toast.LENGTH_SHORT).show()

                    if (newAmount >= goal.targetAmount) {
                        showGoalAchievedDialog(goal)
                    }
                } else {
                    Toast.makeText(this, "Invalid amount", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun confirmDeleteGoal(goal: SavingsGoal) {
        AlertDialog.Builder(this)
            .setTitle("Delete Goal")
            .setMessage("Are you sure you want to delete '${goal.name}'?")
            .setPositiveButton("Delete") { _, _ ->
                dbHelper.deleteSavingsGoal(goal.id)
                loadSavingsGoals()
                Toast.makeText(this, "Goal deleted", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showGoalAchievedDialog(goal: SavingsGoal) {
        AlertDialog.Builder(this)
            .setTitle("🎉 Congratulations! 🎉")
            .setMessage("You've achieved your savings goal: ${goal.name}!\nWell done!")
            .setPositiveButton("Awesome!", null)
            .show()
    }

    private fun showDatePickerDialog(onDateSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePicker = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            val date = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
            onDateSelected(date)
        }, year, month, day)

        datePicker.show()
    }

    private fun calculateDaysLeft(targetDate: String): Int {
        return try {
            val target = dateFormat.parse(targetDate)
            val today = Date()
            val diff = target.time - today.time
            maxOf((diff / (24 * 60 * 60 * 1000)).toInt(), 0)
        } catch (e: Exception) {
            0
        }
    }

    private fun formatDate(dateString: String): String {
        return try {
            val date = dateFormat.parse(dateString)
            SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(date)
        } catch (e: Exception) {
            dateString
        }
    }
}