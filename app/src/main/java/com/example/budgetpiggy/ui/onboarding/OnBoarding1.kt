package com.example.budgetpiggy.ui.onboarding

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.budgetpiggy.R

class OnBoarding1 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Edge to edge layout
        enableEdgeToEdge()
        setContentView(R.layout.onboard_1)

        // Handles the padding
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.onboard_1)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val nextButton: Button = findViewById(R.id.nextButton)

        // Animation of next button
        nextButton.setOnClickListener { view ->
            view.animate().scaleX(0.95f).scaleY(0.95f).setDuration(25).withEndAction {
                    view.animate().scaleX(1f).scaleY(1f).setDuration(25).start()
                    startActivity(Intent(this, OnBoarding2::class.java))
                   // startActivity(Intent(this, TestActivity::class.java))
                }.start()
        }
    }
}
