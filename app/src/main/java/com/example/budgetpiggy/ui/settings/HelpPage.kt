package com.example.budgetpiggy.ui.settings

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.example.budgetpiggy.R
import com.example.budgetpiggy.ui.core.BaseActivity
import com.example.budgetpiggy.ui.core.SplashActivity
import com.example.budgetpiggy.ui.home.HomePage
import com.example.budgetpiggy.ui.notifications.Notification
import com.example.budgetpiggy.ui.reports.ReportsPage
import com.example.budgetpiggy.ui.wallet.WalletPage
import com.example.budgetpiggy.utils.SessionManager
import androidx.core.view.isVisible

class HelpPage : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.help_page)

        val helpContainer = findViewById<LinearLayout>(R.id.helpContainer)
        val inflater = LayoutInflater.from(this)

        val sections = listOf(
            R.layout.help_section_add_expense,
            R.layout.help_section_notifications,
            R.layout.help_section_rewards,
            R.layout.help_section_budgets
        )

        for (layoutId in sections) {
            val sectionView = inflater.inflate(layoutId, helpContainer, false)
            helpContainer.addView(sectionView)

            setupExpandableSection(sectionView)
        }

        // Bell → Notification screen
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

        // Back arrow → go back
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

        // Bottom nav icons
        val navHome    = findViewById<ImageView>(R.id.nav_home)
        val navWallet  = findViewById<ImageView>(R.id.nav_wallet)
        val navReports = findViewById<ImageView>(R.id.nav_reports)
        val navProfile = findViewById<ImageView>(R.id.nav_profile)
        val currency = findViewById<LinearLayout>(R.id.currency)
        val logoutButton = findViewById<LinearLayout>(R.id.logout)
        val aboutMe = findViewById<LinearLayout>(R.id.about)
        val help = findViewById<LinearLayout>(R.id.help)


        help.setOnClickListener{
            startActivity(Intent(this, HelpPage::class.java))
        }
        aboutMe.setOnClickListener {
            startActivity(Intent(this, AboutUsPage::class.java))
        }
        logoutButton.setOnClickListener {
            // clear the saved session
            SessionManager.logout(this)

            // go back to SplashActivity and clear everything
            val intent = Intent(this, SplashActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            finish()
        }

        currency.setOnClickListener {
            startActivity(Intent(this, ChangeCurrency::class.java))
        }
        navHome.setOnClickListener { v: View ->
            setActiveNavIcon(v as ImageView)
            startActivity(Intent(this, HomePage::class.java))
        }

        navWallet.setOnClickListener { v: View ->
            setActiveNavIcon(v as ImageView)
            startActivity(Intent(this, WalletPage::class.java))
        }

        navReports.setOnClickListener { v: View ->
            setActiveNavIcon(v as ImageView)
            startActivity(Intent(this, ReportsPage::class.java))
        }

        navProfile.setOnClickListener { v: View ->
            // Already in AccountPage
            setActiveNavIcon(v as ImageView)
        }

    }

    private fun setupExpandableSection(sectionView: View) {
        val header = sectionView.findViewById<TextView>(R.id.sectionHeader)
        val content = sectionView.findViewById<LinearLayout>(R.id.sectionContent)
        val icon = sectionView.findViewById<ImageView>(R.id.expandIcon)

        content.visibility = View.GONE

        header.setOnClickListener {
            val isVisible = content.isVisible
            content.visibility = if (isVisible) View.GONE else View.VISIBLE
            icon.setImageResource(
                if (isVisible) R.drawable.ic_arrow_drop_down else R.drawable.ic_arrow_up
            )
        }
    }

    override fun onResume() {
        super.onResume()
        // Highlight profile/account icon
        setActiveNavIcon(findViewById(R.id.nav_profile))
    }
}

