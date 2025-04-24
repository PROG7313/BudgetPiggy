package com.example.budgetpiggy

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

class AddAccountPage : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.addaccount)


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.addAccountPage)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        findViewById<ImageView>(R.id.piggyIcon)?.visibility = View.GONE
        findViewById<ImageView>(R.id.streakIcon)?.visibility = View.GONE
        findViewById<TextView>(R.id.greetingText)?.visibility = View.GONE


        val pageTitle = findViewById<TextView>(R.id.pageTitle)
        pageTitle.visibility = View.VISIBLE
        pageTitle.text = getString(R.string.add_account)


        val nameInput = findViewById<EditText>(R.id.accountNameInput)
        val descInput = findViewById<EditText>(R.id.accountDescInput)
        val allocInput= findViewById<EditText>(R.id.allocateInput)

        // show keyboard on focus for the amount field
        allocInput.setOnFocusChangeListener { v, has ->
            if (has) {
                (getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager)
                    .showSoftInput(v, InputMethodManager.SHOW_IMPLICIT)
            }
        }


        findViewById<View>(R.id.btnConfirm)?.setOnClickListener {
            // TODO: save to DB via Room
            Toast.makeText(this, "Account Created!", Toast.LENGTH_SHORT).show()
            finish()
        }
        val navWallet = findViewById<ImageView>(R.id.nav_wallet)
        val navReports = findViewById<ImageView>(R.id.nav_reports)
        val navProfile = findViewById<ImageView>(R.id.nav_profile)
        val backArrow = findViewById<ImageView>(R.id.backArrow)
        val bellIcon = findViewById<ImageView>(R.id.bellIcon)

        backArrow.setOnClickListener {
                view ->

            view.animate()
                .scaleX(0.95f)
                .scaleY(0.95f)
                .setDuration(25)
                .withEndAction {
                    view.animate().scaleX(1f).scaleY(1f).setDuration(25).start()
                    onBackPressedDispatcher.onBackPressed()
                }.start()

        }

        bellIcon.setOnClickListener {
            startActivity(Intent(this, Notification::class.java))
        }

        navWallet.setOnClickListener {
                view ->

            setActiveNavIcon(navWallet)
            view.animate()
                .scaleX(0.95f)
                .scaleY(0.95f)
                .setDuration(25)
                .withEndAction {
                    view.animate().scaleX(1f).scaleY(1f).setDuration(25).start()
                    startActivity(Intent(this, WalletPage::class.java))
                }.start()


        }
        navReports.setOnClickListener {
            setActiveNavIcon(navReports)
            startActivity(Intent(this, ReportsPage::class.java))
        }
        navProfile.setOnClickListener {
            setActiveNavIcon(navProfile)
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
