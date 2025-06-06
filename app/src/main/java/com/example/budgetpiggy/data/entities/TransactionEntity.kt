package com.example.budgetpiggy.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["userId"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = AccountEntity::class,
            parentColumns = ["accountId"],
            childColumns = ["accountId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["categoryId"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("userId"), Index("accountId"), Index("categoryId")]
)
data class TransactionEntity(
    @PrimaryKey val transactionId: String,
    val userId: String,
    val accountId: String,
    val categoryId: String?,  // Nullable in case you ever want to SET_NULL instead
    val amount: Double,
    val description: String?,
    val date: Long,
    val receiptImageUrl: String? = null,
    val receiptLocalPath: String? = null
)

