package com.monied.budgetapp.ui.dialog

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.monied.budgetapp.R
import com.monied.budgetapp.data.BudgetAlertData
import com.monied.budgetapp.data.DatabaseHelper

class AlertsDialogFragment : BottomSheetDialogFragment() {

    private lateinit var dbHelper: DatabaseHelper
    private var userId: Int = -1

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_alerts, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dbHelper = DatabaseHelper(requireContext())
        val prefs = requireContext().getSharedPreferences("MoniedPrefs", Context.MODE_PRIVATE)
        userId = prefs.getInt("userId", -1)

        val rvAlerts = view.findViewById<RecyclerView>(R.id.rvAlerts)
        val tvEmpty = view.findViewById<TextView>(R.id.tvEmptyAlerts)
        val btnClose = view.findViewById<View>(R.id.btnClose)

        val alerts = dbHelper.getAlerts(userId)
        
        if (alerts.isEmpty()) {
            tvEmpty.visibility = View.VISIBLE
            rvAlerts.visibility = View.GONE
        } else {
            tvEmpty.visibility = View.GONE
            rvAlerts.visibility = View.VISIBLE
            rvAlerts.layoutManager = LinearLayoutManager(requireContext())
            rvAlerts.adapter = AlertsAdapter(alerts)
        }

        btnClose.setOnClickListener { dismiss() }
    }

    class AlertsAdapter(private val alerts: List<BudgetAlertData>) : RecyclerView.Adapter<AlertsAdapter.ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val v = LayoutInflater.from(parent.context).inflate(android.R.layout.simple_list_item_2, parent, false)
            return ViewHolder(v)
        }
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val context = holder.itemView.context
            val alert = alerts[position]
            holder.title.text = alert.title
            holder.message.text = context.getString(R.string.alert_message_format, alert.message, alert.date)
            
            // Basic styling for "Advanced" look
            holder.title.setTextColor(ContextCompat.getColor(context, R.color.red_500))
            holder.message.setTextColor(ContextCompat.getColor(context, R.color.gray_500))
        }
        override fun getItemCount() = alerts.size
        class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
            val title: TextView = v.findViewById(android.R.id.text1)
            val message: TextView = v.findViewById(android.R.id.text2)
        }
    }
}
