package com.example.budgetpiggy.data

import androidx.room.*

@Dao
interface StreakDao {

    // Insert a new streak record
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStreak(streak: StreakEntity)

    // Update existing streak
    @Update
    suspend fun updateStreak(streak: StreakEntity)

    // Get streak by user ID
    @Query("SELECT * FROM streak WHERE userOwnerId = :userId LIMIT 1")
    suspend fun getStreakByUserId(userId: Int): StreakEntity?

    // Delete streak
    @Delete
    suspend fun deleteStreak(streak: StreakEntity)

    // Optional: Clear all streaks (for debugging/testing)
    @Query("DELETE FROM streak")
    suspend fun clearAll()
}