package com.example.budgetpiggy.data.entities
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val userId: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val authProvider: String,
    val profilePictureUrl: String? = null,
    val profilePictureLocalPath: String? = null,
    val currency: String,
    val passwordHash: String? = null
)

