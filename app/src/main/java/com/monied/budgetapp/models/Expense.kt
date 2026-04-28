package com.monied.budgetapp.models

data class Expense(
    val id: Int,
    val amount: Double,
    val description: String,
    val date: String,
    val startTime: String,
    val endTime: String,
    val categoryId: Int,
    val photoUri: String? = null,
    val categoryName: String = "" // For joining with categories table
) {
    val formattedAmount: String get() = "R ${String.format("%.2f", amount)}"
    val formattedDateTime: String get() = "$date at $startTime"

    val formattedDate: String
        get() = formatDate(date)

    val duration: String
        get() = calculateDuration(startTime, endTime)

    private fun formatDate(dateString: String): String {
        return try {
            val inputFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            val outputFormat = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
            val date = inputFormat.parse(dateString)
            outputFormat.format(date ?: java.util.Date())
        } catch (e: Exception) {
            dateString
        }
    }

    private fun calculateDuration(start: String, end: String): String {
        return try {
            val timeFormat = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
            val startTime = timeFormat.parse(start)
            val endTime = timeFormat.parse(end)
            val durationMillis = endTime.time - startTime.time
            val hours = durationMillis / (1000 * 60 * 60)
            val minutes = (durationMillis % (1000 * 60 * 60)) / (1000 * 60)
            when {
                hours > 0 && minutes > 0 -> "${hours}h ${minutes}m"
                hours > 0 -> "${hours}h"
                minutes > 0 -> "${minutes}m"
                else -> "0m"
            }
        } catch (e: Exception) {
            ""
        }
    }
}