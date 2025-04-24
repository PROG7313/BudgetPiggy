package com.example.budgetpiggy

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Notification : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.notification)

        findViewById<ImageView>(R.id.piggyIcon).visibility = View.GONE
        findViewById<TextView>(R.id.greetingText).visibility = View.GONE
        findViewById<ImageView>(R.id.streakIcon).visibility = View.GONE

        val pageTitle = findViewById<TextView>(R.id.pageTitle)
        pageTitle.visibility = View.VISIBLE
        pageTitle.text = getString(R.string.notifications_1)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainLayout)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val backArrow = findViewById<ImageView>(R.id.backArrow)
        val notifyList = findViewById<LinearLayout>(R.id.notificationList)
        val scrollView = findViewById<ScrollView>(R.id.scrollArea)
        val fabWrapper = findViewById<View>(R.id.fabWrapper)

        backArrow.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        val navWallet = findViewById<ImageView>(R.id.nav_wallet)
        val navReports = findViewById<ImageView>(R.id.nav_reports)
        val navProfile = findViewById<ImageView>(R.id.nav_profile)
        val navHome = findViewById<ImageView>(R.id.nav_home)

        navHome.setOnClickListener { view ->
            setActiveNavIcon(navHome)
            view.animate().scaleX(0.95f).scaleY(0.95f).setDuration(25).withEndAction {
                view.animate().scaleX(1f).scaleY(1f).setDuration(25).start()
                startActivity(Intent(this, HomePage::class.java))
            }.start()
        }

        navWallet.setOnClickListener { view ->
            setActiveNavIcon(navWallet)
            view.animate().scaleX(0.95f).scaleY(0.95f).setDuration(25).withEndAction {
                view.animate().scaleX(1f).scaleY(1f).setDuration(25).start()
                startActivity(Intent(this, WalletPage::class.java))
            }.start()
        }

        navReports.setOnClickListener {
            setActiveNavIcon(navReports)
            // startActivity(Intent(this, ReportsPage::class.java))
        }

        navProfile.setOnClickListener {
            setActiveNavIcon(navProfile)
            // startActivity(Intent(this, ProfilePage::class.java))
        }

        var lastScrollY = 0
        scrollView.viewTreeObserver.addOnScrollChangedListener {
            val scrollY = scrollView.scrollY
            val isScrollingUp = scrollY < lastScrollY
            val isAtTop = !scrollView.canScrollVertically(-1)
            val isAtBottom = !scrollView.canScrollVertically(1)

            when {
                isScrollingUp || isAtTop || isAtBottom -> {
                    fabWrapper.visibility = View.VISIBLE
                    fabWrapper.animate().alpha(1f).setDuration(1).start()
                }
                else -> {
                    fabWrapper.animate().alpha(0f).setDuration(1)
                        .withEndAction { fabWrapper.visibility = View.GONE }
                        .start()
                }
            }

            lastScrollY = scrollY
        }

        val sampleNotifs = listOf(
            "🎉 Well Done! You earned your sign-up Starsucks voucher.\nDiscount Code: STARSUCK111",
            "🚀 No more physical budgeting needed! Use CODE111 to earn your first Rewards Badge"
        )

        for (message in sampleNotifs) {
            val notifItem = layoutInflater.inflate(R.layout.item_notification_card, notifyList, false)
            val msgView = notifItem.findViewById<TextView>(R.id.notificationMessage)
            msgView.text = message
            notifyList.addView(notifItem)
        }
    }

    override fun setActiveNavIcon(activeIcon: ImageView) {
        val navIcons = listOf(
            R.id.nav_home to R.drawable.vec_home_inactive,
            R.id.nav_wallet to R.drawable.vec_wallet_inactive,
            R.id.nav_reports to R.drawable.vec_reports_inactive,
            R.id.nav_profile to R.drawable.vec_profile_inactive
        )

        for ((id, inactiveDrawable) in navIcons) {
            val icon = findViewById<ImageView>(id)
            icon.setImageResource(inactiveDrawable)
        }

        when (activeIcon.id) {
            R.id.nav_home -> activeIcon.setImageResource(R.drawable.vec_home_active)
            R.id.nav_wallet -> activeIcon.setImageResource(R.drawable.vec_wallet_active)
            R.id.nav_reports -> activeIcon.setImageResource(R.drawable.vec_reports_active)
            R.id.nav_profile -> activeIcon.setImageResource(R.drawable.vec_profile_active)
        }
    }
}
