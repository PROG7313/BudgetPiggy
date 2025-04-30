package com.example.budgetpiggy.ui.core

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.lifecycle.lifecycleScope
import com.example.budgetpiggy.R
import com.example.budgetpiggy.data.database.AppDatabase
import com.example.budgetpiggy.data.entities.RewardCodeEntity
import com.example.budgetpiggy.data.entities.RewardEntity
import com.example.budgetpiggy.ui.auth.GettingStartedPage
import com.example.budgetpiggy.ui.auth.LoginPage
import com.example.budgetpiggy.ui.home.HomePage
import com.example.budgetpiggy.ui.notifications.NotificationHelper
import com.example.budgetpiggy.ui.onboarding.OnBoarding1
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Notification channel
        NotificationHelper.createNotificationChannel(this)

        // 2. Request Android 13+ notification permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 1001)
        }

        // 3. Seed data, check goals, then navigate
        lifecycleScope.launch(Dispatchers.IO) {
            val db = AppDatabase.getDatabase(this@SplashActivity)
            val pkg = packageName

            // A) Seed reward codes catalog
            val codes = listOf(
                Triple("SIGNUP2025",   "Welcome Bonus",                  "android.resource://$pkg/${R.drawable.ic_welcome_bonus}"),
                Triple("FIRSTACC2025", "First Account",                  "android.resource://$pkg/${R.drawable.ic_first_account}"),
                Triple("FIRSTCAT2025", "First Category",                 "android.resource://$pkg/${R.drawable.ic_first_category}"),
                Triple("GOAL_MIN",     "Minimum Spending Goal Achieved", "android.resource://$pkg/${R.drawable.ic_goal_min}"),
                Triple("GOAL_MAX",     "Maximum Spending Goal Surpassed","android.resource://$pkg/${R.drawable.ic_goal_max}")
            )
            codes.forEach { (code, name, imgUrl) ->
                if (db.rewardCodeDao().getByCode(code) == null) {
                    db.rewardCodeDao().insert(RewardCodeEntity(code, name, imgUrl))
                }
            }

            // B) If 30 days since goal set, award goal-based reward
            val prefs = getSharedPreferences("app_piggy_prefs", Context.MODE_PRIVATE)
            val ts    = prefs.getLong("goal_set_ts", 0L)
            val now   = System.currentTimeMillis()
            if (ts > 0 && now - ts >= 30L * 24 * 60 * 60 * 1000) {
                Log.d("SplashActivity", "Goal‐reward block running (ts=$ts, now=$now)")
                val uid = prefs.getString("logged_in_user_id", null)
                if (uid != null) {
                    val totalExp = db.transactionDao().sumMonthlySpending(uid, ts, now)
                    val minGoal  = prefs.getInt("min_expense_goal", 0)
                    val maxGoal  = prefs.getInt("max_expense_goal", 0)
                    val awardCode = when {
                        totalExp >= maxGoal -> "GOAL_MAX"
                        totalExp >= minGoal -> "GOAL_MIN"
                        else                -> null
                    }
                    awardCode?.takeIf { db.rewardDao().getByCodeForUser(it, uid) == null }?.let { code ->
                        val name = db.rewardCodeDao().getByCode(code)?.rewardName ?: code
                        db.rewardDao().insert(
                            RewardEntity(
                                rewardId   = UUID.randomUUID().toString(),
                                userId     = uid,
                                rewardName = name,
                                unlockedAt = Date().time
                            )
                        )
                        Log.d("SplashActivity", "Inserted goal reward $code for user $uid")
                    }
                }
                prefs.edit { remove("goal_set_ts") }
                Log.d("SplashActivity", "Cleared goal_set_ts")
            }

            // C) Navigate on main thread
            withContext(Dispatchers.Main) {
                val seenOnboarding      = prefs.getBoolean("has_seen_onboarding", false)
                val userId              = prefs.getString("logged_in_user_id", null)
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

    // Handle Android 13+ notification-permission result
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1001 &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            NotificationHelper.sendNotification(
                context = this,
                title = "Welcome!",
                message = "You're all set to receive BudgetPiggy rewards"
            )
        } else if (requestCode == 1001) {
            Toast.makeText(this, "Notifications permission denied", Toast.LENGTH_SHORT).show()
        }
    }
}
