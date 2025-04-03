package com.example.budgetpiggy


import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class OnBoarding4 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.onboard_4)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.onboard_4)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val previousButton: Button = findViewById(R.id.previousButtonWelcomePage4)
        val nextButton: Button = findViewById(R.id.nextButtonWelcomePage4)
        previousButton.setOnClickListener {

            startActivity(Intent(this, OnBoarding3::class.java))
        }

            nextButton.setOnClickListener {
                startActivity( Intent(this, WelcomePage::class.java))
            }


    }
}
