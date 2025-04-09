package com.example.budgetpiggy.data.database
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.budgetpiggy.data.entities.UserEntity
import com.example.budgetpiggy.data.entities.AccountEntity
import com.example.budgetpiggy.data.dao.UserDao
import com.example.budgetpiggy.data.dao.AccountDao
@Database(
    entities = [UserEntity::class, AccountEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun accountDao(): AccountDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "budget_piggy_db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
