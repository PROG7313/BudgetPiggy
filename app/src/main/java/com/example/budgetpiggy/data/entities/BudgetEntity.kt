package com.example.budgetpiggy.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "budgets",
    foreignKeys = [
        ForeignKey(entity = UserEntity::class, parentColumns = ["userId"], childColumns = ["userId"]),
        ForeignKey(entity = AccountEntity::class, parentColumns = ["accountId"], childColumns = ["accountId"]),
        ForeignKey(entity = CategoryEntity::class, parentColumns = ["categoryId"], childColumns = ["categoryId"])
    ],
    indices = [Index("userId"), Index("accountId"), Index("categoryId")]
)
data class BudgetEntity(
    @PrimaryKey val budgetId: String,
    val userId: String,
    val categoryId: String,
    val accountId: String,
    val limit: Double,
    val spent: Double,
    val createdAt: Long
)
