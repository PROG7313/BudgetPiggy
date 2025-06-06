package com.example.budgetpiggy.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "currency_rates")
data class RateEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val baseCode: String,
    val targetCode: String,
    val rate: Float
)
