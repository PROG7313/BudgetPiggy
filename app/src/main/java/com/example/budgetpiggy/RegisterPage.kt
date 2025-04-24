package com.example.budgetpiggy


import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import com.example.budgetpiggy.data.database.AppDatabase
import com.example.budgetpiggy.data.entities.UserEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class RegisterPage : AppCompatActivity() {
    @OptIn(UnstableApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.register)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.RegisterPage)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val backArrow = findViewById<ImageView>(R.id.backArrow)
        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)
        val passwordLabel = findViewById<TextView>(R.id.passwordLabel)
        val emailEditText = findViewById<EditText>(R.id.emailEditText)
        val emailLabel = findViewById<TextView>(R.id.emailLabel)
        val firstNameLabel = findViewById<TextView>(R.id.firstNameLabel)
        val firstNameEditText = findViewById<EditText>(R.id.firstNameEditText)
        val lastNameLabel = findViewById<TextView>(R.id.lastNameLabel)
        val lastNameEditText = findViewById<EditText>(R.id.lastNameEditText)
        val eyeIcon = findViewById<ImageView>(R.id.eyeIcon)
        val loginRedirectText = findViewById<TextView>(R.id.loginRedirectText)
        val signUpButton = findViewById<Button>(R.id.signUpButton)
        backArrow.setOnClickListener {
                view ->


            view.animate()
                .scaleX(0.95f)
                .scaleY(0.95f)
                .setDuration(25)
                .withEndAction {
                    view.animate().scaleX(1f).scaleY(1f).setDuration(25).start()
                    onBackPressedDispatcher.onBackPressed()
                }.start()

        //    overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        }
        signUpButton.setOnClickListener { view ->
            view.animate()
                .scaleX(0.95f)
                .scaleY(0.95f)
                .setDuration(25)
                .withEndAction {
                    view.animate().scaleX(1f).scaleY(1f).setDuration(25).start()

                    val firstName = firstNameEditText.text.toString().trim()
                    val lastName = lastNameEditText.text.toString().trim()
                    val email = emailEditText.text.toString().trim()
                    val password = passwordEditText.text.toString()

                    if (firstName.isBlank() || lastName.isBlank() || email.isBlank() || password.isBlank()) {
                        Log.e("REGISTER", "❌ One or more fields are empty.")
                        return@withEndAction
                    }

                    val userId = UUID.randomUUID().toString()
                    val user = UserEntity(
                        userId = userId,
                        firstName = firstName,
                        lastName = lastName,
                        email = email,
                        authProvider = "email_password",
                        profilePictureUrl = null,
                        profilePictureLocalPath = null,
                        currency = "ZAR",
                        passwordHash = password
                    )

                    lifecycleScope.launch(Dispatchers.IO) {
                        try {
                            val db = AppDatabase.getDatabase(this@RegisterPage)
                            db.userDao().insert(user)
                            Log.d("REGISTER", " User inserted successfully: $user")

                            withContext(Dispatchers.Main) {
                                // Redirect to LoginPage after successful registration
                                startActivity(Intent(this@RegisterPage, LoginPage::class.java))
                                finish()
                            }
                        } catch (e: Exception) {
                            Log.e("REGISTER", " Insert failed", e)
                        }
                    }
                }.start()
        }


        loginRedirectText.setOnClickListener {
                view ->


            view.animate()
                .scaleX(0.95f)
                .scaleY(0.95f)
                .setDuration(25)
                .withEndAction {
                    view.animate().scaleX(1f).scaleY(1f).setDuration(25).start()
                    startActivity(Intent(this, LoginPage::class.java))
                }.start()

        }

        var isPasswordVisible = false
        eyeIcon.setOnClickListener {
            isPasswordVisible = !isPasswordVisible

            if (isPasswordVisible) {
                // Show password
                passwordEditText.transformationMethod = null
                eyeIcon.setImageResource(R.drawable.vec_eye_closed) // Switch icon
            } else {
                // Hide password
                passwordEditText.transformationMethod = PasswordTransformationMethod.getInstance()
                eyeIcon.setImageResource(R.drawable.vec_eye_open) // Switch icon
            }

            // Move cursor to the end
            passwordEditText.setSelection(passwordEditText.text.length)
        }
        fun setupFloatingLabel(editText: EditText, label: TextView, hintText: String) {
            editText.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus || editText.text.isNotEmpty()) {
                    if (label.visibility != View.VISIBLE) {
                        label.alpha = 0f
                        label.visibility = View.VISIBLE
                        label.animate().alpha(1f).setDuration(400).start()
                    }
                    editText.hint = ""
                } else {
                    if (editText.text.isEmpty()) {
                        label.animate()
                            .alpha(0f)
                            .setDuration(400)
                            .withEndAction { label.visibility = View.GONE }
                            .start()
                        editText.hint = hintText
                    }
                }
            }

            editText.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    if (s.isNullOrEmpty()) {
                        label.animate()
                            .alpha(0f)
                            .setDuration(150)
                            .withEndAction { label.visibility = View.GONE }
                            .start()
                        editText.hint = hintText
                    } else {
                        if (label.visibility != View.VISIBLE) {
                            label.alpha = 0f
                            label.visibility = View.VISIBLE
                            label.animate().alpha(1f).setDuration(150).start()
                        }
                        editText.hint = ""
                    }
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            })
        }
        setupFloatingLabel(emailEditText, emailLabel, getString(R.string.email_address))
        setupFloatingLabel(passwordEditText, passwordLabel, getString(R.string.password))
        setupFloatingLabel(firstNameEditText, firstNameLabel, getString(R.string.first_name))
        setupFloatingLabel(lastNameEditText, lastNameLabel, getString(R.string.last_name))
    }
}
