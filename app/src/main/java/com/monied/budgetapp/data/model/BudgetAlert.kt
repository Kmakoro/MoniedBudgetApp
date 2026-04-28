package com.monied.budgetapp.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

/**
 * Room Entity representing a Budget Alert/Notification
 */
@Entity(tableName = "budget_alerts")
data class BudgetAlert(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val title: String,
    val message: String,
    val type: AlertType,
    val severity: AlertSeverity,
    val category: String? = null,
    val isDismissed: Boolean = false,
    val createdAt: Date = Date()
)

enum class AlertType {
    BUDGET_WARNING,
    BUDGET_EXCEEDED,
    CATEGORY_ALERT,
    WEEKLY_SUMMARY,
    GOAL_ACHIEVED
}

enum class AlertSeverity {
    INFO,
    SUCCESS,
    WARNING,
    CRITICAL
}
