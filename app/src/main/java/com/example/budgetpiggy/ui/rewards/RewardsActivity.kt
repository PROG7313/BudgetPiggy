package com.example.budgetpiggy.ui.rewards

import android.content.Intent
import android.os.Bundle
import android.view.View
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
import com.example.budgetpiggy.ui.home.HomePage
import com.example.budgetpiggy.ui.notifications.Notification
import com.example.budgetpiggy.ui.reports.ReportsPage
import com.example.budgetpiggy.ui.settings.AccountPage
import com.example.budgetpiggy.ui.wallet.WalletPage
import com.example.budgetpiggy.utils.SessionManager

class RewardsActivity : BaseActivity() {

    // ViewModel scoped to this activity, initialized using factory with dependencies (Android, 2025).
    private val viewModel: RewardsViewModel by viewModels {
        val userId = SessionManager.getUserId(this)
            ?: throw IllegalStateException("No user logged in!")
        RewardsViewModelFactory(
            repo = RewardRepository(
                rewardDao = AppDatabase.getDatabase(this).rewardDao(),
                codeDao = AppDatabase.getDatabase(this).rewardCodeDao(),
                notifDao = AppDatabase.getDatabase(this).notificationDao()
            ),
            userId = userId
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rewards)

        // Handles padding
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.activity_rewards)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        // Hide unnecessary UI elements
        findViewById<ImageView>(R.id.piggyIcon)?.visibility = View.GONE
        findViewById<ImageView>(R.id.streakIcon)?.visibility = View.GONE
        findViewById<TextView>(R.id.greetingText)?.visibility = View.GONE

        // Set page title
        findViewById<TextView>(R.id.pageTitle).apply {
            visibility = View.VISIBLE
            text = getString(R.string.RewardsTitle)
        }

        val rewardsGrid = findViewById<RecyclerView>(R.id.rewardsGrid)
        rewardsGrid.layoutManager = GridLayoutManager(this, 3)

        viewModel.rewards.observe(this) { items ->
            rewardsGrid.adapter = RewardsAdapter(items)
        }

        // Navbar icons with click listeners
        findViewById<ImageView>(R.id.nav_home).setOnClickListener {
            setActiveNavIcon(it as ImageView)
            startActivity(Intent(this, HomePage::class.java))
        }
        findViewById<ImageView>(R.id.backArrow).setOnClickListener { v ->
            v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(25)
                .withEndAction {
                    v.animate().scaleX(1f).scaleY(1f).setDuration(25).start()
                    onBackPressed()
                }.start()
        }
        findViewById<ImageView>(R.id.bellIcon).setOnClickListener {
            startActivity(Intent(this, Notification::class.java))
        }
        findViewById<ImageView>(R.id.nav_wallet).setOnClickListener {
            setActiveNavIcon(it as ImageView)
            startActivity(Intent(this, WalletPage::class.java))
        }
        findViewById<ImageView>(R.id.nav_reports).setOnClickListener {
            setActiveNavIcon(it as ImageView)
            startActivity(Intent(this, ReportsPage::class.java))
        }
        findViewById<ImageView>(R.id.nav_profile).setOnClickListener {
            setActiveNavIcon(it as ImageView)
            startActivity(Intent(this, AccountPage::class.java))
        }
    }

    // Highlights the active navigation icon
    override fun setActiveNavIcon(activeIcon: ImageView) {
        val navIcons = listOf(
            R.id.nav_home to R.drawable.vec_home_inactive,
            R.id.nav_wallet to R.drawable.vec_wallet_inactive,
            R.id.nav_reports to R.drawable.vec_reports_inactive,
            R.id.nav_profile to R.drawable.vec_profile_inactive
        )
        navIcons.forEach { (id, drawable) ->
            findViewById<ImageView>(id).setImageResource(drawable)
        }
        when (activeIcon.id) {
            R.id.nav_home -> activeIcon.setImageResource(R.drawable.vec_home_active)
            R.id.nav_wallet -> activeIcon.setImageResource(R.drawable.vec_wallet_active)
            R.id.nav_reports -> activeIcon.setImageResource(R.drawable.vec_reports_active)
            R.id.nav_profile -> activeIcon.setImageResource(R.drawable.vec_profile_active)
        }
    }

    // Back press action
    override fun onBackPressed() {
        if (!isTaskRoot) {
            super.onBackPressed()
        } else {
            Toast.makeText(this, "Use the logout button to exit", Toast.LENGTH_SHORT).show()
        }
    }

    // Clears navigation icon highlight
    override fun onResume() {
        super.onResume()
        clearNavIcons()
    }
}
