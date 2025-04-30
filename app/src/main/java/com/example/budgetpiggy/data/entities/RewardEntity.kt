package com.example.budgetpiggy.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName   = "rewards",
    foreignKeys = [
        ForeignKey(
            entity        = UserEntity::class,
            parentColumns = ["userId"],
            childColumns  = ["userId"],
            onDelete      = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("userId"),
        Index("code")
    ]
)
data class RewardEntity(
    @PrimaryKey val rewardId: String,
    val userId: String,
    val code: String? = null,
    val rewardName: String,
    val unlockedAt: Long
)
