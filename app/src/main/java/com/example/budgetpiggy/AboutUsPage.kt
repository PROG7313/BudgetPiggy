package com.example.budgetpiggy

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class AboutUsPage : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.about_us)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.aboutUsPage)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Set visibility for top icons (if applicable)
        findViewById<ImageView>(R.id.piggyIcon).visibility = View.VISIBLE
        findViewById<ImageView>(R.id.streakIcon).visibility = View.VISIBLE

        // Set up back arrow navigation
        val backArrow = findViewById<ImageView>(R.id.backArrow)
        backArrow.setOnClickListener { view ->
            view.animate()
                .scaleX(0.95f)
                .scaleY(0.95f)
                .setDuration(25)
                .withEndAction {
                    view.animate().scaleX(1f).scaleY(1f).setDuration(25).start()
                    onBackPressedDispatcher.onBackPressed()
                }.start()
        }

        val navHome = findViewById<ImageView>(R.id.nav_home)
        val navReports = findViewById<ImageView>(R.id.nav_reports)
        val navWallet = findViewById<ImageView>(R.id.nav_wallet)

        val bellIcon = findViewById<ImageView>(R.id.bellIcon)

        bellIcon.setOnClickListener {
                view ->


            view.animate()
                .scaleX(0.95f)
                .scaleY(0.95f)
                .setDuration(25)
                .withEndAction {
                    view.animate().scaleX(1f).scaleY(1f).setDuration(25).start()
                    startActivity(Intent(this, Notification::class.java))
                }.start()

        }
        // Set active nav icon
        navHome.setOnClickListener {
                view ->

            setActiveNavIcon(navHome)
            view.animate()
                .scaleX(0.95f)
                .scaleY(0.95f)
                .setDuration(25)
                .withEndAction {
                    view.animate().scaleX(1f).scaleY(1f).setDuration(25).start()
                    startActivity(Intent(this, HomePage::class.java))
                }.start()
        }
        navWallet.setOnClickListener {
            setActiveNavIcon(navWallet)
            startActivity(Intent(this, WalletPage::class.java))
        }

        navReports.setOnClickListener {
            setActiveNavIcon(navReports)
            // startActivity(Intent(this, ReportsPage::class.java))
        }

    }

    private fun setActiveNavIcon(activeIcon: ImageView) {
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
