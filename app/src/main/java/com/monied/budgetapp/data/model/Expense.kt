package com.monied.budgetapp.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

/**
 * Room Entity representing an Expense
 */
@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val amount: Double,
    val date: Date,
    val startTime: String,
    val endTime: String? = null,
    val categoryId: Long,
    val description: String,
    val photoUri: String? = null,
    val createdAt: Date = Date()
)
