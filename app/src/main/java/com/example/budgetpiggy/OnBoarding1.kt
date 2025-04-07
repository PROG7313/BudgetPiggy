package com.example.budgetpiggy

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class OnBoarding1 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.onboard_1)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.onboard_1)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val nextButton: Button = findViewById(R.id.nextButton)
        nextButton.isHapticFeedbackEnabled = true

        nextButton.setOnClickListener { view ->


            view.animate()
                .scaleX(0.95f)
                .scaleY(0.95f)
                .setDuration(100)
                .withEndAction {
                    view.animate().scaleX(1f).scaleY(1f).setDuration(100).start()
                    startActivity(Intent(this, OnBoarding2::class.java))
                }.start()
        }


    }
}
