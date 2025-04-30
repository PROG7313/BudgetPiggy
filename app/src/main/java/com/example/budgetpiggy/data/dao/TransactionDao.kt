package com.example.budgetpiggy.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.budgetpiggy.data.entities.TransactionEntity

@Dao
interface TransactionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: TransactionEntity)

    @Query("SELECT * FROM transactions WHERE transactionId = :id")
    suspend fun getById(id: String): TransactionEntity?

    @Query("SELECT * FROM transactions WHERE userId = :userId")
    suspend fun getByUserId(userId: String): List<TransactionEntity>

    // Returns raw amounts for positive transactions (earnings) this month
    @Query("""
        SELECT amount
          FROM transactions
         WHERE userId = :userId
           AND amount > 0
           AND date BETWEEN :startTimestamp AND :endTimestamp
    """)
    suspend fun getMonthlyEarnings(
        userId: String,
        startTimestamp: Long,
        endTimestamp: Long
    ): List<Double>

    // Returns raw amounts for negative transactions (spending) this month
    @Query("""
        SELECT amount
          FROM transactions
         WHERE userId = :userId
           AND amount < 0
           AND date BETWEEN :startTimestamp AND :endTimestamp
    """)
    suspend fun getMonthlySpending(
        userId: String,
        startTimestamp: Long,
        endTimestamp: Long
    ): List<Double>

    // Summed spending (negative amounts made positive) for the month
    @Query("""
        SELECT SUM(-amount)
          FROM transactions
         WHERE userId = :userId
           AND amount < 0
           AND date BETWEEN :startTimestamp AND :endTimestamp
    """)
    suspend fun sumMonthlySpending(
        userId: String,
        startTimestamp: Long,
        endTimestamp: Long
    ): Double

    // Summed earnings for the month
    @Query("""
        SELECT SUM(amount)
          FROM transactions
         WHERE userId = :userId
           AND amount > 0
           AND date BETWEEN :startTimestamp AND :endTimestamp
    """)
    suspend fun sumMonthlyEarnings(
        userId: String,
        startTimestamp: Long,
        endTimestamp: Long
    ): Double

    // Aggregated spending by category
    @Query("""
        SELECT c.categoryName AS category, SUM(t.amount) AS total
          FROM transactions t
          INNER JOIN categories c ON t.categoryId = c.categoryId
         WHERE t.userId = :userId
           AND t.amount < 0
         GROUP BY t.categoryId
    """)
    suspend fun getSpendingByCategory(userId: String): List<CategorySpendingView>

    @Update
    suspend fun update(transaction: TransactionEntity)

    @Delete
    suspend fun delete(transaction: TransactionEntity)

    data class CategorySpendingView(
        val category: String,
        val total: Double
    )
}
