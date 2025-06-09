package com.example.budgetpiggy.ui.auth

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.budgetpiggy.R
import com.example.budgetpiggy.data.database.AppDatabase
import com.example.budgetpiggy.data.entities.NotificationEntity
import com.example.budgetpiggy.data.entities.UserEntity
import com.example.budgetpiggy.ui.core.SplashActivity
import com.example.budgetpiggy.utils.PasswordUtils
import com.example.budgetpiggy.utils.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID
import androidx.core.content.edit
import com.example.budgetpiggy.data.repository.RewardRepository
import com.example.budgetpiggy.ui.core.BaseActivity
import com.example.budgetpiggy.ui.notifications.NotificationHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.budgetpiggy.data.entities.User
import com.example.budgetpiggy.ui.home.HomePage

class RegisterPage : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.register)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.RegisterPage)) { v, insets ->
            val sb = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(sb.left, sb.top, sb.right, sb.bottom)
            insets
        }

        val backArrow = findViewById<ImageView>(R.id.backArrow)
        val firstNameEdit = findViewById<EditText>(R.id.firstNameEditText)
        val firstNameLabel = findViewById<TextView>(R.id.firstNameLabel)
        val lastNameEdit = findViewById<EditText>(R.id.lastNameEditText)
        val lastNameLabel = findViewById<TextView>(R.id.lastNameLabel)
        val emailEdit = findViewById<EditText>(R.id.emailEditText)
        val emailLabel = findViewById<TextView>(R.id.emailLabel)
        val passwordEdit = findViewById<EditText>(R.id.passwordEditText)
        val passwordLabel = findViewById<TextView>(R.id.passwordLabel)
        val eyeIcon = findViewById<ImageView>(R.id.eyeIcon)
        val signUpButton = findViewById<Button>(R.id.signUpButton)
        val loginRedirectText = findViewById<TextView>(R.id.loginRedirectText)

        backArrow.setOnClickListener { v ->
            v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(25)
                .withEndAction {
                    v.animate().scaleX(1f).scaleY(1f).setDuration(25).start()
                    onBackPressedDispatcher.onBackPressed()
                }.start()
        }

        var isPwdVisible = false
        eyeIcon.setOnClickListener {
            isPwdVisible = !isPwdVisible
            passwordEdit.transformationMethod = if (isPwdVisible)
                null
            else
                PasswordTransformationMethod.getInstance()
            eyeIcon.setImageResource(
                if (isPwdVisible) R.drawable.vec_eye_closed
                else R.drawable.vec_eye_open
            )
            passwordEdit.setSelection(passwordEdit.text.length)
        }

        fun setupFloatingLabel(editTxt: EditText, label: TextView, hint: String) {
            editTxt.setOnFocusChangeListener { _, focused ->
                if (focused || editTxt.text.isNotEmpty()) {
                    if (label.visibility != View.VISIBLE) {
                        label.alpha = 0f
                        label.visibility = View.VISIBLE
                        label.animate().alpha(1f).setDuration(200).start()
                    }
                    editTxt.hint = ""
                } else if (editTxt.text.isEmpty()) {
                    label.animate()
                        .alpha(0f)
                        .setDuration(200)
                        .withEndAction { label.visibility = View.GONE }
                        .start()
                    editTxt.hint = hint
                }
            }
            editTxt.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    if (s.isNullOrEmpty()) {
                        label.animate()
                            .alpha(0f)
                            .setDuration(150)
                            .withEndAction { label.visibility = View.GONE }
                            .start()
                        editTxt.hint = hint
                    } else if (label.visibility != View.VISIBLE) {
                        label.alpha = 0f
                        label.visibility = View.VISIBLE
                        label.animate().alpha(1f).setDuration(150).start()
                        editTxt.hint = ""
                    }
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            })
        }

        setupFloatingLabel(firstNameEdit, firstNameLabel, getString(R.string.first_name))
        setupFloatingLabel(lastNameEdit, lastNameLabel, getString(R.string.last_name))
        setupFloatingLabel(emailEdit, emailLabel, getString(R.string.email_address))
        setupFloatingLabel(passwordEdit, passwordLabel, getString(R.string.password))

        signUpButton.setOnClickListener { v ->
            v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(25)
                .withEndAction {
                    v.animate().scaleX(1f).scaleY(1f).setDuration(25).start()

                    val first = firstNameEdit.text.toString().trim()
                    val last = lastNameEdit.text.toString().trim()
                    val email = emailEdit.text.toString().trim()
                    val pwd = passwordEdit.text.toString()

                    if (pwd.length < 8 || !pwd.any { it.isDigit() } || !pwd.any { it.isUpperCase() }) {
                        Toast.makeText(this, "Password must be at least 8 characters, contain a digit and an uppercase letter.", Toast.LENGTH_LONG).show()
                        return@withEndAction
                    }

                    if (first.isBlank() || last.isBlank() || email.isBlank() || pwd.isBlank()) {
                        Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                        return@withEndAction
                    }

                    val fullName = "$first $last"

                    // Firebase Registration
                    registerUser(email, pwd, fullName) { success, errorMessage ->
                        if (!success) {
                            Toast.makeText(this, "Registration failed: $errorMessage", Toast.LENGTH_LONG).show()
                        }
                    }
                    // Old RoomDB logic
                    lifecycleScope.launch(Dispatchers.IO) {
                        val userDao = AppDatabase.getDatabase(this@RegisterPage).userDao()
                        val existingUser = userDao.getUserByEmail(email)

                        if (existingUser != null) {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(this@RegisterPage, "An account with this email already exists.", Toast.LENGTH_SHORT).show()
                            }
                            return@launch
                        }

                        val newUserId = UUID.randomUUID().toString()
                        val user = UserEntity(
                            userId = newUserId,
                            firstName = first,
                            lastName = last,
                            email = email,
                            authProvider = "email_password",
                            profilePictureUrl = null,
                            profilePictureLocalPath = null,
                            currency = "ZAR",
                            passwordHash = PasswordUtils.hashPassword(pwd)
                        )
                        userDao.insert(user)

                        val prefs = getSharedPreferences("app_piggy_prefs", MODE_PRIVATE)
                        val welcomeKey = "hasWelcomed_$newUserId"

                        if (!prefs.getBoolean(welcomeKey, false)) {
                            val db = AppDatabase.getDatabase(this@RegisterPage)
                            val rewardRepo = RewardRepository(
                                rewardDao = db.rewardDao(),
                                codeDao = db.rewardCodeDao(),
                                notifDao = db.notificationDao()
                            )

                            rewardRepo.unlockCode(userId = newUserId, code = "SIGNUP2025")

                            NotificationHelper.sendNotification(
                                context = this@RegisterPage,
                                title = "🎁 Reward Unlocked!",
                                message = "You just unlocked WELCOME BONUS reward!"
                            )

                            val welcomeNotif = NotificationEntity(
                                notificationId = UUID.randomUUID().toString(),
                                userId = newUserId,
                                message = "🎉 Welcome to Budget Piggy! Let’s get started.",
                                timestamp = System.currentTimeMillis(),
                                isRead = false,
                                iconUrl = "android.resource://${packageName}/${R.drawable.ic_welcome_bonus}",
                                rewardCodeId = null
                            )
                            db.notificationDao().insert(welcomeNotif)

                            prefs.edit().putBoolean(welcomeKey, true).apply()
                        }

                        SessionManager.saveUserId(this@RegisterPage, newUserId)
                        getSharedPreferences("app_piggy_prefs", MODE_PRIVATE)
                            .edit {
                                putBoolean("needs_getting_started", true)
                            }

                        withContext(Dispatchers.Main) {
                            val intent = Intent(this@RegisterPage, SplashActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            }
                            startActivity(intent)
                            finish()
                        }
                    }
                }

            loginRedirectText.setOnClickListener { v ->
                v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(25)
                    .withEndAction {
                        v.animate().scaleX(1f).scaleY(1f).setDuration(25).start()
                        startActivity(Intent(this, LoginPage::class.java))
                    }.start()
            }
        }
    }

    fun registerUser(email: String, password: String, fullName: String, onResult: (Boolean, String?) -> Unit) {
        val auth = FirebaseAuth.getInstance()
        val firestore = FirebaseFirestore.getInstance()

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val uid = task.result?.user?.uid ?: return@addOnCompleteListener
                    val user = User(uid, fullName, email)

                    firestore.collection("users")
                        .document(uid)
                        .set(user)
                        .addOnSuccessListener {
                            // Save session
                            SessionManager.saveFirebaseId(this@RegisterPage, uid)
                            onResult(true, null)
                        }
                        .addOnFailureListener { e ->
                            onResult(false, e.message)
                        }
                } else {
                    onResult(false, task.exception?.message)
                }
            }
    }
}


