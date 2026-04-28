package com.monied.budgetapp.data.database

import androidx.room.TypeConverter
import com.monied.budgetapp.data.model.AlertSeverity
import com.monied.budgetapp.data.model.AlertType
import java.util.Date

/**
 * Type converters for Room Database
 */
class Converters {

    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun fromAlertType(value: AlertType?): String? {
        return value?.name
    }

    @TypeConverter
    fun toAlertType(value: String?): AlertType? {
        return value?.let { AlertType.valueOf(it) }
    }

    @TypeConverter
    fun fromAlertSeverity(value: AlertSeverity?): String? {
        return value?.name
    }

    @TypeConverter
    fun toAlertSeverity(value: String?): AlertSeverity? {
        return value?.let { AlertSeverity.valueOf(it) }
    }
}
