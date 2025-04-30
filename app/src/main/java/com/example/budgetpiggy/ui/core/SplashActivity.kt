package com.example.budgetpiggy.ui.core

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.lifecycle.lifecycleScope
import com.example.budgetpiggy.R
import com.example.budgetpiggy.data.database.AppDatabase
import com.example.budgetpiggy.data.entities.RewardCodeEntity
import com.example.budgetpiggy.ui.auth.GettingStartedPage
import com.example.budgetpiggy.ui.auth.LoginPage
import com.example.budgetpiggy.ui.home.HomePage
import com.example.budgetpiggy.ui.notifications.NotificationHelper
import com.example.budgetpiggy.ui.onboarding.OnBoarding1
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    // 1. Create notification channel
    NotificationHelper.createNotificationChannel(this)

    // 2. Request permission for Android 13+
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 1001)
        }
    }

    // 3. Launch seeding + navigation
    lifecycleScope.launch(Dispatchers.IO) {
        val db = AppDatabase.getDatabase(this@SplashActivity)
        val pkg = packageName

        // 4. Seed reward codes
        val codes = listOf(
            Triple("SIGNUP2025", "Welcome Bonus", "android.resource://$pkg/${R.drawable.ic_welcome_bonus}"),
            Triple("FIRSTACC2025", "First Account", "android.resource://$pkg/${R.drawable.ic_first_account}"),
            Triple("FIRSTCAT2025", "First Category", "android.resource://$pkg/${R.drawable.ic_first_category}")
        )

        codes.forEach { (code, name, imgUrl) ->
            if (db.rewardCodeDao().getByCode(code) == null) {
                db.rewardCodeDao().insert(
                    RewardCodeEntity(code = code, rewardName = name, rewardImageUrl = imgUrl)
                )
            }
        }

        // 5. Navigate to the correct screen
        withContext(Dispatchers.Main) {
            val prefs = getSharedPreferences("app_piggy_prefs", MODE_PRIVATE)
            val seenOnboarding = prefs.getBoolean("has_seen_onboarding", false)
            val userId = prefs.getString("logged_in_user_id", null)
            val needsGettingStarted = prefs.getBoolean("needs_getting_started", false)

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

    //  Handle the result of notification permission request
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 1001) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Send test notification after permission granted
                NotificationHelper.sendNotification(
                    context = this,
                    title = "Welcome!",
                    message = "You're all set to receive BudgetPiggy rewards"
                )
            } else {
                // Optional: show rationale or toast if user denies
                Toast.makeText(this, "Notifications permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
