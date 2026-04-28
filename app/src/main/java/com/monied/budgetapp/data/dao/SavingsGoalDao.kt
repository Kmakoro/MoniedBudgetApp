package com.monied.budgetapp.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.monied.budgetapp.data.model.SavingsGoal

/**
 * Data Access Object for Savings Goal operations
 */
@Dao
interface SavingsGoalDao {

    @Query("SELECT * FROM savings_goals ORDER BY deadline ASC")
    fun getAllSavingsGoals(): LiveData<List<SavingsGoal>>

    @Query("SELECT * FROM savings_goals WHERE id = :goalId")
    fun getSavingsGoalById(goalId: Long): LiveData<SavingsGoal>

    @Query("SELECT SUM(currentAmount) FROM savings_goals")
    fun getTotalSavings(): LiveData<Double>

    @Query("SELECT SUM(targetAmount) FROM savings_goals")
    fun getTotalTarget(): LiveData<Double>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavingsGoal(savingsGoal: SavingsGoal): Long

    @Update
    suspend fun updateSavingsGoal(savingsGoal: SavingsGoal)

    @Delete
    suspend fun deleteSavingsGoal(savingsGoal: SavingsGoal)

    @Query("UPDATE savings_goals SET currentAmount = currentAmount + :amount WHERE id = :goalId")
    suspend fun addMoneyToGoal(goalId: Long, amount: Double)
}
