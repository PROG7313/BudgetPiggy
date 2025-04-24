package com.example.budgetpiggy

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class TransferFunds : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.transfer_funds)

        // Handle window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.transferFundsPage)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }
        listOf(R.id.greetingText, R.id.piggyIcon, R.id.streakBadge).forEach {
            findViewById<View>(it)?.visibility = View.GONE
        }
        // Top Bar
        findViewById<ImageView>(R.id.backArrow)?.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        findViewById<ImageView>(R.id.bellIcon)?.setOnClickListener {
            startActivity(Intent(this, Notification::class.java))
        }

        // Bottom Navigation
        setupBottomNav()

        // Toggle Buttons
        val btnTransfer = findViewById<Button>(R.id.btnTransferFunds)
        val btnMakeTxn  = findViewById<Button>(R.id.btnMakeTransaction)
        btnTransfer.setOnClickListener {
            setToggleButtons(btnTransfer, btnMakeTxn)
        }
        btnMakeTxn.setOnClickListener {
            setToggleButtons(btnMakeTxn, btnTransfer)
            // navigate to Transaction screen
          //  startActivity(Intent(this, TransactionActivity::class.java))
        }

        // Amount Input → show system keyboard
        val amountInput = findViewById<EditText>(R.id.amountInput)
        amountInput.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                (getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager)
                    .showSoftInput(v, InputMethodManager.SHOW_IMPLICIT)
            }
        }

        // Account Spinners
        val spinnerFrom = findViewById<Spinner>(R.id.spinnerFromAccount)
        val spinnerTo   = findViewById<Spinner>(R.id.spinnerToAccount)
        val accounts    = listOf("Savings", "Debit", "Cheque")
        spinnerFrom.adapter = ArrayAdapter(this,
            android.R.layout.simple_spinner_dropdown_item, accounts)
        spinnerTo  .adapter = ArrayAdapter(this,
            android.R.layout.simple_spinner_dropdown_item, accounts)

        // Category pickers (scrollable + highlight)
        val catFrom = findViewById<LinearLayout>(R.id.categoryFromList)
        val catTo   = findViewById<LinearLayout>(R.id.categoryToList)
        fun fillCats(container: LinearLayout) {
            container.removeAllViews()
            val icons = listOf(
                R.drawable.vec_car,
                R.drawable.vec_food_circle,
                R.drawable.vec_gift_circle
            )
            icons.forEach { resId ->
                val iv = ImageView(this).apply {
                    setImageResource(resId)
                    val pad = (8 * resources.displayMetrics.density).toInt()
                    setPadding(pad,pad,pad,pad)
                    setOnClickListener {
                        // clear previous
                        for (i in 0 until container.childCount) {
                            container.getChildAt(i).background = null
                        }
                        // highlight this
                        setBackgroundResource(R.drawable.bg_selected_category)
                    }
                }
                container.addView(iv)
            }
        }
        fillCats(catFrom)
        fillCats(catTo)

        // Confirm button
        findViewById<Button>(R.id.btnConfirm).setOnClickListener {
            // TODO: read fields and perform transfer
            Toast.makeText(this, "Transfer Confirmed!", Toast.LENGTH_SHORT).show()
        }

        // Start with Transfer active
        setToggleButtons(btnTransfer, btnMakeTxn)
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
    private fun setupBottomNav() {
        val navMap = mapOf(
            R.id.nav_home    to HomePage::class.java,
            R.id.nav_wallet  to WalletPage::class.java,
            R.id.nav_reports to ReportsPage::class.java,
            //R.id.nav_profile to ProfilePage::class.java
        )
        navMap.forEach { (id, cls) ->
            findViewById<ImageView>(id)?.setOnClickListener { icon ->
                setActiveNavIcon(icon as ImageView)
                startActivity(Intent(this, cls))
            }
        }
        // Highlight Wallet icon by default
      //  findViewById<ImageView>(R.id.nav_wallet)?.let { setActiveNavIcon(it) }
    }

    private fun setToggleButtons(active: Button, inactive: Button) {
        active.background   = resources.getDrawable(R.drawable.bg_toggle_active, null)
        active.setTextColor(resources.getColor(android.R.color.white, null))
        inactive.background = resources.getDrawable(R.drawable.bg_toggle_inactive, null)
        inactive.setTextColor(resources.getColor(R.color.black, null))
    }
}
