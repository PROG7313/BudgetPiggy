package com.example.budgetpiggy.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.budgetpiggy.data.entities.RewardCodeEntity

@Dao
interface RewardCodeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(code: RewardCodeEntity)

    @Query("SELECT * FROM reward_codes WHERE code = :code")
    suspend fun getByCode(code: String): RewardCodeEntity?

    @Query("SELECT * FROM reward_codes")
    suspend fun getAll(): List<RewardCodeEntity>

    @Update
    suspend fun update(code: RewardCodeEntity)

    @Delete
    suspend fun delete(code: RewardCodeEntity)
}
