package com.example.budgetpiggy.ui.wallet

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.budgetpiggy.ui.settings.AccountPage
import com.example.budgetpiggy.ui.core.BaseActivity
import com.example.budgetpiggy.ui.home.HomePage
import com.example.budgetpiggy.ui.notifications.Notification
import com.example.budgetpiggy.R
import com.example.budgetpiggy.data.database.AppDatabase
import com.example.budgetpiggy.data.entities.AccountEntity
import com.example.budgetpiggy.ui.reports.ReportsPage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class AddAccountPage : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.addaccount)

        // Handle system-bar insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.addAccountPage)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }
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
        findViewById<ImageView>(R.id.piggyIcon).visibility = View.GONE
        findViewById<TextView>(R.id.greetingText).visibility = View.GONE
        findViewById<ImageView>(R.id.streakIcon).visibility = View.GONE

        val pageTitle = findViewById<TextView>(R.id.pageTitle)
        pageTitle.visibility = View.VISIBLE
        pageTitle.text = getString(R.string.add_account)

        // Page title
        findViewById<TextView>(R.id.pageTitle).apply {
            visibility = View.VISIBLE
            text = getString(R.string.add_account)
        }

        // Inputs
        val nameInput  = findViewById<EditText>(R.id.accountNameInput)
        val typeInput  = findViewById<EditText>(R.id.accountDescInput)
        val allocInput = findViewById<EditText>(R.id.allocateInput)

        // Show keyboard on amount focus
        allocInput.setOnFocusChangeListener { v, has ->
            if (has) {
                (getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager)
                    .showSoftInput(v, InputMethodManager.SHOW_IMPLICIT)
            }
        }

        // Confirm button: validate & insert into Room
        findViewById<View>(R.id.btnConfirm)?.setOnClickListener {
            val name       = nameInput.text.toString().trim()
            val type       = typeInput.text.toString().trim()
            val balance    = allocInput.text.toString().trim().toDoubleOrNull() ?: 0.0

            if (name.isEmpty()) {
                Toast.makeText(this, "Please enter an account name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (type.isEmpty()) {
                Toast.makeText(this, "Please enter an account type", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // get userId from prefs
            val prefs  = getSharedPreferences("app_prefs", MODE_PRIVATE)
            val userId = prefs.getString("logged_in_user_id", null)
            if (userId == null) {
                Toast.makeText(this, "No logged-in user!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    val dao = AppDatabase
                        .getDatabase(this@AddAccountPage)
                        .accountDao()

                    val account = AccountEntity(
                        accountId   = UUID.randomUUID().toString(),
                        userId      = userId,
                        accountName = name,
                        balance     = balance,
                        type        = type,
                        createdAt   = System.currentTimeMillis()
                    )
                    dao.insert(account)
                }

                // back on main thread
                Toast.makeText(this@AddAccountPage,
                    "Account Created!", Toast.LENGTH_SHORT).show()
                finish()  // returns to WalletPage
            }
        }

        // Back arrow
        findViewById<ImageView>(R.id.backArrow)?.setOnClickListener { view ->
            view.animate()
                .scaleX(0.95f).scaleY(0.95f).setDuration(25)
                .withEndAction {
                    view.animate().scaleX(1f).scaleY(1f).setDuration(25).start()
                    onBackPressedDispatcher.onBackPressed()
                }.start()
        }

        // Bell icon
        findViewById<ImageView>(R.id.bellIcon)?.setOnClickListener {
            startActivity(Intent(this, Notification::class.java))
        }

        // Bottom nav: Wallet
        findViewById<ImageView>(R.id.nav_wallet)?.setOnClickListener { view ->
            setActiveNavIcon(findViewById(R.id.nav_wallet))
            view.animate()
                .scaleX(0.95f).scaleY(0.95f).setDuration(25)
                .withEndAction {
                    view.animate().scaleX(1f).scaleY(1f).setDuration(25).start()
                    startActivity(Intent(this, WalletPage::class.java))
                }.start()
        }
        // Bottom nav: Reports & Profile
        findViewById<ImageView>(R.id.nav_reports)?.setOnClickListener {
            setActiveNavIcon(findViewById(R.id.nav_reports))
            startActivity(Intent(this, ReportsPage::class.java))
        }
        findViewById<ImageView>(R.id.nav_profile)?.setOnClickListener {
            setActiveNavIcon(findViewById(R.id.nav_profile))
            startActivity(Intent(this, AccountPage::class.java))
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
