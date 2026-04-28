package com.monied.budgetapp.adapters



import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.monied.budgetapp.R
import com.monied.budgetapp.data.SavingsGoal

class SavingsGoalAdapter(
    private val goals: List<SavingsGoal>,
    private val onItemClick: (SavingsGoal) -> Unit,
    private val onEditClick: (SavingsGoal) -> Unit,
    private val onDeleteClick: (SavingsGoal) -> Unit,
    private val onAddMoneyClick: (SavingsGoal) -> Unit
) : RecyclerView.Adapter<SavingsGoalAdapter.GoalViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GoalViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_savings_goal, parent, false)
        return GoalViewHolder(view)
    }

    override fun onBindViewHolder(holder: GoalViewHolder, position: Int) {
        holder.bind(goals[position])
    }

    override fun getItemCount(): Int = goals.size

    inner class GoalViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cvGoal: CardView = itemView.findViewById(R.id.cvGoal)
        private val tvName: TextView = itemView.findViewById(R.id.tvGoalName)
        private val tvTarget: TextView = itemView.findViewById(R.id.tvTargetAmount)
        private val tvSaved: TextView = itemView.findViewById(R.id.tvSavedAmount)
        private val tvProgress: TextView = itemView.findViewById(R.id.tvProgressPercent)
        private val progressBar: ProgressBar = itemView.findViewById(R.id.progressBarGoal)
        private val tvEdit: TextView = itemView.findViewById(R.id.tvEdit)
        private val tvDelete: TextView = itemView.findViewById(R.id.tvDelete)
        private val tvAddMoney: TextView = itemView.findViewById(R.id.tvAddMoney)

        fun bind(goal: SavingsGoal) {
            tvName.text = goal.name
            tvTarget.text = String.format("Target: $%.2f", goal.targetAmount)
            tvSaved.text = String.format("Saved: $%.2f", goal.currentAmount)

            val progressPercent = if (goal.targetAmount > 0) {
                (goal.currentAmount / goal.targetAmount * 100).toInt()
            } else 0
            tvProgress.text = "$progressPercent%"
            progressBar.progress = progressPercent

            cvGoal.setOnClickListener { onItemClick(goal) }
            tvEdit.setOnClickListener { onEditClick(goal) }
            tvDelete.setOnClickListener { onDeleteClick(goal) }
            tvAddMoney.setOnClickListener { onAddMoneyClick(goal) }
        }
    }
}