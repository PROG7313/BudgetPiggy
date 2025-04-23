package com.example.budgetpiggy.data.dao
import androidx.room.*

import com.example.budgetpiggy.data.entities.AccountEntity

@Dao
interface AccountDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: AccountEntity)

    @Query("SELECT * FROM accounts WHERE userId = :userId")
    suspend fun getAccountsByUser(userId: String): List<AccountEntity>

    @Delete
    suspend fun deleteAccount(account: AccountEntity)
}
