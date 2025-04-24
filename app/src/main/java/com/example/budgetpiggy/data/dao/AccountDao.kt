package com.example.budgetpiggy.data.dao
import androidx.room.*

import com.example.budgetpiggy.data.entities.AccountEntity

@Dao
interface AccountDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(account: AccountEntity)

    @Query("SELECT * FROM accounts WHERE accountId = :id")
    suspend fun getById(id: String): AccountEntity?

    @Query("SELECT * FROM accounts WHERE userId = :userId")
    suspend fun getByUserId(userId: String): List<AccountEntity>

    @Query("SELECT accountName, balance FROM accounts WHERE userId = :userId")
    suspend fun getBalancesForUser(userId: String): List<AccountBalanceView>


    @Update
    suspend fun update(account: AccountEntity)

    @Delete
    suspend fun delete(account: AccountEntity)





    data class AccountBalanceView(
        val accountName: String,
        val balance: Double
    )


}

