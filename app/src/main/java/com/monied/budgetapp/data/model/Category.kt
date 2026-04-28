package com.monied.budgetapp.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room Entity representing an Expense Category
 */
@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val name: String,
    val color: String, // Hex color code
    val icon: String? = null, // Emoji or icon identifier
    val expenseCount: Int = 0
)
