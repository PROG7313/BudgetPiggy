package com.example.budgetpiggy.devtools

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.budgetpiggy.R
import com.example.budgetpiggy.data.database.AppDatabase
import com.example.budgetpiggy.data.entities.UserEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TestActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)

        db = AppDatabase.getDatabase(applicationContext)

        findViewById<Button>(R.id.btnInsertUser).setOnClickListener { insertTestUser() }
        findViewById<Button>(R.id.btnGetUser).setOnClickListener { getUserById() }
        findViewById<Button>(R.id.btnUpdateUser).setOnClickListener { updateUser() }
        findViewById<Button>(R.id.btnDeleteUser).setOnClickListener { deleteUser() }
        findViewById<Button>(R.id.btnGetAllUsers).setOnClickListener { getAllUsers() }
    }

    private fun insertTestUser() {
        CoroutineScope(Dispatchers.IO).launch {
            val user = UserEntity(
                userId = "user123",
                firstName = "Test",
                lastName = "User",
                email = "test@example.com",
                currency = "ZAR",
                authProvider = "email",
                profilePictureUrl = null,
                profilePictureLocalPath = null,
                passwordHash = "hash123"
            )
            db.userDao().insertUser(user)
            showToast("User inserted!")
        }
    }

    private fun getUserById() {
        CoroutineScope(Dispatchers.IO).launch {
            val user = db.userDao().getUserById("user123")
            showToast(user?.firstName ?: "User not found")
        }
    }

    private fun updateUser() {
        CoroutineScope(Dispatchers.IO).launch {
            val existing = db.userDao().getUserById("user123")
            if (existing != null) {
                val updated = existing.copy(firstName = "Updated")
                db.userDao().insertUser(updated)
                showToast("User updated")
            } else {
                showToast("User not found")
            }
        }
    }

    private fun deleteUser() {
        CoroutineScope(Dispatchers.IO).launch {
            val user = db.userDao().getUserById("user123")
            if (user != null) {
                db.userDao().deleteUser(user)
                showToast("User deleted")
            } else {
                showToast("User not found")
            }
        }
    }

    private fun getAllUsers() {
        CoroutineScope(Dispatchers.IO).launch {
            val users = db.userDao().getAllUsers()

            val userDetails = users.joinToString("\n") { user ->
                "ID: ${user.userId}, Name: ${user.firstName} ${user.lastName}, Email: ${user.email}"
            }

            withContext(Dispatchers.Main) {
                showToast(userDetails)
            }
        }
    }


    private fun showToast(message: String) {
        runOnUiThread {
            Toast.makeText(this@TestActivity, message, Toast.LENGTH_SHORT).show()
        }
    }
}
