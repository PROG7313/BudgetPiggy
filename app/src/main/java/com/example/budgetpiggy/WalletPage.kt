package com.example.budgetpiggy

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class WalletPage : BaseActivity() {

    private lateinit var accountList: LinearLayout
    private lateinit var budgetList: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.wallet)

        // Edge-to-edge insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.walletPage)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        // Hide home-only icons
        findViewById<ImageView>(R.id.piggyIcon)?.visibility   = View.GONE
        findViewById<ImageView>(R.id.streakIcon)?.visibility  = View.GONE
        findViewById<TextView>(R.id.greetingText)?.visibility = View.GONE

        // Show page title “Wallet”
        findViewById<TextView>(R.id.pageTitle).apply {
            visibility = View.VISIBLE
            text = getString(R.string.wallet)
        }

        // Top-bar bell → Notifications
        findViewById<ImageView>(R.id.bellIcon)
            .setOnClickListener { v ->
                v.animate()
                    .scaleX(0.95f).scaleY(0.95f).setDuration(25)
                    .withEndAction {
                        v.animate().scaleX(1f).scaleY(1f).setDuration(25).start()
                        startActivity(Intent(this, Notification::class.java))
                    }
                    .start()
            }

        // Top-bar back arrow
        findViewById<ImageView>(R.id.backArrow)
            .setOnClickListener { v ->
                v.animate()
                    .scaleX(0.95f).scaleY(0.95f).setDuration(25)
                    .withEndAction {
                        v.animate().scaleX(1f).scaleY(1f).setDuration(25).start()
                        onBackPressedDispatcher.onBackPressed()
                    }
                    .start()
            }

        // Lists
        accountList = findViewById(R.id.accountList)
        budgetList  = findViewById(R.id.budgetList)
        accountList.removeAllViews()
        budgetList.removeAllViews()

        // Bottom nav wiring
        setupBottomNav()

        // Scroll behavior for FAB
        val scrollView  = findViewById<ScrollView>(R.id.walletScrollView)
        val fabWrapper  = findViewById<View>(R.id.fabWrapper)
        var lastScrollY = 0
        scrollView.viewTreeObserver.addOnScrollChangedListener {
            val y        = scrollView.scrollY
            val showFab  = (y < lastScrollY) || !scrollView.canScrollVertically(-1) || !scrollView.canScrollVertically(1)
            if (showFab) {
                fabWrapper.visibility = View.VISIBLE
                fabWrapper.animate().alpha(1f).setDuration(1).start()
            } else {
                fabWrapper.animate().alpha(0f).setDuration(1)
                    .withEndAction { fabWrapper.visibility = View.GONE }
                    .start()
            }
            lastScrollY = y
        }

        loadAccountTypes()
        loadBudgetCategories()
    }

    override fun onResume() {
        super.onResume()
        // Highlight Wallet icon
        setActiveNavIcon(findViewById(R.id.nav_wallet))
    }

    private fun setupBottomNav() {
        val navHome    = findViewById<ImageView>(R.id.nav_home)
        val navWallet  = findViewById<ImageView>(R.id.nav_wallet)
        val navReports = findViewById<ImageView>(R.id.nav_reports)
        val navProfile = findViewById<ImageView>(R.id.nav_profile)

        navHome.setOnClickListener { v ->
            setActiveNavIcon(v as ImageView)
            startActivity(Intent(this, HomePage::class.java))
        }
        navWallet.setOnClickListener { v ->
            setActiveNavIcon(v as ImageView)
            // Already here
        }
        navReports.setOnClickListener { v ->
            setActiveNavIcon(v as ImageView)
            startActivity(Intent(this, ReportsPage::class.java))
        }
        navProfile.setOnClickListener { v ->
            setActiveNavIcon(v as ImageView)
            startActivity(Intent(this, AccountPage::class.java))
        }
    }

    private fun loadAccountTypes() {
        listOf("DEBIT" to "R30000", "CHEQUE" to "R30000").forEach { (type, bal) ->
            val item = layoutInflater.inflate(R.layout.item_wallet_account_row, accountList, false)
            item.findViewById<TextView>(R.id.accountTypeText).text    = type
            item.findViewById<TextView>(R.id.accountBalanceText).text = getString(R.string.account_balance_text, bal)
            item.findViewById<ImageView>(R.id.deleteIcon)
                .setOnClickListener { showDeleteDialog {
                    accountList.removeView(item)
                    Toast.makeText(this, "$type removed", Toast.LENGTH_SHORT).show()
                }}
            accountList.addView(item)
        }
    }

    private fun loadBudgetCategories() {
        val data = listOf(
            "Home" to "R20000" to "Debit",
            "Groceries" to "R5000" to "Cheque",
            // … repeat as needed …
        )
        data.forEach { (pair, from) ->
            val (category, amount) = pair
            val item = layoutInflater.inflate(R.layout.item_wallet_budget_row, budgetList, false)
            item.findViewById<TextView>(R.id.categoryName).text   = category
            item.findViewById<TextView>(R.id.budgetAmount).text   = getString(R.string.budget_amount_text, amount)
            item.findViewById<TextView>(R.id.budgetSource).text   = getString(R.string.budget_source_text, from)
            val icon = item.findViewById<ImageView>(R.id.categoryIcon)
            icon.setImageResource(when (category) {
                "Vehicle"   -> R.drawable.vec_car
                "Home"      -> R.drawable.vec_home_circle
                "Groceries" -> R.drawable.vec_food_circle
                else        -> R.drawable.vec_wallet_inactive
            })
            item.findViewById<ImageView>(R.id.deleteIcon)
                .setOnClickListener { showDeleteDialog {
                    budgetList.removeView(item)
                    Toast.makeText(this, "$category removed", Toast.LENGTH_SHORT).show()
                }}
            budgetList.addView(item)
        }
    }

    private fun showDeleteDialog(onConfirm: () -> Unit) {
        AlertDialog.Builder(this)
            .setTitle("Confirm Deletion")
            .setMessage("Are you sure you want to delete this item?")
            .setPositiveButton("Yes") { _, _ -> onConfirm() }
            .setNegativeButton("No", null)
            .show()
    }
}
