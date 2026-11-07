package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expenses ORDER BY timestamp DESC")
    fun getAllExpensesFlow(): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses ORDER BY timestamp DESC")
    suspend fun getAllExpenses(): List<ExpenseEntity>

    @Query("SELECT * FROM expenses ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentExpenses(limit: Int): List<ExpenseEntity>

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM expenses")
    fun getTotalExpenseFlow(): Flow<Double>

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM expenses")
    suspend fun getTotalExpense(): Double

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM expenses WHERE LOWER(category) LIKE '%' || LOWER(:category) || '%' OR LOWER(itemName) LIKE '%' || LOWER(:category) || '%'")
    suspend fun getTotalByCategory(category: String): Double

    @Query("SELECT category, SUM(amount) as totalAmount, COUNT(*) as itemCount FROM expenses GROUP BY category ORDER BY totalAmount DESC")
    fun getCategorySummariesFlow(): Flow<List<CategorySummary>>

    @Query("SELECT category, SUM(amount) as totalAmount, COUNT(*) as itemCount FROM expenses GROUP BY category ORDER BY totalAmount DESC")
    suspend fun getCategorySummaries(): List<CategorySummary>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpenses(expenses: List<ExpenseEntity>): List<Long>

    @Update
    suspend fun updateExpense(expense: ExpenseEntity)

    @Delete
    suspend fun deleteExpense(expense: ExpenseEntity)

    @Query("DELETE FROM expenses")
    suspend fun clearAll()
}
