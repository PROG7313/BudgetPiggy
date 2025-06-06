package com.example.budgetpiggy.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.budgetpiggy.data.entities.RateEntity

@Dao
interface RateDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(rates: List<RateEntity>)

    @Query("SELECT * FROM currency_rates WHERE baseCode = :base")
    suspend fun getRatesForBase(base: String): List<RateEntity>

    @Query("SELECT rate FROM currency_rates WHERE baseCode = :base AND targetCode = :target")
    suspend fun getRate(base: String, target: String): Float?
}
