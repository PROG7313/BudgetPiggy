package com.example.budgetpiggy


import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class OnBoarding2 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.onboard_2)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.onboard_2)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val previousButton: Button = findViewById(R.id.previousButtonWelcomePage3)
        val nextButton : Button = findViewById(R.id.nextButtonWelcomePage3)
        previousButton.setOnClickListener {
                view ->


            view.animate()
                .scaleX(0.95f)
                .scaleY(0.95f)
                .setDuration(25)
                .withEndAction {
                    view.animate().scaleX(1f).scaleY(1f).setDuration(25).start()
                    startActivity(Intent(this, OnBoarding1::class.java))
                }.start()

        }
            nextButton.setOnClickListener {
                    view ->


                view.animate()
                    .scaleX(0.95f)
                    .scaleY(0.95f)
                    .setDuration(25)
                    .withEndAction {
                        view.animate().scaleX(1f).scaleY(1f).setDuration(25).start()
                        startActivity(Intent(this, OnBoarding3::class.java))
                    }.start()

        }
    }
}
