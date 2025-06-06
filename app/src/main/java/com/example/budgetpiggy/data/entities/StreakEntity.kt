package com.example.budgetpiggy.data

import androidx.room.*
import com.example.budgetpiggy.data.entities.UserEntity
import java.time.LocalDate

@Entity(
    tableName = "streak",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["userId"],
            childColumns = ["userOwnerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["userOwnerId"])]
)
data class StreakEntity(
    @PrimaryKey(autoGenerate = true) val streakId: Int = 0,
    @ColumnInfo(name = "userOwnerId") val userId: Int,
    val lastActiveDate: LocalDate,
    val streakCount: Int
)