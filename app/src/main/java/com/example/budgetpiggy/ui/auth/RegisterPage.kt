package com.example.budgetpiggy.ui.auth

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.budgetpiggy.ui.home.HomePage
import com.example.budgetpiggy.R
import com.example.budgetpiggy.data.database.AppDatabase
import com.example.budgetpiggy.data.entities.NotificationEntity
import com.example.budgetpiggy.data.entities.UserEntity
import com.example.budgetpiggy.utils.PasswordUtils
import com.example.budgetpiggy.utils.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class RegisterPage : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.register)

        // handle system-bar insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.RegisterPage)) { v, insets ->
            val sb = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(sb.left, sb.top, sb.right, sb.bottom)
            insets
        }

        // view references
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

        // back arrow animation
        backArrow.setOnClickListener { v ->
            v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(25)
                .withEndAction {
                    v.animate().scaleX(1f).scaleY(1f).setDuration(25).start()
                    onBackPressedDispatcher.onBackPressed()
                }.start()
        }

        // toggle password visibility
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

        // floating labels helper
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

                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            })
        }

        // attach floating labels
        setupFloatingLabel(firstNameEdit, firstNameLabel, getString(R.string.first_name))
        setupFloatingLabel(lastNameEdit, lastNameLabel, getString(R.string.last_name))
        setupFloatingLabel(emailEdit, emailLabel, getString(R.string.email_address))
        setupFloatingLabel(passwordEdit, passwordLabel, getString(R.string.password))

        // signup button
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

                    if (first.isBlank() || last.isBlank() ||
                        email.isBlank() || pwd.isBlank()
                    ) {
                        Toast.makeText(
                            this,
                            "Please fill in all fields",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@withEndAction
                    }

                    // create and save user
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

                    lifecycleScope.launch(Dispatchers.IO) {
                        val userDao = AppDatabase.getDatabase(this@RegisterPage).userDao()
                        val existingUser = userDao.getUserByEmail(email)

                        if (existingUser != null) {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(
                                    this@RegisterPage,
                                    "An account with this email already exists.",
                                    Toast.LENGTH_SHORT
                                ).show()
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
                            passwordHash = pwd
                        )

                        userDao.insert(user)

                        // only welcome once
                        val prefs = getSharedPreferences("app_piggy_prefs", MODE_PRIVATE)
                        if (!prefs.getBoolean("hasWelcomed", false)) {
                            // build your welcome notification
                            val welcome = NotificationEntity(
                                notificationId = UUID.randomUUID().toString(),
                                userId         = newUserId,
                                message        = "🎉 Welcome to Budget Piggy!",
                                timestamp      = System.currentTimeMillis(),
                                isRead         = false,
                                iconUrl        = null,
                                rewardCodeId   = null
                            )
                            // insert it
                            AppDatabase.getDatabase(this@RegisterPage)
                                .notificationDao()
                                .insert(welcome)

                            // don’t do it again
                            prefs.edit()
                                .putBoolean("hasWelcomed", true)
                                .apply()
                        }


                        // save logged_in_user_id
                        // persist the logged in user id to the shared preferences
                        SessionManager.saveUserId(this@RegisterPage, newUserId)

                        withContext(Dispatchers.Main) {
                            val intent = Intent(this@RegisterPage, HomePage::class.java).apply {
                                flags =
                                    Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            }
                            startActivity(intent)
                            finish()
                        }
                    }

                }

            // login redirect
            loginRedirectText.setOnClickListener { v ->
                v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(25)
                    .withEndAction {
                        v.animate().scaleX(1f).scaleY(1f).setDuration(25).start()
                        startActivity(Intent(this, LoginPage::class.java))
                    }.start()
            }
        }
    }
}
