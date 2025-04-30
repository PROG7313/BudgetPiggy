package com.example.budgetpiggy.ui.auth


import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.budgetpiggy.R
import com.example.budgetpiggy.ui.core.SplashActivity
import com.example.budgetpiggy.ui.home.HomePage

class GettingStartedPage : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // Allows drawing behind the system bars
        setContentView(R.layout.getting_started) // set layout

        // Ensures the user interface elements have correct padding around system bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.gettingStartedPage)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Reference to back arrow and button (Android, 2025)
        val backArrow = findViewById<ImageView>(R.id.backArrow)
        val getBudgetingBtn = findViewById<Button>(R.id.getStartedButton)
        backArrow.setOnClickListener { view ->

        // Handle back arrow action with animation
        view.animate()
            .scaleX(0.95f)
            .scaleY(0.95f)
            .setDuration(25)
            .withEndAction {
                    view.animate().scaleX(1f).scaleY(1f).setDuration(25).start()
                    onBackPressed()
            }.start()

        }

        // Handle button with animation
        getBudgetingBtn.setOnClickListener { view ->
            view.animate()
                .scaleX(0.95f)
                .scaleY(0.95f)
                .setDuration(25)
                .withEndAction {
                    view.animate().scaleX(1f).scaleY(1f).setDuration(25).start()

                    // Save preference so that the user only sees the Getting Started page once
                    getSharedPreferences("app_piggy_prefs", MODE_PRIVATE).edit {
                        putBoolean("needs_getting_started", false)
                    }

                    // Go to HomePage
                    startActivity(Intent(this, HomePage::class.java))
                    finish()
                }.start()
        }


    }

    // Override back button to confirm with user before exiting (Ambitions, 2025)
    @Suppress("MissingSuperCall")
    override fun onBackPressed() {
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle("Exit Getting Started?")
        builder.setMessage("Are you sure you want to cancel getting started?")
        builder.setPositiveButton("Yes") { _, _ ->


            getSharedPreferences("app_piggy_prefs", MODE_PRIVATE).edit {
                putBoolean("needs_getting_started", false)
            }

            // Then go to SplashActivity
            val intent = Intent(this, SplashActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            finish()
        }
        builder.setNegativeButton("No") { dialog, _ ->
            dialog.dismiss()
        }
        builder.show()
    }
}