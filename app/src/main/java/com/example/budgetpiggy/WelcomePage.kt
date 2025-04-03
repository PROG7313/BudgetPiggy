package com.example.budgetpiggy

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class WelcomePage : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.welcome_page)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.welcomePage)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val createAccountBtn = findViewById<Button>(R.id.createAccountButton)
        val loginTextView = findViewById<TextView>(R.id.loginTextView)
        createAccountBtn.setOnClickListener {
            startActivity(Intent(this, RegisterPage::class.java))
        }

loginTextView.setOnClickListener {
    startActivity(Intent(this, LoginPage::class.java))
}


    }
}
