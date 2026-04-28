package com.monied.budgetapp.ui.main

import android.app.DatePickerDialog
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import com.monied.budgetapp.R
import com.monied.budgetapp.data.DatabaseHelper
import com.monied.budgetapp.models.Expense
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class HistoryFragment : Fragment() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var adapter: ExpenseHistoryAdapter
    private val expenseList = mutableListOf<Expense>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dbHelper = DatabaseHelper(requireContext())

        val rvExpenses = view.findViewById<RecyclerView>(R.id.rvExpenses)
        rvExpenses.layoutManager = LinearLayoutManager(requireContext())
        adapter = ExpenseHistoryAdapter(expenseList,
            onItemClick = { expense -> showExpenseDetails(expense) },      // Click on whole item
            onPhotoClick = { expense -> showPhotoDialog(expense) }        // Click on photo button
        )
        rvExpenses.adapter = adapter

        val etStartDate = view.findViewById<TextInputEditText>(R.id.etStartDate)
        val etEndDate = view.findViewById<TextInputEditText>(R.id.etEndDate)
        val btnFilter = view.findViewById<Button>(R.id.btnFilter)
        val tvTotalAmount = view.findViewById<TextView>(R.id.tvTotalAmount)
        val tvCount = view.findViewById<TextView>(R.id.tvCount)

        etStartDate.setOnClickListener { showDatePicker(etStartDate) }
        etEndDate.setOnClickListener { showDatePicker(etEndDate) }

        btnFilter.setOnClickListener {
            loadExpenses(etStartDate.text.toString(), etEndDate.text.toString(), tvTotalAmount, tvCount)
        }

        val dateFormat = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
        val cal = Calendar.getInstance()
        etEndDate.setText(dateFormat.format(cal.time))
        cal.add(Calendar.DAY_OF_MONTH, -10)
        etStartDate.setText(dateFormat.format(cal.time))
        loadExpenses(etStartDate.text.toString(), etEndDate.text.toString(), tvTotalAmount, tvCount)
    }

    private fun showDatePicker(editText: TextInputEditText) {
        val c = Calendar.getInstance()
        DatePickerDialog(requireContext(), { _, year, month, day ->
            editText.setText("$year/${month+1}/$day")
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun loadExpenses(start: String, end: String, totalView: TextView, countView: TextView) {
        expenseList.clear()
        expenseList.addAll(dbHelper.getExpensesByDateRange(start, end))
        adapter.notifyDataSetChanged()
        val total = expenseList.sumOf { it.amount }
        totalView.text = "R %.2f".format(total)
        countView.text = expenseList.size.toString()
    }

    private fun showExpenseDetails(expense: Expense) {
        AlertDialog.Builder(requireContext())
            .setTitle(expense.description)
            .setMessage("Amount: R ${expense.amount}\nDate: ${expense.date}\nTime: ${expense.startTime} - ${expense.endTime}\nCategory: ${expense.categoryName}")
            .setPositiveButton("OK", null)
            .show()
    }

    private fun showPhotoDialog(expense: Expense) {
        if (expense.photoUri.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "No photo attached", Toast.LENGTH_SHORT).show()
            return
        }

        val uri = Uri.parse(expense.photoUri)
        val imageView = ImageView(requireContext())
        imageView.scaleType = ImageView.ScaleType.CENTER_INSIDE
        imageView.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        try {
            if (uri.scheme == "file") {
                val file = File(uri.path)
                if (file.exists()) {
                    val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                    imageView.setImageBitmap(bitmap)
                } else {
                    imageView.setImageResource(android.R.drawable.ic_menu_gallery)
                    Toast.makeText(requireContext(), "Image file not found", Toast.LENGTH_SHORT).show()
                }
            } else {
                imageView.setImageURI(uri)
            }
        } catch (e: Exception) {
            imageView.setImageResource(android.R.drawable.ic_menu_gallery)
            Toast.makeText(requireContext(), "Cannot load image", Toast.LENGTH_SHORT).show()
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Receipt Photo")
            .setView(imageView)
            .setPositiveButton("Close") { d, _ -> d.dismiss() }
            .show()
    }
}

class ExpenseHistoryAdapter(
    private val list: List<Expense>,
    private val onItemClick: (Expense) -> Unit,
    private val onPhotoClick: (Expense) -> Unit
) : RecyclerView.Adapter<ExpenseHistoryAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_expense_history, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val expense = list[position]
        holder.tvCategory.text = expense.categoryName
        holder.tvDesc.text = expense.description
        holder.tvAmount.text = "R %.2f".format(expense.amount)
        holder.tvDateTime.text = "${expense.date} • ${expense.startTime} - ${expense.endTime}"

        // Whole item click shows details
        holder.itemView.setOnClickListener { onItemClick(expense) }

        // Photo button click shows photo (only if photo exists)
        if (!expense.photoUri.isNullOrEmpty()) {
            holder.btnViewPhoto.visibility = View.VISIBLE
            holder.btnViewPhoto.setOnClickListener { onPhotoClick(expense) }
        } else {
            holder.btnViewPhoto.visibility = View.GONE
        }
    }

    override fun getItemCount() = list.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvCategory: TextView = itemView.findViewById(R.id.tvCategoryName)
        val tvDesc: TextView = itemView.findViewById(R.id.tvDescription)
        val tvAmount: TextView = itemView.findViewById(R.id.tvAmount)
        val tvDateTime: TextView = itemView.findViewById(R.id.tvDateTime)
        val btnViewPhoto: Button = itemView.findViewById(R.id.btnViewPhoto)
    }
}