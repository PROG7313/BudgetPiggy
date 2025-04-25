package com.example.budgetpiggy.ui.core

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.budgetpiggy.ui.auth.LoginPage
import com.example.budgetpiggy.ui.home.HomePage
import com.example.budgetpiggy.ui.onboarding.OnBoarding1

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val seenOnboarding = prefs.getBoolean("has_seen_onboarding", false)
        val userId = prefs.getString("logged_in_user_id", null)

        when {
            // 1) first‐ever launch: show your onboarding flow
            !seenOnboarding -> {
                prefs.edit().putBoolean("has_seen_onboarding", true).apply()
                startActivity(Intent(this, OnBoarding1::class.java))
            }
            // 2) already registered and logged in: go straight to home
            userId != null -> {
                startActivity(Intent(this, HomePage::class.java))
            }
            // 3) registered but not logged in (or cleared data): show login
            else -> {
                startActivity(Intent(this, LoginPage::class.java))
            }
        }
        finish()
    }
}
