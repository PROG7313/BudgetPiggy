package com.example.budgetpiggy

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.budgetpiggy.data.dao.AccountDao
import com.example.budgetpiggy.data.dao.UserDao
import com.example.budgetpiggy.data.database.AppDatabase
import com.example.budgetpiggy.data.entities.AccountEntity
import com.example.budgetpiggy.data.entities.UserEntity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DatabaseTest {

    private lateinit var db: AppDatabase
    private lateinit var userDao: UserDao
    private lateinit var accountDao: AccountDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()

        userDao = db.userDao()
        accountDao = db.accountDao()
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun testUserAccountInsertAndQuery() = runBlocking {
        val testUser = UserEntity(
            userId = "user1",
            firstName = "Jane",
            lastName = "Doe",
            email = "jane@example.com",
            currency = "ZAR",
            authProvider = "email",
            profilePictureUrl = null,
            profilePictureLocalPath = null,
            passwordHash = "hashed_pass"
        )

        userDao.insertUser(testUser)
        val retrievedUser = userDao.getUserById("user1")

        assertNotNull(retrievedUser)
        assertEquals("Jane", retrievedUser?.firstName)

        val testAccount = AccountEntity(
            accountId = "acc1",
            userId = "user1",
            accountName = "Savings",
            balance = 1000.0,
            type = "Bank",
            createdAt = System.currentTimeMillis()
        )

        accountDao.insertAccount(testAccount)
        val accounts = accountDao.getAccountsByUser("user1")

        assertEquals(1, accounts.size)
        assertEquals("Savings", accounts[0].accountName)

        // Now delete it and verify it's gone
        accountDao.deleteAccount(testAccount)
        val accountsAfterDelete = accountDao.getAccountsByUser("user1")
        assertTrue(accountsAfterDelete.isEmpty())
    }
}