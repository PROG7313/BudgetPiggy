package com.example.budgetpiggy.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName   = "notifications",
    foreignKeys = [
        ForeignKey(
            entity        = UserEntity::class,
            parentColumns = ["userId"],
            childColumns  = ["userId"],
            onDelete      = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity        = RewardCodeEntity::class,
            parentColumns = ["code"],
            childColumns  = ["rewardCodeId"],
            onDelete      = ForeignKey.SET_NULL
        )
    ],
    indices = [ Index("userId"), Index("rewardCodeId") ]
)
data class NotificationEntity(
    @PrimaryKey val notificationId: String,
    val userId: String,
    val message: String,
    val timestamp: Long,
    val isRead: Boolean = false,
    val iconUrl: String?      = null,
    val rewardCodeId: String? = null
)
