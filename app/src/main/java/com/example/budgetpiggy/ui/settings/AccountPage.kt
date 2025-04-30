package com.example.budgetpiggy.ui.settings

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.budgetpiggy.R
import com.example.budgetpiggy.data.database.AppDatabase
import com.example.budgetpiggy.ui.core.BaseActivity
import com.example.budgetpiggy.ui.core.SplashActivity
import com.example.budgetpiggy.ui.home.HomePage
import com.example.budgetpiggy.ui.notifications.Notification
import com.example.budgetpiggy.ui.reports.ReportsPage
import com.example.budgetpiggy.ui.rewards.RewardsActivity
import com.example.budgetpiggy.ui.transaction.TransactionHistory
import com.example.budgetpiggy.ui.wallet.WalletPage
import com.example.budgetpiggy.utils.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AccountPage : BaseActivity() {

    private lateinit var ivAvatar: ImageView
    private lateinit var tvUserName: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.account)

        // Insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.accountPage)) { v, insets ->
            val sys = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(sys.left, sys.top, sys.right, sys.bottom)
            insets
        }

        // Hide unused icons
        findViewById<ImageView>(R.id.piggyIcon)?.visibility = View.GONE
        findViewById<ImageView>(R.id.streakIcon)?.visibility = View.GONE
        findViewById<TextView>(R.id.greetingText)?.visibility = View.GONE

        // Title
        findViewById<TextView>(R.id.pageTitle).apply {
            visibility = View.VISIBLE
            text = getString(R.string.account)
        }

        // Bind avatar & username
        ivAvatar    = findViewById(R.id.ivAvatar)
        tvUserName  = findViewById(R.id.tvUserName)

        // Load user from DB
        val userId = SessionManager.getUserId(this) ?: return
        lifecycleScope.launch(Dispatchers.IO) {
            val user = AppDatabase
                .getDatabase(this@AccountPage)
                .userDao()
                .getById(userId)

            withContext(Dispatchers.Main) {
                tvUserName.text = user?.firstName ?: ""

                // display profile picture
                user?.profilePictureLocalPath?.let { path ->
                    ivAvatar.setImageURI(path.toUri())
                } ?: user?.profilePictureUrl?.let { url ->
                    ivAvatar.setImageURI(url.toUri())
                }
            }
        }

        // Bell → Notifications
        findViewById<ImageView>(R.id.bellIcon).setOnClickListener { v ->
            v.animate()
                .scaleX(0.95f).scaleY(0.95f).setDuration(25)
                .withEndAction {
                    v.animate().scaleX(1f).scaleY(1f).setDuration(25).start()
                    startActivity(Intent(this, Notification::class.java))
                }
                .start()
        }

        // Back arrow
        findViewById<ImageView>(R.id.backArrow).setOnClickListener { v ->
            v.animate()
                .scaleX(0.95f).scaleY(0.95f).setDuration(25)
                .withEndAction {
                    v.animate().scaleX(1f).scaleY(1f).setDuration(25).start()
                    onBackPressedDispatcher.onBackPressed()
                }
                .start()
        }

        // Section buttons (CodingStuff, 2024).
        findViewById<LinearLayout>(R.id.AccountManagement).setOnClickListener {
            startActivity(Intent(this, AccountManagement::class.java))
        }
        findViewById<LinearLayout>(R.id.ManageAccounts).setOnClickListener {
            startActivity(Intent(this, WalletPage::class.java))
        }
        findViewById<LinearLayout>(R.id.TransactionHistory).setOnClickListener {
            startActivity(Intent(this, TransactionHistory::class.java))
        }
        findViewById<LinearLayout>(R.id.Rewards).setOnClickListener {
            startActivity(Intent(this, RewardsActivity::class.java))
        }
        findViewById<LinearLayout>(R.id.feedback).setOnClickListener {
            startActivity(Intent(this, SendFeedbackPage::class.java))
        }
        findViewById<LinearLayout>(R.id.help).setOnClickListener {
            startActivity(Intent(this, HelpPage::class.java))
        }
        findViewById<LinearLayout>(R.id.about).setOnClickListener {
            startActivity(Intent(this, AboutUsPage::class.java))
        }
        findViewById<LinearLayout>(R.id.logout).setOnClickListener {
            SessionManager.logout(this)
            Intent(this, SplashActivity::class.java).also {
                it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(it)
            }
            finish()
        }
        findViewById<LinearLayout>(R.id.currency).setOnClickListener {
            startActivity(Intent(this, ChangeCurrency::class.java))
        }

        // Bottom nav
        findViewById<ImageView>(R.id.nav_home).setOnClickListener { v ->
            setActiveNavIcon(v as ImageView)
            startActivity(Intent(this, HomePage::class.java))
        }
        findViewById<ImageView>(R.id.nav_wallet).setOnClickListener { v ->
            setActiveNavIcon(v as ImageView)
            startActivity(Intent(this, WalletPage::class.java))
        }
        findViewById<ImageView>(R.id.nav_reports).setOnClickListener { v ->
            setActiveNavIcon(v as ImageView)
            startActivity(Intent(this, ReportsPage::class.java))
        }
        findViewById<ImageView>(R.id.nav_profile).setOnClickListener { v ->
            setActiveNavIcon(v as ImageView)
            // already here
        }
    }

    override fun onResume() {
        super.onResume()
        setActiveNavIcon(findViewById(R.id.nav_profile))
    }

    override fun setActiveNavIcon(activeIcon: ImageView) {
        val navMap = listOf(
            R.id.nav_home    to R.drawable.vec_home_inactive,
            R.id.nav_wallet  to R.drawable.vec_wallet_inactive,
            R.id.nav_reports to R.drawable.vec_reports_inactive,
            R.id.nav_profile to R.drawable.vec_profile_inactive
        )
        navMap.forEach { (id, drawable) ->
            findViewById<ImageView>(id).setImageResource(drawable)
        }
        when (activeIcon.id) {
            R.id.nav_home    -> activeIcon.setImageResource(R.drawable.vec_home_active)
            R.id.nav_wallet  -> activeIcon.setImageResource(R.drawable.vec_wallet_active)
            R.id.nav_reports -> activeIcon.setImageResource(R.drawable.vec_reports_active)
            R.id.nav_profile -> activeIcon.setImageResource(R.drawable.vec_profile_active)
        }
    }
}
