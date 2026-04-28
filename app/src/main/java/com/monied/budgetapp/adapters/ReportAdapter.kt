package com.monied.budgetapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.monied.budgetapp.data.CategorySpending // Ensure this points to your internal data class

class ReportAdapter(private var spendingList: List<CategorySpending>) : RecyclerView.Adapter<ReportAdapter.ReportViewHolder>() {

    class ReportViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvCategory: TextView = itemView.findViewById(android.R.id.text1)
        val tvTotal: TextView = itemView.findViewById(android.R.id.text2)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(android.R.layout.simple_list_item_2, parent, false)
        return ReportViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReportViewHolder, position: Int) {
        val item = spendingList[position]
        holder.tvCategory.text = item.categoryName
        holder.tvTotal.text = "Total Spent: R ${String.format("%.2f", item.total)}"
    }

    override fun getItemCount(): Int = spendingList.size

    fun updateData(newList: List<CategorySpending>) {
        spendingList = newList
        notifyDataSetChanged()
    }
}