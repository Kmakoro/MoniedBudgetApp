package com.monied.budgetapp.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

/**
 * Room Entity representing a Savings Goal
 */
@Entity(tableName = "savings_goals")
data class SavingsGoal(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val name: String,
    val targetAmount: Double,
    val currentAmount: Double = 0.0,
    val deadline: Date,
    val icon: String? = null, // Emoji
    val color: String, // Hex color code
    val createdAt: Date = Date()
)
