package com.example.budgetpiggy.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey val categoryId: String,
    val userId: String,
    val categoryName: String,
    val linkedAccountType: String,    
    val budgetAmount: Double,
    val iconName: String?,
    val iconUrl: String?,
    val iconLocalPath: String?,
    val createdAt: Long
)
