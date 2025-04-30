package com.example.budgetpiggy.ui.core

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.lifecycle.lifecycleScope
import com.example.budgetpiggy.data.database.AppDatabase
import com.example.budgetpiggy.data.entities.RewardCodeEntity
import com.example.budgetpiggy.ui.auth.GettingStartedPage
import com.example.budgetpiggy.ui.auth.LoginPage
import com.example.budgetpiggy.ui.home.HomePage
import com.example.budgetpiggy.ui.onboarding.OnBoarding1
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.budgetpiggy.R // import your R

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Launch a coroutine tied to the activity lifecycle (Android, 2025)
        lifecycleScope.launch(Dispatchers.IO) {
            // DB instance
            val db = AppDatabase.getDatabase(this@SplashActivity)
            // Package name
            val pkg = packageName // <- needed for building resource URIs
            // List of reward codes
            val codes = listOf(
                Triple(
                    "SIGNUP2025",
                    "Welcome Bonus",
                    "android.resource://$pkg/${R.drawable.ic_welcome_bonus}"
                ),
                Triple(
                    "FIRSTACC2025",
                    "First Account",
                    "android.resource://$pkg/${R.drawable.ic_first_account}"
                ),
                Triple(
                    "FIRSTCAT2025",
                    "First Category",
                    "android.resource://$pkg/${R.drawable.ic_first_category}"
                )
                // more codes can be added later
            )

            // Insert codes into the DB only if they don't already exist
            codes.forEach { (code, name, imgUrl) ->
                if (db.rewardCodeDao().getByCode(code) == null) {
                    db.rewardCodeDao().insert(
                        RewardCodeEntity(
                            code           = code,
                            rewardName     = name,
                            rewardImageUrl = imgUrl
                        )
                    )
                }
            }

            // back to Main thread
            withContext(Dispatchers.Main) {
                val prefs               = getSharedPreferences("app_piggy_prefs", MODE_PRIVATE)
                val seenOnboarding      = prefs.getBoolean("has_seen_onboarding", false)
                val userId              = prefs.getString("logged_in_user_id", null)
                val needsGettingStarted = prefs.getBoolean("needs_getting_started", false)

                // Navigation to the correct screen (Android, 2025)
                when {
                    !seenOnboarding -> {
                        prefs.edit { putBoolean("has_seen_onboarding", true) }
                        startActivity(Intent(this@SplashActivity, OnBoarding1::class.java))
                    }
                    needsGettingStarted && userId != null -> {
                        prefs.edit { putBoolean("needs_getting_started", false) }
                        startActivity(Intent(this@SplashActivity, GettingStartedPage::class.java))
                    }
                    userId != null -> {
                        startActivity(Intent(this@SplashActivity, HomePage::class.java))
                    }
                    else -> {
                        startActivity(Intent(this@SplashActivity, LoginPage::class.java))
                    }
                }

                finish()
            }
        }
    }
}
