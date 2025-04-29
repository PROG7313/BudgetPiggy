package com.example.budgetpiggy.ui.auth

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
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.budgetpiggy.ui.home.HomePage
import com.example.budgetpiggy.R
import com.example.budgetpiggy.data.database.AppDatabase
import com.example.budgetpiggy.ui.core.BaseActivity
import com.example.budgetpiggy.utils.PasswordUtils
import com.example.budgetpiggy.utils.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginPage : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.LoginPage)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val loginBtn = findViewById<Button>(R.id.loginButton)
        val backArrow = findViewById<ImageView>(R.id.backArrow)
        val signUpTextView = findViewById<TextView>(R.id.signUpText)
        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)
        val passwordLabel = findViewById<TextView>(R.id.passwordLabel)
        val emailEditText = findViewById<EditText>(R.id.emailEditText)
        val emailLabel = findViewById<TextView>(R.id.emailLabel)
        val eyeIcon = findViewById<ImageView>(R.id.eyeIcon)
        loginBtn.setOnClickListener { view ->
            view.animate().scaleX(0.95f).scaleY(0.95f).setDuration(25).withEndAction {
                view.animate().scaleX(1f).scaleY(1f).setDuration(25).start()

                val email = emailEditText.text.toString().trim()
                val password = passwordEditText.text.toString()

                lifecycleScope.launch(Dispatchers.IO) {
                    val db = AppDatabase.getDatabase(this@LoginPage)
                    val user = db.userDao().getUserByEmail(email)

                    withContext(Dispatchers.Main) {
                        if (user?.passwordHash?.let { PasswordUtils.verifyPassword(password, it) } == true) {

                            SessionManager.saveUserId(this@LoginPage, user.userId)


                            // Login successful
                            val intent = Intent(this@LoginPage, HomePage::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            }
                            startActivity(intent)
                            finish()

                        } else {
                            // Show login error
                            Toast.makeText(this@LoginPage, "Invalid email or password", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }.start()
        }


        signUpTextView.setOnClickListener {
                view ->


            view.animate()
                .scaleX(0.95f)
                .scaleY(0.95f)
                .setDuration(25)
                .withEndAction {
                    view.animate().scaleX(1f).scaleY(1f).setDuration(25).start()
                    startActivity(Intent(this, RegisterPage::class.java))
                }.start()

        }
        backArrow.setOnClickListener {
                view ->


            view.animate()
                .scaleX(0.95f)
                .scaleY(0.95f)
                .setDuration(25)
                .withEndAction {
                    view.animate().scaleX(1f).scaleY(1f).setDuration(25).start()
                    onBackPressed()
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


    }
    override fun onBackPressed() {
        if (!isTaskRoot) {
            // we were pushed here from somewhere else → go back normally
            super.onBackPressed()
        } else {
            // this is the root of the task → swallow or toast
            Toast
                .makeText(
                    this,
                    "Use the menu",
                    Toast.LENGTH_SHORT
                )
                .show()
        }
    }
}
