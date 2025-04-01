package com.example.budgetpiggy

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class WelcomePage3 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.welcome_page_3)
        enableEdgeToEdge()
        setContentView(R.layout.welcome_page_3)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.welcomePage3)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val previousButton: Button = findViewById(R.id.previousButtonWelcomePage3)
        val nextButton: Button = findViewById(R.id.nextButtonWelcomePage3)
        previousButton.setOnClickListener {
            val intent = Intent(this, WelcomePage2::class.java)

            // ✅ Apply slide reverse animation
            val options = ActivityOptions.makeCustomAnimation(
                this,
                R.anim.fade_in,
                R.anim.fade_out
            )
            startActivity(intent, options.toBundle())
        }
            /*
            nextButton.setOnClickListener {
                val nextIntent = Intent(this, WelcomePage3::class.java)
                val nextOptions = ActivityOptions.makeCustomAnimation(
                    this,
                    R.anim.fade_in,
                    R.anim.fade_out
                )

                startActivity(nextIntent, nextOptions.toBundle())
            }
            */

    }
}
