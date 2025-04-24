package com.example.budgetpiggy.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "notifications",
    foreignKeys = [ForeignKey(entity = UserEntity::class, parentColumns = ["userId"], childColumns = ["userId"])],
    indices = [Index("userId")]
)
data class NotificationEntity(
    @PrimaryKey val notificationId: String,
    val iconPictureUrl: String? = null,
    val iconPictureLocalPath: String? = null,
    val userId: String,
    val message: String,
    val timestamp: Long,
    val isRead: Boolean
)
