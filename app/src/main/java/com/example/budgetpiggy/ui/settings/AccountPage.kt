package com.example.budgetpiggy.ui.settings

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.budgetpiggy.ui.core.BaseActivity
import com.example.budgetpiggy.R
import com.example.budgetpiggy.data.database.AppDatabase
import com.example.budgetpiggy.ui.core.SplashActivity
import com.example.budgetpiggy.ui.home.HomePage
import com.example.budgetpiggy.ui.notifications.Notification
import com.example.budgetpiggy.ui.reports.ReportsPage
import com.example.budgetpiggy.ui.transaction.TransactionHistory
import com.example.budgetpiggy.ui.wallet.WalletPage
import com.example.budgetpiggy.utils.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AccountPage : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.account)

        // Edge-to-edge insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.accountPage)) { v, insets ->
            val sys = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(sys.left, sys.top, sys.right, sys.bottom)
            insets
        }

        findViewById<ImageView>(R.id.piggyIcon)?.visibility = View.GONE
        findViewById<ImageView>(R.id.streakIcon)?.visibility = View.GONE
        findViewById<TextView>(R.id.greetingText)?.visibility = View.GONE
        val tvUserName = findViewById<TextView>(R.id.tvUserName)
        val userId = SessionManager.getUserId(this) ?: return
        userId.let { id ->
            lifecycleScope.launch(Dispatchers.IO) {
                val user = AppDatabase.getDatabase(this@AccountPage).userDao().getById(id)
                withContext(Dispatchers.Main) {
                    tvUserName.text = "${user?.firstName}"
                }
            }
        }

// Show and set the title to "Wallet"
        findViewById<TextView>(R.id.pageTitle).apply {
            visibility = View.VISIBLE
            text = getString(R.string.account)
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
        val accountManage = findViewById<LinearLayout>(R.id.AccountManagement)
        val feedback = findViewById<LinearLayout>(R.id.feedback)
        val transactionHistory = findViewById<LinearLayout>(R.id.TransactionHistory)

        transactionHistory.setOnClickListener{
            startActivity(Intent(this, TransactionHistory::class.java))
        }
        accountManage.setOnClickListener{
            startActivity(Intent(this, AccountManagement::class.java))
        }
        feedback.setOnClickListener{
            startActivity(Intent(this, SendFeedbackPage::class.java))
        }

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

    override fun onResume() {
        super.onResume()
        // Highlight profile/account icon
        setActiveNavIcon(findViewById(R.id.nav_profile))
    }
}
