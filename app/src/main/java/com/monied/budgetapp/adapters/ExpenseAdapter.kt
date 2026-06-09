package com.monied.budgetapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.card.MaterialCardView
import com.monied.budgetapp.R
import com.monied.budgetapp.models.Expense

class ExpenseAdapter(
    private var expenses: List<Expense>,
    private val onItemClick: (Expense) -> Unit,
    private val onDeleteClick: (Expense) -> Unit
) : RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_expense, parent, false)
        return ExpenseViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        holder.bind(expenses[position])
    }

    override fun getItemCount(): Int = expenses.size

    fun updateData(newExpenses: List<Expense>) {
        expenses = newExpenses
        notifyDataSetChanged()
    }

    inner class ExpenseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cvExpense: MaterialCardView = itemView.findViewById(R.id.cvExpense)
        private val tvAmount: TextView = itemView.findViewById(R.id.tvAmount)
        private val tvDescription: TextView = itemView.findViewById(R.id.tvDescription)
        private val tvCategory: TextView = itemView.findViewById(R.id.tvCategory)
        private val tvDateTime: TextView = itemView.findViewById(R.id.tvDateTime)
        private val btnDelete: ImageView = itemView.findViewById(R.id.btnDelete)
        private val ivPhoto: ImageView = itemView.findViewById(R.id.ivPhoto)
        private val ivPhotoIcon: ImageView = itemView.findViewById(R.id.ivPhotoIcon)

        fun bind(expense: Expense) {
            tvAmount.text = expense.formattedAmount
            tvDescription.text = expense.description
            tvCategory.text = expense.categoryName
            tvDateTime.text = expense.formattedDateTime

            // Handle photo display
            if (!expense.photoUri.isNullOrEmpty()) {
                Glide.with(itemView.context)
                    .load(expense.photoUri)
                    .thumbnail(0.25f)
                    .centerCrop()
                    .error(R.drawable.ic_image_error)
                    .into(ivPhoto)
                ivPhoto.visibility = View.VISIBLE
                ivPhotoIcon.visibility = View.GONE
            } else {
                ivPhoto.visibility = View.GONE
                ivPhotoIcon.visibility = View.VISIBLE
            }

            cvExpense.setOnClickListener { onItemClick(expense) }
            btnDelete.setOnClickListener { onDeleteClick(expense) }
        }
    }
}