package com.example.budgetpiggy.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "transfers",
    foreignKeys = [ForeignKey(entity = UserEntity::class, parentColumns = ["userId"], childColumns = ["userId"])],
    indices = [Index("userId")]
)
data class TransferEntity(
    @PrimaryKey val transferId: String,
    val userId: String,
    val fromAccountId: String?,
    val toAccountId: String?,
    val fromCategoryId: String?,
    val toCategoryId: String?,
    val amount: Double,
    val date: Long
)
