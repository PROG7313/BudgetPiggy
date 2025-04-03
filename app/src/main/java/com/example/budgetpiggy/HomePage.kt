package com.example.budgetpiggy

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class HomePage : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.home)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.homePage)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val navHome = findViewById<ImageView>(R.id.nav_home)
        val navWallet = findViewById<ImageView>(R.id.nav_wallet)
        val navReports = findViewById<ImageView>(R.id.nav_reports)
        val navProfile = findViewById<ImageView>(R.id.nav_profile)
        val backArrow = findViewById<ImageView>(R.id.backArrow)
        setActiveNavIcon(navHome)

        navHome.setOnClickListener {
            setActiveNavIcon(navHome)
            val nextIntent = Intent(this, HomePage::class.java)
            val nextOptions = ActivityOptions.makeCustomAnimation(
                this,
                R.anim.fade_in,
                R.anim.fade_out
            )

            startActivity(nextIntent, nextOptions.toBundle())
        }
        backArrow.setOnClickListener {

            onBackPressedDispatcher.onBackPressed()
        }
        navWallet.setOnClickListener {
            setActiveNavIcon(navWallet)
            // startActivity(Intent(this, WalletPage::class.java))
        }

        navReports.setOnClickListener {
            setActiveNavIcon(navReports)
            // startActivity(Intent(this, ReportsPage::class.java))
        }

        navProfile.setOnClickListener {
            setActiveNavIcon(navProfile)
            // startActivity(Intent(this, ProfilePage::class.java))
        }
        addSampleData()
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

    private fun addSampleData() {
        val accountList = findViewById<LinearLayout>(R.id.accountBalanceList)
        val budgetList = findViewById<LinearLayout>(R.id.budgetRemainingList)
        val transactionList = findViewById<LinearLayout>(R.id.transactionList)

        // Account Balance Items
        val accountData = listOf(
            Triple("Savings", 400, 1000),
            Triple("Debit", 800, 1000),
            Triple("Cheque", 200, 1000)
        )

        for ((label, progress, max) in accountData) {
            val row = layoutInflater.inflate(R.layout.item_balance_row, accountList, false)
            row.findViewById<TextView>(R.id.labelText).text = label
            row.findViewById<ProgressBar>(R.id.progressBar).apply {
                this.progress = progress
                this.max = max
            }
            accountList.addView(row)
        }

        // Budget Remaining Items
        val budgetData = listOf(
            Triple("Misc", 350, 600),
            Triple("Rent", 1500, 2000),
            Triple("Fuel", 200, 400)
        )

        for ((label, progress, max) in budgetData) {
            val row = layoutInflater.inflate(R.layout.item_balance_row, budgetList, false)
            row.findViewById<TextView>(R.id.labelText).text = label
            row.findViewById<ProgressBar>(R.id.progressBar).apply {
                this.progress = progress
                this.max = max
            }
            budgetList.addView(row)
        }

        // Transactions (limit 5)
        val transactions = listOf(
            Triple("Food", "Steers", "-R250"),
            Triple("Fuel", "Engen", "-R800"),
            Triple("Salary", "Monthly", "+R10000")
        )

        for ((category, name, amount) in transactions) {
            val row = layoutInflater.inflate(R.layout.item_transaction_row, transactionList, false)
            row.findViewById<TextView>(R.id.categoryText).text = category
            row.findViewById<TextView>(R.id.nameText).text = name
            row.findViewById<TextView>(R.id.amountText).text = amount
            transactionList.addView(row)
        }
    }
}
