package com.example.budgetpiggy.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.budgetpiggy.data.entities.NotificationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(notification: NotificationEntity)

    @Query("""
    SELECT *
      FROM notifications
     WHERE userId = :userId
  ORDER BY timestamp DESC
  """)
    fun notificationsFor(userId: String): Flow<List<NotificationEntity>>


    @Query("""
    SELECT * FROM notifications 
    WHERE userId = :userId 
    ORDER BY timestamp DESC
  """)
    fun streamForUser(userId: String): Flow<List<NotificationEntity>>

    @Query("UPDATE notifications SET isRead = 1 WHERE notificationId = :id")
    suspend fun markAsRead(id: String)

    @Query("DELETE FROM notifications WHERE userId = :userId")
    suspend fun clearAll(userId: String)

}
