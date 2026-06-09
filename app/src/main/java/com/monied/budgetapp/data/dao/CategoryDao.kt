package com.monied.budgetapp.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.monied.budgetapp.data.model.Category

/**
 * Data Access Object for Category operations
 */
@Dao
interface CategoryDao {

    @Query("SELECT * FROM categories ORDER BY name ASC")
    fun getAllCategories(): LiveData<List<Category>>

    @Query("SELECT * FROM categories ORDER BY name ASC")
    suspend fun getAllCategoriesSync(): List<Category>

    @Query("SELECT * FROM categories WHERE id = :categoryId")
    fun getCategoryById(categoryId: Long): LiveData<Category>

    @Query("SELECT * FROM categories WHERE id = :categoryId")
    suspend fun getCategoryByIdSync(categoryId: Long): Category?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: Category): Long

    @Update
    suspend fun updateCategory(category: Category)

    @Delete
    suspend fun deleteCategory(category: Category)

    @Query("UPDATE categories SET expenseCount = (SELECT COUNT(*) FROM expenses WHERE categoryId = :categoryId) WHERE id = :categoryId")
    suspend fun updateExpenseCount(categoryId: Long)
}
