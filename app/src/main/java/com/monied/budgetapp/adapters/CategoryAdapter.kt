package com.monied.budgetapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.monied.budgetapp.R
import com.monied.budgetapp.models.Category

class CategoryAdapter(
    private var categoryList: List<Category>,
    private val onCategoryClick: (Category) -> Unit,
    private val onEditClick: (Category) -> Unit,
    private val onDeleteClick: (Category) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvIcon: TextView = itemView.findViewById(R.id.tvCategoryIcon)
        val tvName: TextView = itemView.findViewById(R.id.tvCategoryName)
        val tvCount: TextView = itemView.findViewById(R.id.tvExpenseCount)
        val btnEdit: ImageButton = itemView.findViewById(R.id.btnEdit)
        val btnDelete: ImageButton = itemView.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_category, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val currentCategory = categoryList[position]

        holder.tvName.text = currentCategory.name
        holder.tvCount.text = "${currentCategory.expenseCount} expenses | R ${String.format("%.2f", currentCategory.totalSpent)}"
        holder.tvIcon.text = currentCategory.name.take(1).uppercase()

        holder.itemView.setOnClickListener {
            onCategoryClick(currentCategory)
        }

        // When clicked,  pass the currentCategory back to the Activity
        holder.btnEdit.setOnClickListener {
            onEditClick(currentCategory)
        }

        holder.btnDelete.setOnClickListener {
            onDeleteClick(currentCategory)
        }
    }

    override fun getItemCount(): Int = categoryList.size

    fun updateData(newList: List<Category>) {
        categoryList = newList
        notifyDataSetChanged()
    }
}