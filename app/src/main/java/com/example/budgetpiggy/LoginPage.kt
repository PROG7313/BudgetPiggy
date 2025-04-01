package com.example.budgetpiggy

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class LoginPage : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.LoginPage)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val loginbtn = findViewById<Button>(R.id.loginButton)
        val backArrow = findViewById<ImageView>(R.id.backArrow)
        val signUpTextView = findViewById<TextView>(R.id.signUpText)
        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)
        val eyeIcon = findViewById<ImageView>(R.id.eyeIcon)
        loginbtn.setOnClickListener {
            val intent = Intent(this, WelcomePage::class.java)
            val options = ActivityOptions.makeCustomAnimation(
                this,
                R.anim.fade_in,
                R.anim.fade_out
            )

            startActivity(intent, options.toBundle())
        }
        backArrow.setOnClickListener {
            val prevIntent = Intent(this, WelcomePage::class.java)


            val prevOptions = ActivityOptions.makeCustomAnimation(
                this,
                R.anim.fade_in,
                R.anim.fade_out
            )

            startActivity(prevIntent, prevOptions.toBundle())
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


    }
}
