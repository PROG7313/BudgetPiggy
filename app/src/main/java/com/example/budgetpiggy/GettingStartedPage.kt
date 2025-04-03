package com.example.budgetpiggy

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

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
            onBackPressedDispatcher.onBackPressed()
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        }

getBudgetingBtn.setOnClickListener {
    val nextIntent = Intent(this, HomePage::class.java)
    val nextOptions = ActivityOptions.makeCustomAnimation(
        this,
        R.anim.fade_in,
        R.anim.fade_out
    )

    startActivity(nextIntent, nextOptions.toBundle())
}

    }
}
