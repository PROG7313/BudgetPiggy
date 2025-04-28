package com.example.budgetpiggy.ui.wallet

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
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
import com.example.budgetpiggy.ui.category.AddCategoryPage
import com.example.budgetpiggy.ui.reports.ReportsPage
import com.example.budgetpiggy.utils.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL
import java.text.NumberFormat
import java.util.Currency

class WalletPage : BaseActivity() {

    private lateinit var accountList: LinearLayout
    private lateinit var budgetList: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.wallet)

        // Edge‐to‐edge insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.walletPage)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        // Hide home-only icons
        findViewById<ImageView>(R.id.piggyIcon)?.visibility   = View.GONE
        findViewById<ImageView>(R.id.streakIcon)?.visibility  = View.GONE
        findViewById<TextView>(R.id.greetingText)?.visibility = View.GONE

        // Page title
        findViewById<TextView>(R.id.pageTitle).apply {
            visibility = View.VISIBLE
            text = getString(R.string.wallet)
        }

        // Bind lists
        accountList = findViewById(R.id.accountList)
        budgetList  = findViewById(R.id.budgetList)

        // Top-bar actions
        findViewById<ImageView>(R.id.bellIcon).setOnClickListener { v ->
            v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(25)
                .withEndAction {
                    v.animate().scaleX(1f).scaleY(1f).setDuration(25).start()
                    startActivity(Intent(this, Notification::class.java))
                }.start()
        }
        findViewById<ImageView>(R.id.backArrow).setOnClickListener { v ->
            v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(25)
                .withEndAction {
                    v.animate().scaleX(1f).scaleY(1f).setDuration(25).start()
                    onBackPressedDispatcher.onBackPressed()
                }.start()
        }

        // Bottom nav setup
        setupBottomNav()

        // FAB actions
        findViewById<ImageView>(R.id.addAccountPlus).setOnClickListener {
            startActivity(Intent(this, AddAccountPage::class.java))
        }
        findViewById<ImageView>(R.id.addCategoryPlus).setOnClickListener {
            startActivity(Intent(this, AddCategoryPage::class.java))
        }
        setupFabScrollBehavior()
    }

    override fun onResume() {
        super.onResume()
        setActiveNavIcon(findViewById(R.id.nav_wallet))
        loadAccountTypes()
        loadBudgetCategories()
    }

    private fun setupFabScrollBehavior() {
        val scrollView = findViewById<ScrollView>(R.id.walletScrollView)
        val fabWrapper = findViewById<View>(R.id.fabWrapper)
        var lastY = 0
        scrollView.viewTreeObserver.addOnScrollChangedListener {
            val y = scrollView.scrollY
            val show = y < lastY || !scrollView.canScrollVertically(-1) || !scrollView.canScrollVertically(1)
            if (show) {
                fabWrapper.visibility = View.VISIBLE
                fabWrapper.alpha = 1f
            } else {
                fabWrapper.animate().alpha(0f).setDuration(150)
                    .withEndAction { fabWrapper.visibility = View.GONE }
                    .start()
            }
            lastY = y
        }
    }

    private fun setupBottomNav() {
        findViewById<ImageView>(R.id.nav_home).setOnClickListener { v ->
            setActiveNavIcon(v as ImageView)
            startActivity(Intent(this, HomePage::class.java))
        }
        findViewById<ImageView>(R.id.nav_wallet).setOnClickListener { v ->
            setActiveNavIcon(v as ImageView)
        }
        findViewById<ImageView>(R.id.nav_reports).setOnClickListener { v ->
            setActiveNavIcon(v as ImageView)
            startActivity(Intent(this, ReportsPage::class.java))
        }
        findViewById<ImageView>(R.id.nav_profile).setOnClickListener { v ->
            setActiveNavIcon(v as ImageView)
            startActivity(Intent(this, AccountPage::class.java))
        }
    }

    private fun loadAccountTypes() {
        accountList.removeAllViews()

        val userId = getSharedPreferences("app_piggy_prefs", MODE_PRIVATE).getString("logged_in_user_id", "")!!

        lifecycleScope.launch {
            val (balances, user, rateMap) = withContext(Dispatchers.IO) {
                val db      = AppDatabase.getDatabase(this@WalletPage)
                val dao     = db.accountDao()
                val uDao    = db.userDao()
                val balList = dao.getBalancesForUser(userId)
                val usr     = uDao.getById(userId)!!

                // fetch ZAR→X rates
                val json = URL("https://api.exchangerate-api.com/v4/latest/ZAR")
                    .openConnection().run {
                        connect()
                        BufferedReader(InputStreamReader(inputStream)).use { it.readText() }
                    }
                val ratesObj = JSONObject(json).getJSONObject("rates")
                val map = ratesObj.keys().asSequence()
                    .associateWith { ratesObj.getDouble(it) }

                Triple(balList, usr, map)
            }

            val nf = NumberFormat.getCurrencyInstance().apply {
                currency = Currency.getInstance(user.currency)
            }

            balances.forEach { acct ->
                val converted = acct.balance * (rateMap[user.currency] ?: 1.0)
                val item = layoutInflater.inflate(
                    R.layout.item_wallet_account_row,
                    accountList, false
                )
                item.findViewById<TextView>(R.id.accountTypeText).text =
                    acct.accountName
                item.findViewById<TextView>(R.id.accountBalanceText).text =
                    getString(R.string.account_balance_text, nf.format(converted))
                item.findViewById<ImageView>(R.id.deleteIcon)
                    .setOnClickListener {
                        showDeleteDialog {
                            deleteAccount(userId, acct.accountName)
                        }
                    }
                accountList.addView(item)
            }
        }
    }

    private fun deleteAccount(userId: String, accountName: String) {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                val dao = AppDatabase.getDatabase(this@WalletPage).accountDao()
                dao.getByUserId(userId)
                    .firstOrNull { it.accountName == accountName }
                    ?.let { dao.delete(it) }
            }
            loadAccountTypes()
        }
    }

    private fun loadBudgetCategories() {
        budgetList.removeAllViews()


        val userId = SessionManager.getUserId(this) ?: return

        lifecycleScope.launch {
            val (cats, user, rateMap) = withContext(Dispatchers.IO) {
                val db   = AppDatabase.getDatabase(this@WalletPage)
                val cDao = db.categoryDao()
                val uDao = db.userDao()
                val list = cDao.getByUserId(userId)
                val usr  = uDao.getById(userId)!!

                // fetch ZAR→X rates
                val json = URL("https://api.exchangerate-api.com/v4/latest/ZAR")
                    .openConnection().run {
                        connect()
                        BufferedReader(InputStreamReader(inputStream)).use { it.readText() }
                    }
                val ratesObj = JSONObject(json).getJSONObject("rates")
                val map = ratesObj.keys().asSequence()
                    .associateWith { ratesObj.getDouble(it) }

                Triple(list, usr, map)
            }

            val nf = NumberFormat.getCurrencyInstance().apply {
                currency = Currency.getInstance(user.currency)
            }

            cats.forEach { cat ->
                val converted = cat.budgetAmount * (rateMap[user.currency] ?: 1.0)
                val item = layoutInflater.inflate(
                    R.layout.item_wallet_budget_row,
                    budgetList, false
                )
                item.findViewById<TextView>(R.id.categoryName).text   = cat.categoryName
                item.findViewById<TextView>(R.id.budgetAmount).text   =
                    getString(R.string.budget_amount_text, nf.format(converted))
                item.findViewById<TextView>(R.id.budgetSource).text   =
                    getString(R.string.budget_source_text, cat.linkedAccountType)

                val iconImage = item.findViewById<ImageView>(R.id.categoryIcon)

                if (cat.iconLocalPath != null) {
                    val fileUri = Uri.parse(cat.iconLocalPath)
                    iconImage.setImageURI(fileUri)
                } else {
                    val resName = cat.iconName ?: "vec_filter"
                    val resId = resources.getIdentifier(resName, "drawable", packageName)
                    iconImage.setImageResource(resId)
                }

                item.findViewById<ImageView>(R.id.deleteIcon)
                    .setOnClickListener {
                        showDeleteDialog {
                            deleteCategory(userId, cat.categoryName)
                        }
                    }

                budgetList.addView(item)
            }
        }
    }

    private fun deleteCategory(userId: String, categoryName: String) {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                val dao = AppDatabase.getDatabase(this@WalletPage).categoryDao()
                dao.getByUserId(userId)
                    .firstOrNull { it.categoryName == categoryName }
                    ?.let { dao.delete(it) }
            }
            loadBudgetCategories()
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

    override fun setActiveNavIcon(activeIcon: ImageView) {
        val navIcons = listOf(
            R.id.nav_home to R.drawable.vec_home_inactive,
            R.id.nav_wallet to R.drawable.vec_wallet_inactive,
            R.id.nav_reports to R.drawable.vec_reports_inactive,
            R.id.nav_profile to R.drawable.vec_profile_inactive
        )
        navIcons.forEach { (id, drawable) ->
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
