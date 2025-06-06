package com.example.budgetpiggy.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reward_codes")
data class RewardCodeEntity(
    @PrimaryKey val code: String,
    val rewardName: String,
    val rewardImageUrl: String? = null,
    val rewardImageLocalPath: String?   = null
)
