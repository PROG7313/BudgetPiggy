package com.example.budgetpiggy.ui.auth


import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.budgetpiggy.R
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
        backArrow.setOnClickListener {
                view ->


            view.animate()
                .scaleX(0.95f)
                .scaleY(0.95f)
                .setDuration(25)
                .withEndAction {
                    view.animate().scaleX(1f).scaleY(1f).setDuration(25).start()
                    onBackPressedDispatcher.onBackPressed()
                }.start()

        }

getBudgetingBtn.setOnClickListener {
        view ->


    view.animate()
        .scaleX(0.95f)
        .scaleY(0.95f)
        .setDuration(25)
        .withEndAction {
            view.animate().scaleX(1f).scaleY(1f).setDuration(25).start()
            startActivity(Intent(this, HomePage::class.java))
        }.start()


}


}

    }

