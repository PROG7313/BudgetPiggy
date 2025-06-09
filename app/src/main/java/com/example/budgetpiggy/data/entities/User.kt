package com.example.budgetpiggy.data.entities

data class User(
    val uid: String = "",
    val fullName: String = "",
    val email: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
