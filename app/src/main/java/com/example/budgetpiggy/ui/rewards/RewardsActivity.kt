package com.example.budgetpiggy.ui.rewards

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.budgetpiggy.R
import com.example.budgetpiggy.data.database.AppDatabase
import com.example.budgetpiggy.data.repository.RewardRepository
import com.example.budgetpiggy.ui.core.BaseActivity
import com.example.budgetpiggy.utils.SessionManager

class RewardsActivity : BaseActivity() {

    // 1) build the ViewModel, injecting your repo + current user
    private val viewModel: RewardsViewModel by viewModels {
        val userId = SessionManager.getUserId(this)
            ?: throw IllegalStateException("No user logged in!")
        RewardsViewModelFactory(
            repo   = RewardRepository(
                rewardDao    = AppDatabase.getDatabase(this).rewardDao(),
                codeDao      = AppDatabase.getDatabase(this).rewardCodeDao(),
                notifDao     = AppDatabase.getDatabase(this).notificationDao()
            ),
            userId = userId
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rewards)

        // Edge‐to‐edge insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.activity_rewards)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        // Hide home-only icons
        findViewById<ImageView>(R.id.piggyIcon)?.visibility   = View.GONE
        findViewById<ImageView>(R.id.streakIcon)?.visibility  = View.GONE
        findViewById<TextView>(R.id.greetingText)?.visibility = View.GONE

        // Page title
        findViewById<TextView>(R.id.pageTitle).apply {
            visibility = View.VISIBLE
            text = getString(R.string.RewardsTitle)
        }

        // 2) hook up your UI
        val topBar        = findViewById<View>(R.id.topBar)
        val codeInput     = findViewById<EditText>(R.id.codeInput)
        val confirmBtn    = findViewById<Button>(R.id.confirmButton)
        val rewardsGrid   = findViewById<RecyclerView>(R.id.rewardsGrid)

        // 3) wire up RecyclerView as a 3-column grid
        rewardsGrid.layoutManager = GridLayoutManager(this, 3)

        // 4) observe the LiveData of (code, unlocked) pairs
        viewModel.rewards.observe(this) { items ->
            rewardsGrid.adapter = RewardsAdapter(items)
            // update badge on top-bar to count of UNREAD notifications
            // (optional—you can also call into your BaseActivity helper)
            updateNotificationBadgeGlobally(topBar, /* fetch unread from DB if you like */ 0)
        }

        // 5) handle “Confirm” button tap
        confirmBtn.setOnClickListener { v ->
            val code = codeInput.text.toString().trim()
            if (code.isEmpty()) {
                Toast.makeText(this, "Please enter a code", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // disable while pending
            v.isEnabled = false
            viewModel.confirm(code)
        }

        // 6) show feedback when unlocking finishes
        viewModel.result.observe(this) { result ->
            findViewById<Button>(R.id.confirmButton).isEnabled = true
            result.fold(
                onSuccess = {
                    Toast.makeText(this, "Reward unlocked!", Toast.LENGTH_SHORT).show()
                    codeInput.text?.clear()
                },
                onFailure = { err ->
                    Toast.makeText(this, err.message ?: "Failed", Toast.LENGTH_SHORT).show()
                }
            )
        }

        // 7) back arrow & bottom nav can be wired exactly as in your other screens:
        findViewById<View>(R.id.backArrow).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        // …and so on for nav_home, nav_wallet, etc., calling setActiveNavIcon(...) + startActivity(...)
    }
}
