package com.example.budgetpiggy.ui.transaction

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.budgetpiggy.ui.core.BaseActivity
import com.example.budgetpiggy.ui.home.HomePage
import com.example.budgetpiggy.ui.notifications.Notification
import com.example.budgetpiggy.R
import com.example.budgetpiggy.ui.wallet.WalletPage
import com.example.budgetpiggy.ui.reports.ReportsPage

class TransferFunds : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.transfer_funds)
        findViewById<ImageView>(R.id.piggyIcon).visibility = View.GONE
        findViewById<TextView>(R.id.greetingText).visibility = View.GONE
        findViewById<ImageView>(R.id.streakIcon).visibility = View.GONE

        val pageTitle = findViewById<TextView>(R.id.pageTitle)
        pageTitle.visibility = View.VISIBLE
        pageTitle.text = getString(R.string.transfer_funds)

        // 1) Edge-to-edge insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.transferFundsPage)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }


        // 3) Top bar
        findViewById<ImageView>(R.id.backArrow)
            ?.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
        findViewById<ImageView>(R.id.bellIcon)
            ?.setOnClickListener { startActivity(Intent(this, Notification::class.java)) }

        // 4) Bottom nav
        setupBottomNav()

        // 5) Main toggle
        val btnTransfer = findViewById<Button>(R.id.btnTransferFunds)
        val btnTxn      = findViewById<Button>(R.id.btnMakeTransaction)
        btnTransfer.setOnClickListener {
            setToggleButtons(btnTransfer, btnTxn)
        }
        btnTxn.setOnClickListener {
            setToggleButtons(btnTxn, btnTransfer)
             startActivity(Intent(this, TransactionActivity::class.java))
        }

        // 6) Amount → system keyboard
        val amountInput = findViewById<EditText>(R.id.amountInput)
        amountInput.setOnFocusChangeListener { v, has ->
            if (has) {
                (getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager)
                    .showSoftInput(v, InputMethodManager.SHOW_IMPLICIT)
            }
        }

        // 7) Sub-mode selector
        val btnModeAccount  = findViewById<Button>(R.id.btnModeAccount)
        val btnModeCategory = findViewById<Button>(R.id.btnModeCategory)
        val groupAccToAcc   = findViewById<LinearLayout>(R.id.groupAccountToAccount)
        val groupAccToCat   = findViewById<LinearLayout>(R.id.groupAccountToCategory)

        btnModeAccount.setOnClickListener {
            setToggleButtons(btnModeAccount, btnModeCategory)
            groupAccToAcc.visibility = View.VISIBLE
            groupAccToCat.visibility = View.GONE
        }
        btnModeCategory.setOnClickListener {
            setToggleButtons(btnModeCategory, btnModeAccount)
            groupAccToAcc.visibility = View.GONE
            groupAccToCat.visibility = View.VISIBLE
        }

        // 8) Populate spinners
        val accounts = listOf("Savings", "Debit", "Cheque")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, accounts)

        findViewById<Spinner>(R.id.spinnerFromAccountAcc).adapter = adapter
        findViewById<Spinner>(R.id.spinnerToAccountAcc).adapter   = adapter
        findViewById<Spinner>(R.id.spinnerFromAccountCat).adapter = adapter

        // 9) Populate categories list for Account→Category
        val catContainer = findViewById<LinearLayout>(R.id.categoryToList)
        fun fillCategories(container: LinearLayout) {
            container.removeAllViews()
            listOf(R.drawable.vec_car, R.drawable.vec_food, R.drawable.vec_gift)
                .forEach { resId ->
                    val iv = ImageView(this).apply {
                        setImageResource(resId)
                        val pad = (8 * resources.displayMetrics.density).toInt()
                        setPadding(pad, pad, pad, pad)
                        setOnClickListener {
                            // deselect
                            for (i in 0 until container.childCount) {
                                container.getChildAt(i).background = null
                            }
                            // select
                            background = resources.getDrawable(R.drawable.bg_selected_category, null)
                        }
                    }
                    container.addView(iv)
                }
        }
        fillCategories(catContainer)

        // 10) Confirm
        findViewById<Button>(R.id.btnConfirm).setOnClickListener {
            Toast.makeText(this, "Transfer Confirmed!", Toast.LENGTH_SHORT).show()
        }

        // 11) Defaults
        btnTransfer.performClick()    // main mode
        btnModeAccount.performClick() // sub-mode
    }

    private fun setToggleButtons(active: Button, inactive: Button) {
        active.background   = resources.getDrawable(R.drawable.bg_toggle_active, null)
        active.setTextColor(resources.getColor(android.R.color.white, null))
        inactive.background = resources.getDrawable(R.drawable.bg_toggle_inactive, null)
        inactive.setTextColor(resources.getColor(R.color.black, null))
    }

    private fun setupBottomNav() {
        val navMap = mapOf(
            R.id.nav_home to HomePage::class.java,
            R.id.nav_wallet to WalletPage::class.java,
            R.id.nav_reports to ReportsPage::class.java
            // R.id.nav_profile to ProfilePage::class.java
        )
        navMap.forEach { (id, cls) ->
            findViewById<ImageView>(id)?.setOnClickListener { icon ->
                setActiveNavIcon(icon as ImageView)
                startActivity(Intent(this, cls))
            }
        }

    }

    override fun setActiveNavIcon(activeIcon: ImageView) {
        val nav = listOf(
            R.id.nav_home to R.drawable.vec_home_inactive,
            R.id.nav_wallet to R.drawable.vec_wallet_inactive,
            R.id.nav_reports to R.drawable.vec_reports_inactive,
            R.id.nav_profile to R.drawable.vec_profile_inactive
        )
        nav.forEach { (id, drawable) ->
            findViewById<ImageView>(id).setImageResource(drawable)
        }
        when (activeIcon.id) {
            R.id.nav_home -> activeIcon.setImageResource(R.drawable.vec_home_active)
            R.id.nav_wallet -> activeIcon.setImageResource(R.drawable.vec_wallet_active)
            R.id.nav_reports -> activeIcon.setImageResource(R.drawable.vec_reports_active)
            R.id.nav_profile -> activeIcon.setImageResource(R.drawable.vec_profile_active)
        }
    }
}
