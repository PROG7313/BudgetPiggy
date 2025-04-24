package com.example.budgetpiggy.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "rewards",
    foreignKeys = [ForeignKey(entity = UserEntity::class, parentColumns = ["userId"], childColumns = ["userId"])],
    indices = [Index("userId")]
)
data class RewardEntity(
    @PrimaryKey val rewardId: String,
    val userId: String,
    val rewardName: String,
    val rewardImageUrl: String? = null,      // Firebase URL
    val rewardLocalPath: String? = null,     // Local file path
    val codeUsed: String,
    val unlocked: Boolean
)
