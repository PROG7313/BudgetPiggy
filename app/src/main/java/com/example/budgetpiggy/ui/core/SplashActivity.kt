package com.example.budgetpiggy.ui.core

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.budgetpiggy.ui.auth.GettingStartedPage
import com.example.budgetpiggy.ui.auth.LoginPage
import com.example.budgetpiggy.ui.home.HomePage
import com.example.budgetpiggy.ui.onboarding.OnBoarding1
import androidx.core.content.edit

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences("app_piggy_prefs", MODE_PRIVATE)
        val seenOnboarding = prefs.getBoolean("has_seen_onboarding", false)
        val userId = prefs.getString("logged_in_user_id", null)
        val needsGettingStarted = prefs.getBoolean("needs_getting_started", false)
        when {
            //  first‐ever launch: show your onboarding flow
            !seenOnboarding -> {
                prefs.edit { putBoolean("has_seen_onboarding", true) }
                startActivity(Intent(this, OnBoarding1::class.java))
            }
            needsGettingStarted && userId != null -> {
                prefs.edit { putBoolean("needs_getting_started", false) }
                startActivity(Intent(this, GettingStartedPage::class.java))
            }
            //  already registered and logged in: go straight to home
            userId != null -> {
                startActivity(Intent(this, HomePage::class.java))
            }
            //  registered but not logged in (or cleared data): show login
            else -> {
                startActivity(Intent(this, LoginPage::class.java))
            }
        }
        finish()
    }
}
