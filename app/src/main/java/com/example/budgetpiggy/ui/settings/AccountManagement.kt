package com.example.budgetpiggy.ui.settings

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.budgetpiggy.ui.core.BaseActivity
import com.example.budgetpiggy.ui.home.HomePage
import com.example.budgetpiggy.ui.notifications.Notification
import com.example.budgetpiggy.R
import com.example.budgetpiggy.ui.reports.ReportsPage
import com.example.budgetpiggy.ui.wallet.WalletPage


class AccountManagement : BaseActivity() {

    private lateinit var firstNameEditText: EditText
    private lateinit var lastNameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.account_management)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.accountManagementPage)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        firstNameEditText = findViewById(R.id.firstNameEditText)
        lastNameEditText = findViewById(R.id.lastNameEditText)
        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)

        // Hide unnecessary icons for account management view
        findViewById<ImageView>(R.id.piggyIcon)?.visibility = View.GONE
        findViewById<ImageView>(R.id.streakIcon)?.visibility = View.GONE
        findViewById<TextView>(R.id.greetingText)?.visibility = View.GONE

        // Show and set the title to "account management"
        val pageTitle = findViewById<TextView>(R.id.pageTitle)
        pageTitle.visibility = View.VISIBLE
        pageTitle.text = getString(R.string.account_management)

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

    override fun onResume() {
        super.onResume()
        // Highlight profile/account icon
        setActiveNavIcon(findViewById(R.id.nav_profile))
    }

}