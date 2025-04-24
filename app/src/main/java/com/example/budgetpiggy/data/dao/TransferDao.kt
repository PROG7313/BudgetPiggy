package com.example.budgetpiggy.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.budgetpiggy.data.entities.TransferEntity

@Dao
interface TransferDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transfer: TransferEntity)

    @Query("SELECT * FROM transfers WHERE transferId = :id")
    suspend fun getById(id: String): TransferEntity?

    @Query("SELECT * FROM transfers WHERE userId = :userId")
    suspend fun getByUserId(userId: String): List<TransferEntity>

    @Update
    suspend fun update(transfer: TransferEntity)

    @Delete
    suspend fun delete(transfer: TransferEntity)
}
