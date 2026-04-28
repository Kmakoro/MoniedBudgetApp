package com.monied.budgetapp.data

import androidx.room.Entity

@Entity(tableName = "SavingsGoals")
data class SavingsGoal(
    val id: Int,
    val name: String,
    val targetAmount: Double,
    val currentAmount: Double,
    val targetDate: String
)