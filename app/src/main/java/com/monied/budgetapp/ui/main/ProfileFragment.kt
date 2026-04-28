package com.monied.budgetapp.ui.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.monied.budgetapp.R
import com.monied.budgetapp.data.DatabaseHelper
import com.monied.budgetapp.data.SavingsGoal
import com.monied.budgetapp.ui.auth.LoginActivity

class ProfileFragment : Fragment() {

    private lateinit var dbHelper: DatabaseHelper

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dbHelper = DatabaseHelper(requireContext())

        val prefs = requireContext().getSharedPreferences("MoniedPrefs", Context.MODE_PRIVATE)
        val currentUser = prefs.getString("loggedInUser", "cyril") ?: "cyril"
        val user = dbHelper.getUser(currentUser)

        if (user != null) {
            view.findViewById<TextView>(R.id.tvFullName).text = user.fullName
            view.findViewById<TextView>(R.id.tvEmail).text = user.email
            view.findViewById<TextView>(R.id.tvInfoFullName).text = user.fullName
            view.findViewById<TextView>(R.id.tvInfoEmail).text = user.email
            view.findViewById<TextView>(R.id.tvInfoPhone).text = user.phone
        }

        val stats = dbHelper.getUserStats(currentUser)
        view.findViewById<TextView>(R.id.tvStats).text = "${stats.expenseCount} Expenses • ${stats.categoryCount} Categories • ${stats.daysActive} Days Active"

        val rvSavings = view.findViewById<RecyclerView>(R.id.rvSavingsGoals)
        rvSavings.layoutManager = LinearLayoutManager(requireContext())
        val goals = dbHelper.getSavingsGoals()
        rvSavings.adapter = SavingsGoalAdapter(goals)

        // Mock alerts count
        view.findViewById<TextView>(R.id.tvAlertCount).text = "3 New"

        view.findViewById<Button>(R.id.btnLogout).setOnClickListener {
            prefs.edit().remove("loggedInUser").apply()
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            requireActivity().finish()
        }
    }
}

class SavingsGoalAdapter(private val goals: List<SavingsGoal>) : RecyclerView.Adapter<SavingsGoalAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(android.R.layout.simple_list_item_2, parent, false)
        return ViewHolder(view)
    }
    override fun onBindViewHolder(holder: ViewHolder, pos: Int) {
        val g = goals[pos]
        holder.text1.text = g.name
        holder.text2.text = "R ${g.currentAmount} / R ${g.targetAmount}"
    }
    override fun getItemCount() = goals.size
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val text1: TextView = view.findViewById(android.R.id.text1)
        val text2: TextView = view.findViewById(android.R.id.text2)
    }
}