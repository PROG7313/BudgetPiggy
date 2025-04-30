package com.example.budgetpiggy.ui.settings

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.budgetpiggy.R
import com.example.budgetpiggy.ui.core.BaseActivity
import com.example.budgetpiggy.ui.home.HomePage
import com.example.budgetpiggy.ui.notifications.Notification
import com.example.budgetpiggy.ui.reports.ReportsPage
import com.example.budgetpiggy.ui.wallet.WalletPage
import androidx.core.view.isVisible

class HelpPage : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.help_page)

        // Apply window insets to avoid UI elements overlapping system bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.helpPage)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Hide piggy icon, streak icon, and greeting text which are not needed on this page
        findViewById<ImageView>(R.id.piggyIcon)?.visibility = View.GONE
        findViewById<ImageView>(R.id.streakIcon)?.visibility = View.GONE
        findViewById<TextView>(R.id.greetingText)?.visibility = View.GONE

        // Show and set the title to "Help"
        findViewById<TextView>(R.id.pageTitle).apply {
            visibility = View.VISIBLE
            text = getString(R.string.help_title)
        }

        // Reference to the container that will hold all help sections
        val helpContainer = findViewById<LinearLayout>(R.id.helpContainer)
        val inflater = LayoutInflater.from(this)

        // List of help section layout resources to inflate dynamically
        val sections = listOf(
            R.layout.help_section_add_expense,
            R.layout.help_section_notifications,
            R.layout.help_section_rewards,
            R.layout.help_section_budgets
        )

        // Inflate and add each help section to the container, and make them expandable
        for (layoutId in sections) {
            val sectionView = inflater.inflate(layoutId, helpContainer, false)
            helpContainer.addView(sectionView)

            setupExpandableSection(sectionView)
        }

        // Bell icon → opens Notification screen with click animation
        findViewById<ImageView>(R.id.bellIcon)
            .setOnClickListener { v: View ->
                v.animate()
                    .scaleX(0.95f).scaleY(0.95f).setDuration(25)
                    .withEndAction {
                        v.animate().scaleX(1f).scaleY(1f).setDuration(25).start()
                        startActivity(Intent(this, Notification::class.java))
                    }
                    .start()
            }

        // Back arrow icon → navigates back with click animation
        findViewById<ImageView>(R.id.backArrow)
            .setOnClickListener { v: View ->
                v.animate()
                    .scaleX(0.95f).scaleY(0.95f).setDuration(25)
                    .withEndAction {
                        v.animate().scaleX(1f).scaleY(1f).setDuration(25).start()
                        onBackPressedDispatcher.onBackPressed()
                    }
                    .start()
            }

        // Bottom navigation icons setup
        val navHome    = findViewById<ImageView>(R.id.nav_home)
        val navWallet  = findViewById<ImageView>(R.id.nav_wallet)
        val navReports = findViewById<ImageView>(R.id.nav_reports)
        val navProfile = findViewById<ImageView>(R.id.nav_profile)

        // Navigate to Home page with icon click animation
        navHome.setOnClickListener { v: View ->
            setActiveNavIcon(v as ImageView)
            startActivity(Intent(this, HomePage::class.java))
        }

        // Navigate to Wallet page with icon click animation
        navWallet.setOnClickListener { v: View ->
            setActiveNavIcon(v as ImageView)
            startActivity(Intent(this, WalletPage::class.java))
        }

        // Navigate to Reports page with icon click animation
        navReports.setOnClickListener { v: View ->
            setActiveNavIcon(v as ImageView)
            startActivity(Intent(this, ReportsPage::class.java))
        }

        // Clicked on Profile icon (HelpPage is considered part of profile/settings), stays on same page
        navProfile.setOnClickListener { v: View ->
            setActiveNavIcon(v as ImageView)
        }
    }

    // Set up expand/collapse behavior for a help section
    private fun setupExpandableSection(sectionView: View) {
        val header = sectionView.findViewById<TextView>(R.id.sectionHeader)
        val content = sectionView.findViewById<LinearLayout>(R.id.sectionContent)
        val icon = sectionView.findViewById<ImageView>(R.id.expandIcon)

        content.visibility = View.GONE // Initially collapsed

        header.setOnClickListener {
            val isVisible = content.isVisible
            content.visibility = if (isVisible) View.GONE else View.VISIBLE
            icon.setImageResource(
                if (isVisible) R.drawable.ic_arrow_drop_down else R.drawable.ic_arrow_up
            )
        }
    }

    // Highlight the profile icon in the bottom nav when returning to this screen
    override fun onResume() {
        super.onResume()
        setActiveNavIcon(findViewById(R.id.nav_profile))
    }
}
