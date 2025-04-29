package com.example.budgetpiggy.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.budgetpiggy.data.entities.CategoryEntity

@Dao
interface CategoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(category: CategoryEntity)

    @Query("SELECT * FROM categories WHERE categoryId = :id")
    suspend fun getById(id: String): CategoryEntity?

    @Query("SELECT * FROM categories WHERE userId = :userId")
    suspend fun getByUserId(userId: String): List<CategoryEntity>

    @Query("UPDATE categories SET budgetAmount = budgetAmount + :amount WHERE categoryId = :categoryId")
    suspend fun addToBudget(categoryId: String, amount: Double)

    @Query("UPDATE categories SET budgetAmount = budgetAmount - :amount WHERE categoryId = :categoryId")
    suspend fun subtractFromBudget(categoryId: String, amount: Double)

    @Update
    suspend fun update(category: CategoryEntity)

    @Delete
    suspend fun delete(category: CategoryEntity)

}
