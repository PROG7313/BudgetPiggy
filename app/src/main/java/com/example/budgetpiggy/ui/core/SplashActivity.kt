package com.example.budgetpiggy.ui.core

import android.content.Context
import android.content.Intent
import android.util.Log
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
import com.example.budgetpiggy.R
import java.util.Calendar
import java.util.Date
import com.example.budgetpiggy.data.entities.RewardEntity
import java.util.UUID


class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch(Dispatchers.IO) {
            val db  = AppDatabase.getDatabase(this@SplashActivity)
            val pkg = packageName

            // 1) seed reward codes
            val codes = listOf(
                Triple("SIGNUP2025",    "Welcome Bonus",                  "android.resource://$pkg/${R.drawable.ic_welcome_bonus}"),
                Triple("FIRSTACC2025",  "First Account",                  "android.resource://$pkg/${R.drawable.ic_first_account}"),
                Triple("FIRSTCAT2025",  "First Category",                 "android.resource://$pkg/${R.drawable.ic_first_category}"),
                Triple("GOAL_MIN",      "Minimum Spending Goal Achieved", "android.resource://$pkg/${R.drawable.ic_goal_min}"),
                Triple("GOAL_MAX",      "Maximum Spending Goal Surpassed","android.resource://$pkg/${R.drawable.ic_goal_max}")
            )
            codes.forEach { (code, name, imgUrl) ->
                if (db.rewardCodeDao().getByCode(code) == null) {
                    db.rewardCodeDao().insert(RewardCodeEntity(code, name, imgUrl))
                }
            }

            //  check 30 days since goals were first set
            val prefs = getSharedPreferences("app_piggy_prefs", Context.MODE_PRIVATE)
            val ts    = prefs.getLong("goal_set_ts", 0L)
            val now   = System.currentTimeMillis()
            if (ts > 0 && now - ts >= 30L * 24 * 60 * 60 * 1000) {
                Log.d("SplashActivity", "🎯 Goal‐reward block is RUNNING! ts=$ts, now=$now")

                val uid = prefs.getString("logged_in_user_id", null)
                if (uid != null) {



                    // sum spending for the month
                    val totalExp = db.transactionDao()
                        .sumMonthlySpending(uid, ts, now)

                    Log.d("SplashActivity", "TotalExp=$totalExp, minGoal=${prefs.getInt("min_expense_goal",0)}, maxGoal=${prefs.getInt("max_expense_goal",0)}")

                    // decide which reward
                    val awardCode = when {
                        totalExp >= prefs.getInt("max_expense_goal", 0) -> "GOAL_MAX"
                        totalExp >= prefs.getInt("min_expense_goal", 0) -> "GOAL_MIN"
                        else -> null
                    }
                    Log.d("SplashActivity", "awardCode=$awardCode")

                    awardCode?.let { code ->
                        if (db.rewardDao().getByCodeForUser(code, uid) == null) {
                            // fetch the catalog name for display
                            val catalog = db.rewardCodeDao().getByCode(code)
                            val name    = catalog?.rewardName ?: code

                            db.rewardDao().insert(
                                RewardEntity(
                                    rewardId   = UUID.randomUUID().toString(),
                                    userId     = uid,
                                    rewardName = name,
                                    unlockedAt = Date().time
                                )
                            )
                            Log.d("SplashActivity", "Inserted RewardEntity for code=$code, user=$uid")
                        } else {
                            Log.d("SplashActivity", "User already has reward code=$code")
                        }
                    }

                    // clear so this only runs once
                    prefs.edit { remove("goal_set_ts") }
                    Log.d("SplashActivity", "Cleared goal_set_ts")
                }
            } else {
                Log.d("SplashActivity", "Skipping goal‐reward block; ts=$ts, now=$now")
            }

            // 3) navigate
            withContext(Dispatchers.Main) {
                val prefs               = getSharedPreferences("app_piggy_prefs", Context.MODE_PRIVATE)
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
}
