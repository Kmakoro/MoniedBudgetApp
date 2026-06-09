package com.monied.budgetapp.ui.dialog

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.monied.budgetapp.R
import com.monied.budgetapp.utils.GamificationManager

class BadgesDialogFragment : DialogFragment() {

    private lateinit var gamificationManager: GamificationManager
    private var userId: Int = -1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_badges, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val prefs = requireContext().getSharedPreferences("MoniedPrefs", Context.MODE_PRIVATE)
        userId = prefs.getInt("userId", -1)

        gamificationManager = GamificationManager(requireContext())

        val rvBadges = view.findViewById<RecyclerView>(R.id.rvBadges)
        val btnClose = view.findViewById<View>(R.id.btnClose)

        rvBadges.layoutManager = LinearLayoutManager(context)
        rvBadges.adapter = BadgeAdapter(GamificationManager.Badge.values().toList())

        btnClose.setOnClickListener { dismiss() }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    inner class BadgeAdapter(private val badges: List<GamificationManager.Badge>) :
        RecyclerView.Adapter<BadgeAdapter.BadgeViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BadgeViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_badge, parent, false)
            return BadgeViewHolder(view)
        }

        override fun onBindViewHolder(holder: BadgeViewHolder, position: Int) {
            val badge = badges[position]
            holder.bind(badge)
        }

        override fun getItemCount(): Int = badges.size

        inner class BadgeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val ivIcon: ImageView = itemView.findViewById(R.id.ivBadgeIcon)
            private val tvTitle: TextView = itemView.findViewById(R.id.tvBadgeTitle)
            private val tvDesc: TextView = itemView.findViewById(R.id.tvBadgeDescription)
            private val ivStatus: ImageView = itemView.findViewById(R.id.ivStatus)

            fun bind(badge: GamificationManager.Badge) {
                tvTitle.text = badge.title
                tvDesc.text = badge.description
                ivIcon.setImageResource(badge.iconRes)

                val isUnlocked = if (userId != -1) gamificationManager.hasBadge(userId, badge) else false
                if (isUnlocked) {
                    itemView.alpha = 1.0f
                    ivStatus.visibility = View.VISIBLE
                    // Set a "checked" or "ticked" icon if available, or just keep ivStatus visible
                    ivStatus.setImageResource(android.R.drawable.checkbox_on_background)
                } else {
                    itemView.alpha = 0.4f
                    ivStatus.visibility = View.GONE
                }
            }
        }
    }
}
