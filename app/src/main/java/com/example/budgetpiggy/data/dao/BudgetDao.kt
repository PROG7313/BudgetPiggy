package com.example.budgetpiggy.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.budgetpiggy.data.entities.BudgetEntity

@Dao
interface BudgetDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(budget: BudgetEntity)

    @Query("SELECT * FROM budgets WHERE budgetId = :id")
    suspend fun getById(id: String): BudgetEntity?

    @Query("SELECT * FROM budgets WHERE userId = :userId")
    suspend fun getByUserId(userId: String): List<BudgetEntity>

    @Update
    suspend fun update(budget: BudgetEntity)

    @Delete
    suspend fun delete(budget: BudgetEntity)
}
