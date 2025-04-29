package com.example.budgetpiggy.ui.transaction

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.children
import androidx.lifecycle.lifecycleScope
import com.example.budgetpiggy.ui.core.BaseActivity
import com.example.budgetpiggy.ui.home.HomePage
import com.example.budgetpiggy.ui.notifications.Notification
import com.example.budgetpiggy.R
import com.example.budgetpiggy.data.database.AppDatabase
import com.example.budgetpiggy.ui.wallet.WalletPage
import com.example.budgetpiggy.ui.reports.ReportsPage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.core.net.toUri
import com.example.budgetpiggy.data.entities.TransactionEntity
import java.util.UUID

class TransferFunds : BaseActivity() {
    private var selectedFromAccountId: String? = null
    private var selectedToAccountId: String? = null
    private var selectedCategoryId: String? = null
    private var selectedAccountForCategoryId: String? = null
    private var isAccountToAccountMode: Boolean = true

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

        //  Edge-to-edge insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.transferFundsPage)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }


        //  Top bar
        findViewById<ImageView>(R.id.backArrow)
            ?.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
        findViewById<ImageView>(R.id.bellIcon)
            ?.setOnClickListener { startActivity(Intent(this, Notification::class.java)) }

        //  Bottom nav
        setupBottomNav()

        //  Main toggle
        val btnTransfer = findViewById<Button>(R.id.btnTransferFunds)
        val btnTxn      = findViewById<Button>(R.id.btnMakeTransaction)
        btnTransfer.setOnClickListener {
            setToggleButtons(btnTransfer, btnTxn)
        }
        btnTxn.setOnClickListener {
            setToggleButtons(btnTxn, btnTransfer)
             startActivity(Intent(this, TransactionActivity::class.java))
        }

        // Amount → system keyboard
        val amountInput = findViewById<EditText>(R.id.amountInput)
        amountInput.setOnFocusChangeListener { v, has ->
            if (has) {
                (getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager)
                    .showSoftInput(v, InputMethodManager.SHOW_IMPLICIT)
            }
        }

        //  Sub-mode selector
        val btnModeAccount  = findViewById<Button>(R.id.btnModeAccount)
        val btnModeCategory = findViewById<Button>(R.id.btnModeCategory)
        val groupAccToAcc   = findViewById<LinearLayout>(R.id.groupAccountToAccount)
        val groupAccToCat   = findViewById<LinearLayout>(R.id.groupAccountToCategory)

        btnModeAccount.setOnClickListener {
            isAccountToAccountMode = true
            setToggleButtons(btnModeAccount, btnModeCategory)
            groupAccToAcc.visibility = View.VISIBLE
            groupAccToCat.visibility = View.GONE
        }

        btnModeCategory.setOnClickListener {
            isAccountToAccountMode = false
            setToggleButtons(btnModeCategory, btnModeAccount)
            groupAccToAcc.visibility = View.GONE
            groupAccToCat.visibility = View.VISIBLE
        }

        val spinnerFromAcc = findViewById<Spinner>(R.id.spinnerFromAccountAcc)
        val spinnerToAcc = findViewById<Spinner>(R.id.spinnerToAccountAcc)
        val spinnerFromCat = findViewById<Spinner>(R.id.spinnerFromAccountCat)
        lifecycleScope.launch {
            val userId = getSharedPreferences("app_piggy_prefs", MODE_PRIVATE)
                .getString("logged_in_user_id", null) ?: return@launch

            val accounts = withContext(Dispatchers.IO) {
                AppDatabase.getDatabase(this@TransferFunds).accountDao().getByUserId(userId)
            }

            val accountNames = accounts.map { it.accountName }
            val adapter = ArrayAdapter(this@TransferFunds, android.R.layout.simple_spinner_dropdown_item, accountNames)

            spinnerFromAcc.adapter = adapter
            spinnerToAcc.adapter = adapter
            spinnerFromCat.adapter = adapter

            spinnerFromAcc.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    selectedFromAccountId = accounts[position].accountId
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }

            spinnerToAcc.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    selectedToAccountId = accounts[position].accountId
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }

            spinnerFromCat.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    selectedAccountForCategoryId = accounts[position].accountId // for Account→Category
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }

        }

//  Populate categories dynamically
        val catContainer = findViewById<LinearLayout>(R.id.categoryToList)

        lifecycleScope.launch {
            val userId = getSharedPreferences("app_piggy_prefs", MODE_PRIVATE)
                .getString("logged_in_user_id", null) ?: return@launch

            val categories = withContext(Dispatchers.IO) {
                AppDatabase.getDatabase(this@TransferFunds).categoryDao().getByUserId(userId)
            }

            withContext(Dispatchers.Main) {
                catContainer.removeAllViews()

                categories.forEach { category ->
                    val iv = ImageView(this@TransferFunds).apply {
                        val pad = (8 * resources.displayMetrics.density).toInt()
                        setPadding(pad, pad, pad, pad)
                        alpha = 0.5f
                        tag = category.categoryId

                        if (!category.iconLocalPath.isNullOrBlank()) {
                            setImageURI(category.iconLocalPath.toUri())
                        } else {
                            val resId = resources.getIdentifier(
                                category.iconName ?: "vec_filter", "drawable", packageName
                            )
                            setImageResource(resId)
                        }

                        setOnClickListener {
                            catContainer.children.forEach { it.alpha = 0.5f }
                            alpha = 1f
                            selectedCategoryId = category.categoryId
                        }
                    }
                    catContainer.addView(iv)
                }
            }
        }

        //  Confirm
        findViewById<Button>(R.id.btnConfirm).setOnClickListener {
            val amountStr = findViewById<EditText>(R.id.amountInput).text.toString()
            val amount = amountStr.toDoubleOrNull()

            if (amount == null || amount <= 0) {
                Toast.makeText(this, "Enter a valid amount", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch(Dispatchers.IO) {
                val db = AppDatabase.getDatabase(this@TransferFunds)
                val accountDao = db.accountDao()
                val transactionDao = db.transactionDao()
                val categoryDao = db.categoryDao()

                val userId = getSharedPreferences("app_piggy_prefs", MODE_PRIVATE)
                    .getString("logged_in_user_id", null) ?: return@launch

                val now = System.currentTimeMillis()

                if (isAccountToAccountMode) {
                    // --- Account → Account ---
                    val fromAccount = selectedFromAccountId?.let { accountDao.getById(it) }
                    val toAccount = selectedToAccountId?.let { accountDao.getById(it) }

                    if (fromAccount == null || toAccount == null || fromAccount.accountId == toAccount.accountId) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@TransferFunds, "Select two different accounts", Toast.LENGTH_SHORT).show()
                        }
                        return@launch
                    }

                    if (fromAccount.balance < amount) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@TransferFunds, "Insufficient funds", Toast.LENGTH_SHORT).show()
                        }
                        return@launch
                    }

                    accountDao.updateBalance(fromAccount.accountId, fromAccount.balance - amount)
                    accountDao.updateBalance(toAccount.accountId, toAccount.balance + amount)

                    transactionDao.insert(
                        TransactionEntity(
                            transactionId = UUID.randomUUID().toString(),
                            userId = userId,
                            accountId = fromAccount.accountId,
                            categoryId = null,
                            amount = -amount,
                            description = "Transfer to ${toAccount.accountName}",
                            date = now,
                            receiptImageUrl = null,
                            receiptLocalPath = null
                        )
                    )

                    transactionDao.insert(
                        TransactionEntity(
                            transactionId = UUID.randomUUID().toString(),
                            userId = userId,
                            accountId = toAccount.accountId,
                            categoryId = null,
                            amount = amount,
                            description = "Transfer from ${fromAccount.accountName}",
                            date = now,
                            receiptImageUrl = null,
                            receiptLocalPath = null
                        )
                    )
                } else {
                    // --- Account → Category ---
                    val fromAccount = selectedAccountForCategoryId?.let { accountDao.getById(it) }
                    val category = selectedCategoryId?.let { categoryDao.getById(it) }

                    if (fromAccount == null || category == null) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@TransferFunds, "Select account and category", Toast.LENGTH_SHORT).show()
                        }
                        return@launch
                    }

                    if (fromAccount.balance < amount) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@TransferFunds, "Insufficient funds", Toast.LENGTH_SHORT).show()
                        }
                        return@launch
                    }

                    // Subtract from account balance
                    accountDao.updateBalance(fromAccount.accountId, fromAccount.balance - amount)

                    // Add to category budget
                    categoryDao.addToBudget(category.categoryId, amount)

                    // Log allocation transaction
                    transactionDao.insert(
                        TransactionEntity(
                            transactionId = UUID.randomUUID().toString(),
                            userId = userId,
                            accountId = fromAccount.accountId,
                            categoryId = category.categoryId,
                            amount = -amount,
                            description = "Allocated to category: ${category.categoryName}",
                            date = now,
                            receiptImageUrl = null,
                            receiptLocalPath = null
                        )
                    )
                }

                withContext(Dispatchers.Main) {
                    Toast.makeText(this@TransferFunds, "Transfer completed!", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }





        //  Defaults
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
