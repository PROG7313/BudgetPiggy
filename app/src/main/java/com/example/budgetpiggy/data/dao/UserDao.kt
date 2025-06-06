package com.example.budgetpiggy.data.dao
import androidx.room.*
import com.example.budgetpiggy.data.entities.UserEntity
@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: UserEntity)

    @Query("SELECT * FROM users WHERE userId = :id")
    suspend fun getById(id: String): UserEntity?

    @Query("SELECT * FROM users")
    suspend fun getAll(): List<UserEntity>
    @Query("SELECT * FROM users WHERE LOWER(email) = LOWER(:email)")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Update
    suspend fun update(user: UserEntity)

    @Delete
    suspend fun delete(user: UserEntity)
}
