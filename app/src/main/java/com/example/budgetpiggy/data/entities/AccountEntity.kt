package com.example.budgetpiggy.data.entities
import androidx.room.*

@Entity(
    tableName = "accounts",
    foreignKeys = [ForeignKey(
        entity = UserEntity::class,
        parentColumns = ["userId"],
        childColumns = ["userId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("userId")]
)
data class AccountEntity(
    @PrimaryKey val accountId: String,
    val userId: String,
    val accountName: String,
    var balance: Double,         // remaining
    var initialBalance: Double,  // total
    val type: String,
    val createdAt: Long
)
