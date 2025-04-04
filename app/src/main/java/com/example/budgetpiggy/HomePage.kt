package com.example.budgetpiggy


import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class HomePage : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.home)

        //update notification badge
        // Find the top bar view (included layout)
        val topBar = findViewById<View>(R.id.topBar)

        // Set the notification count (example count is 3)
        updateNotificationBadge(topBar, 3)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.homePage)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val navWallet = findViewById<ImageView>(R.id.nav_wallet)
        val navReports = findViewById<ImageView>(R.id.nav_reports)
        val navProfile = findViewById<ImageView>(R.id.nav_profile)
        val backArrow = findViewById<ImageView>(R.id.backArrow)
        val bellIcon = findViewById<ImageView>(R.id.bellIcon)



        val scrollView = findViewById<ScrollView>(R.id.scrollArea)
        val fabWrapper = findViewById<View>(R.id.fabWrapper)

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
                    fabWrapper.animate()
                        .alpha(0f)
                        .setDuration(1)
                        .withEndAction { fabWrapper.visibility = View.GONE }
                        .start()
                }
            }

            lastScrollY = scrollY
        }




        /*
                navHome.setOnClickListener {
                    setActiveNavIcon(navHome)
                    startActivity(Intent(this, HomePage::class.java))
                }
                */

        backArrow.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        bellIcon.setOnClickListener {
            startActivity(Intent(this, Notification::class.java))
        }

        navWallet.setOnClickListener {
            setActiveNavIcon(navWallet)
             startActivity(Intent(this, WalletPage::class.java))
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
    override fun onResume() {
        super.onResume()
        setActiveNavIcon(findViewById(R.id.nav_home))
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
                setOnClickListener {
                    Toast.makeText(
                        this@HomePage,
                        "$progress out of $max",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            accountList.addView(row)
        }

        // Budget Remaining Items with click action on the progress bar
        val budgetData = listOf(
            Triple("Misc", 350, 600),
            Triple("Rent", 1500, 2000),
            Triple("Fuel", 200, 400),
            Triple("Rent", 1500, 2000),
            Triple("Rent", 1500, 2000),
            Triple("Rent", 1500, 2000),
            Triple("Rent", 1500, 2000),
            Triple("Rent", 1500, 2000),
            Triple("Rent", 1500, 2000),
            Triple("Rent", 1500, 2000),
            Triple("Rent", 1500, 2000),
            Triple("Rent", 1500, 2000),
            Triple("Misc", 350, 600),
            Triple("Rent", 1500, 2000),
            Triple("Fuel", 200, 400),
            Triple("Rent", 1500, 2000),
            Triple("Rent", 1500, 2000),
            Triple("Rent", 1500, 2000),
            Triple("Rent", 1500, 2000),
            Triple("Rent", 1500, 2000),
            Triple("Rent", 1500, 2000),
            Triple("Rent", 1500, 2000),
            Triple("Rent", 1500, 2000),
            Triple("Rent", 1500, 2000)
        )

        for ((label, progress, max) in budgetData) {
            val row = layoutInflater.inflate(R.layout.item_balance_row, budgetList, false)
            row.findViewById<TextView>(R.id.labelText).text = label
            row.findViewById<ProgressBar>(R.id.progressBar).apply {
                this.progress = progress
                this.max = max
                setOnClickListener {
                    Toast.makeText(
                        this@HomePage,
                        "$progress out of $max",
                        Toast.LENGTH_SHORT
                    ).show()
                }
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
