package com.example.budgetpiggy


import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat


class WalletPage : BaseActivity() {

    private lateinit var accountList: LinearLayout
    private lateinit var budgetList: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.wallet)
        val bellIcons = findViewById<ImageView>(R.id.bellIcon)
bellIcons.setOnClickListener {
    startActivity(Intent(this, Notification::class.java))
}

// Hide unnecessary icons for Wallet view
findViewById<ImageView>(R.id.piggyIcon)?.visibility = View.GONE
findViewById<ImageView>(R.id.streakIcon)?.visibility = View.GONE
findViewById<TextView>(R.id.greetingText)?.visibility = View.GONE

// Show and set the title to "Wallet"
val pageTitle = findViewById<TextView>(R.id.pageTitle)
pageTitle.visibility = View.VISIBLE
pageTitle.text = getString(R.string.wallet)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.walletPage)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        accountList = findViewById(R.id.accountList)
        budgetList = findViewById(R.id.budgetList)
        accountList.removeAllViews()
        budgetList.removeAllViews()

        val backArrow = findViewById<ImageView>(R.id.backArrow)

        val navHome = findViewById<ImageView>(R.id.nav_home)
        val navReports = findViewById<ImageView>(R.id.nav_reports)
        val navProfile = findViewById<ImageView>(R.id.nav_profile)
        val bellIcon = findViewById<ImageView>(R.id.bellIcon)
        bellIcon.setOnClickListener {
                view ->


            view.animate()
                .scaleX(0.95f)
                .scaleY(0.95f)
                .setDuration(25)
                .withEndAction {
                    view.animate().scaleX(1f).scaleY(1f).setDuration(25).start()
                    startActivity(Intent(this, Notification::class.java))
                }.start()

        }

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
        navHome.setOnClickListener {
                view ->

            setActiveNavIcon(navHome)
            view.animate()
                .scaleX(0.95f)
                .scaleY(0.95f)
                .setDuration(25)
                .withEndAction {
                    view.animate().scaleX(1f).scaleY(1f).setDuration(25).start()
                    startActivity(Intent(this, HomePage::class.java))
                }.start()


        }
        /*
        navWallet.setOnClickListener {
            setActiveNavIcon(navWallet)
            startActivity(Intent(this, WalletPage::class.java))
        }
        */

        navReports.setOnClickListener {
            setActiveNavIcon(navReports)
            // startActivity(Intent(this, ReportsPage::class.java))
        }
        navProfile.setOnClickListener {
            setActiveNavIcon(navProfile)
            //just for now it goes to about us page
            startActivity(Intent(this, AboutUsPage::class.java))
            // startActivity(Intent(this, ProfilePage::class.java))
        }
        val scrollView = findViewById<ScrollView>(R.id.walletScrollView)
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
        loadAccountTypes()
        loadBudgetCategories()
    }
    override fun onResume() {
        super.onResume()
        setActiveNavIcon(findViewById(R.id.nav_wallet))
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
//     Pair("SAVINGS", "R10000"),


    private fun loadAccountTypes() {
        val accounts = listOf(
            Pair("DEBIT", "R30000"),
            Pair("CHEQUE", "R30000")

        )

        accounts.forEach { (type, balance) ->
            val item = layoutInflater.inflate(R.layout.item_wallet_account_row, accountList, false)
            item.findViewById<TextView>(R.id.accountTypeText).text = type
            val balanceText = getString(R.string.account_balance_text, balance)
            item.findViewById<TextView>(R.id.accountBalanceText).text = balanceText
            item.findViewById<ImageView>(R.id.deleteIcon).setOnClickListener {
                showDeleteDialog {
                    accountList.removeView(item)
                    Toast.makeText(this, "$type removed", Toast.LENGTH_SHORT).show()
                }
            }
            accountList.addView(item)
        }
    }

    //  Triple("Vehicle", "R4000", "Savings"),
private fun loadBudgetCategories() {
    val budgets = listOf(
        Triple("Home", "R20000", "Debit"),
        Triple("Groceries", "R5000", "Cheque"),
        Triple("Home", "R20000", "Debit"),
        Triple("Groceries", "R5000", "Cheque"),
        Triple("Home", "R20000", "Debit"),
        Triple("Groceries", "R5000", "Cheque"),
        Triple("Home", "R20000", "Debit"),
        Triple("Groceries", "R5000", "Cheque"),
        Triple("Home", "R20000", "Debit"),
        Triple("Groceries", "R5000", "Cheque"),
        Triple("Home", "R20000", "Debit"),
        Triple("Groceries", "R5000", "Cheque"),
        Triple("Home", "R20000", "Debit"),
        Triple("Groceries", "R5000", "Cheque"),
        Triple("Home", "R20000", "Debit"),
        Triple("Groceries", "R5000", "Cheque"),
        Triple("Home", "R20000", "Debit"),
        Triple("Groceries", "R5000", "Cheque")
    )

    budgets.forEach { (category, amount, from) ->
        val item = layoutInflater.inflate(R.layout.item_wallet_budget_row, budgetList, false)
        item.findViewById<TextView>(R.id.categoryName).text = category
        val budgetAmountText = getString(R.string.budget_amount_text, amount)
        val budgetSourceText = getString(R.string.budget_source_text, from)
        item.findViewById<TextView>(R.id.budgetSource).text = budgetSourceText
        item.findViewById<TextView>(R.id.budgetAmount).text = budgetAmountText

        val icon = item.findViewById<ImageView>(R.id.categoryIcon)
        icon.setImageResource(when (category) {
            "Vehicle" -> R.drawable.vec_car
            "Home" -> R.drawable.vec_home_circle
            "Groceries" -> R.drawable.vec_food_circle
            else -> R.drawable.vec_wallet_inactive
        })

        item.findViewById<ImageView>(R.id.deleteIcon).setOnClickListener {
            showDeleteDialog {
                budgetList.removeView(item)
                Toast.makeText(this, "$category removed", Toast.LENGTH_SHORT).show()
            }
        }

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
