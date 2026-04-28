package com.monied.budgetapp.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.monied.budgetapp.data.model.BudgetAlert
import com.monied.budgetapp.data.model.AlertSeverity

/**
 * Data Access Object for Budget Alert operations
 */
@Dao
interface BudgetAlertDao {

    @Query("SELECT * FROM budget_alerts WHERE isDismissed = 0 ORDER BY createdAt DESC")
    fun getActiveAlerts(): LiveData<List<BudgetAlert>>

    @Query("SELECT * FROM budget_alerts ORDER BY createdAt DESC")
    fun getAllAlerts(): LiveData<List<BudgetAlert>>

    @Query("SELECT COUNT(*) FROM budget_alerts WHERE isDismissed = 0")
    fun getActiveAlertCount(): LiveData<Int>

    @Query("SELECT COUNT(*) FROM budget_alerts WHERE isDismissed = 0 AND severity = :severity")
    fun getAlertCountBySeverity(severity: AlertSeverity): LiveData<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlert(alert: BudgetAlert): Long

    @Update
    suspend fun updateAlert(alert: BudgetAlert)

    @Query("UPDATE budget_alerts SET isDismissed = 1 WHERE id = :alertId")
    suspend fun dismissAlert(alertId: Long)

    @Query("DELETE FROM budget_alerts WHERE isDismissed = 1")
    suspend fun deleteDismissedAlerts()
}
