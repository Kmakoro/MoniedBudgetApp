package com.monied.budgetapp.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

/**
 * Room Entity representing Monthly Budget Goals
 */
@Entity(tableName = "budget_goals")
data class BudgetGoal(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val month: String, // Format: "YYYY-MM"
    val minimumGoal: Double,
    val maximumGoal: Double,
    val createdAt: Date = Date()
)
