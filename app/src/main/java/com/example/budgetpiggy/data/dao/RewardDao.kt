package com.example.budgetpiggy.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.budgetpiggy.data.entities.RewardEntity

@Dao
interface RewardDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(reward: RewardEntity)

    @Query("SELECT * FROM rewards WHERE userId = :userId")
    suspend fun getByUserId(userId: String): List<RewardEntity>
}
