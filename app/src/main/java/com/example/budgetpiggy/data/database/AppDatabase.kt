package com.example.budgetpiggy.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.budgetpiggy.data.Converters
import com.example.budgetpiggy.data.StreakDao
import com.example.budgetpiggy.data.StreakEntity
import com.example.budgetpiggy.data.dao.*
import com.example.budgetpiggy.data.entities.*

@Database(
    entities = [
        UserEntity::class,
        AccountEntity::class,
        CategoryEntity::class,
        TransactionEntity::class,
        BudgetEntity::class,
        TransferEntity::class,
        RewardEntity::class,
        RewardCodeEntity::class,
        NotificationEntity::class,
        SavingsGoalEntity::class,
        StreakEntity::class,
        RateEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun accountDao(): AccountDao
    abstract fun categoryDao(): CategoryDao
    abstract fun transactionDao(): TransactionDao
    abstract fun budgetDao(): BudgetDao
    abstract fun transferDao(): TransferDao
    abstract fun rewardDao(): RewardDao
    abstract fun rewardCodeDao(): RewardCodeDao
    abstract fun notificationDao(): NotificationDao
    abstract fun savingsGoalDao(): SavingsGoalDao
    abstract fun streakDao(): StreakDao
    abstract fun rateDao(): RateDao
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            //  DEV-ONLY: Wipe the old DB before building your Room instance
           //context.applicationContext.deleteDatabase("budget_piggy_db")

            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "budget_piggy_db"
                )
                  //  .fallbackToDestructiveMigration() // keeps your schema version at 3
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }

}

