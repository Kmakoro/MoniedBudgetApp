package com.monied.budgetapp.models

data class Category(
    val id: Int,
    val name: String,
    val expenseCount: Int = 0,
    val totalSpent: Double = 0.0
)