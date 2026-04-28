package com.monied.budgetapp.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.monied.budgetapp.data.model.BudgetGoal

/**
 * Data Access Object for Budget Goal operations
 */
@Dao
interface BudgetGoalDao {

    @Query("SELECT * FROM budget_goals WHERE month = :month LIMIT 1")
    fun getBudgetGoalByMonth(month: String): LiveData<BudgetGoal?>

    @Query("SELECT * FROM budget_goals WHERE month = :month LIMIT 1")
    suspend fun getBudgetGoalByMonthSync(month: String): BudgetGoal?

    @Query("SELECT * FROM budget_goals ORDER BY month DESC")
    fun getAllBudgetGoals(): LiveData<List<BudgetGoal>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudgetGoal(budgetGoal: BudgetGoal): Long

    @Update
    suspend fun updateBudgetGoal(budgetGoal: BudgetGoal)

    @Delete
    suspend fun deleteBudgetGoal(budgetGoal: BudgetGoal)
}
