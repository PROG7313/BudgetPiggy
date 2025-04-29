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
        enableEdgeToEdge()
        setContentView(R.layout.getting_started)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.gettingStartedPage)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val backArrow = findViewById<ImageView>(R.id.backArrow)
        val getBudgetingBtn = findViewById<Button>(R.id.getStartedButton)
        backArrow.setOnClickListener { view ->


            view.animate()
                .scaleX(0.95f)
                .scaleY(0.95f)
                .setDuration(25)
                .withEndAction {
                    view.animate().scaleX(1f).scaleY(1f).setDuration(25).start()
                    onBackPressed()
                }.start()

        }

        getBudgetingBtn.setOnClickListener { view ->
            view.animate()
                .scaleX(0.95f)
                .scaleY(0.95f)
                .setDuration(25)
                .withEndAction {
                    view.animate().scaleX(1f).scaleY(1f).setDuration(25).start()


                    getSharedPreferences("app_piggy_prefs", MODE_PRIVATE).edit {
                        putBoolean("needs_getting_started", false)
                    }

                    // Go to HomePage
                    startActivity(Intent(this, HomePage::class.java))
                    finish()
                }.start()
        }


    }

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



