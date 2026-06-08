package com.monied.budgetapp.ui.main

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.switchmaterial.SwitchMaterial
import com.monied.budgetapp.R
import com.monied.budgetapp.data.DatabaseHelper
import com.monied.budgetapp.data.SavingsGoal
import com.monied.budgetapp.ui.auth.LoginActivity
import com.monied.budgetapp.utils.GamificationManager
import java.util.Locale

class ProfileFragment : Fragment() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var prefs: SharedPreferences
    private lateinit var gamificationManager: GamificationManager
    private var userId: Int = -1

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dbHelper = DatabaseHelper(requireContext())
        gamificationManager = GamificationManager(requireContext())
        prefs = requireContext().getSharedPreferences("MoniedPrefs", Context.MODE_PRIVATE)
        userId = prefs.getInt("userId", -1)

        val currentUser = prefs.getString("loggedInUser", "cyril") ?: "cyril"
        val user = dbHelper.getUser(currentUser)

        if (user != null) {
            view.findViewById<TextView>(R.id.tvFullName).text = user.fullName
            view.findViewById<TextView>(R.id.tvEmail).text = user.email
            view.findViewById<TextView>(R.id.tvInfoFullName).text = user.fullName
            view.findViewById<TextView>(R.id.tvInfoEmail).text = user.email
            view.findViewById<TextView>(R.id.tvInfoPhone).text = user.phone

            // Setup Badges (Showing all, with earned ones highlighted)
            val rvBadges = view.findViewById<RecyclerView>(R.id.rvBadges)
            rvBadges.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            rvBadges.adapter = ProfileBadgeAdapter(userId, gamificationManager)

            // Setup Savings Goals
            val rvSavings = view.findViewById<RecyclerView>(R.id.rvSavingsGoals)
            rvSavings.layoutManager = LinearLayoutManager(requireContext())
            val goals = dbHelper.getSavingsGoals(user.id)
            rvSavings.adapter = SavingsGoalAdapter(goals)
        }

        // Dynamic Statistics
        val stats = dbHelper.getUserStats(currentUser)
        view.findViewById<TextView>(R.id.tvStats).text = String.format(Locale.getDefault(), "%d Expenses • %d Categories • %d Days Active", stats.expenseCount, stats.categoryCount, stats.daysActive)

        // Dark Mode Toggle
        val switchDarkMode = view.findViewById<SwitchMaterial>(R.id.switchDarkMode)
        val isDarkMode = prefs.getBoolean("isDarkMode", false)
        switchDarkMode.isChecked = isDarkMode

        switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("isDarkMode", isChecked).apply()
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }

        view.findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        view.findViewById<Button>(R.id.btnLogout).setOnClickListener {
            prefs.edit().remove("loggedInUser").remove("userId").apply()
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            requireActivity().finish()
        }
    }
}

class ProfileBadgeAdapter(private val userId: Int, private val gamificationManager: GamificationManager) :
    RecyclerView.Adapter<ProfileBadgeAdapter.ViewHolder>() {

    private val allBadges = GamificationManager.Badge.values().toList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_badge, parent, false)
        // Adjust width for horizontal scroll
        view.layoutParams = ViewGroup.LayoutParams(600, ViewGroup.LayoutParams.WRAP_CONTENT)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, pos: Int) {
        val badge = allBadges[pos]
        holder.tvTitle.text = badge.title
        holder.tvDesc.text = badge.description
        holder.ivIcon.setImageResource(badge.iconRes)

        val isUnlocked = if (userId != -1) gamificationManager.hasBadge(userId, badge) else false
        if (isUnlocked) {
            holder.itemView.alpha = 1.0f
            holder.ivStatus.visibility = View.VISIBLE
            holder.ivStatus.setImageResource(android.R.drawable.checkbox_on_background)
        } else {
            holder.itemView.alpha = 0.4f
            holder.ivStatus.visibility = View.GONE
        }
    }

    override fun getItemCount() = allBadges.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tvBadgeTitle)
        val tvDesc: TextView = view.findViewById(R.id.tvBadgeDescription)
        val ivIcon: ImageView = view.findViewById(R.id.ivBadgeIcon)
        val ivStatus: ImageView = view.findViewById(R.id.ivStatus)
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
        holder.text1.setTextColor(if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) 0xFFFFFFFF.toInt() else 0xFF1F2937.toInt())
        holder.text2.text = String.format(Locale.getDefault(), "R %.2f / R %.2f", g.currentAmount, g.targetAmount)
        holder.text2.setTextColor(0xFF6B7280.toInt())
    }
    override fun getItemCount() = goals.size
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val text1: TextView = view.findViewById(android.R.id.text1)
        val text2: TextView = view.findViewById(android.R.id.text2)
    }
}
