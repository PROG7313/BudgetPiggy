package com.example.budgetpiggy.data.dao
import androidx.room.*
import com.example.budgetpiggy.data.entities.UserEntity

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Query("SELECT * FROM users WHERE userId = :id")
    suspend fun getUserById(id: String): UserEntity?

    @Query("SELECT * FROM users")
    suspend fun getAllUsers(): List<UserEntity>
    @Delete
    suspend fun deleteUser(user: UserEntity)
}
