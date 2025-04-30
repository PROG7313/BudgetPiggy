package com.example.budgetpiggy.ui.home

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.budgetpiggy.ui.settings.AccountPage
import com.example.budgetpiggy.ui.core.BaseActivity
import com.example.budgetpiggy.R

import com.example.budgetpiggy.data.database.AppDatabase
import com.example.budgetpiggy.ui.notifications.Notification
import com.example.budgetpiggy.ui.reports.ReportsPage
import com.example.budgetpiggy.ui.transaction.TransactionActivity
import com.example.budgetpiggy.ui.transaction.TransactionHistory
import com.example.budgetpiggy.ui.transaction.TransferFunds
import com.example.budgetpiggy.ui.wallet.WalletPage
import com.example.budgetpiggy.utils.CurrencyManager
import com.example.budgetpiggy.utils.SessionManager
import com.example.budgetpiggy.utils.StreakTracker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.NumberFormat
import java.util.*
import kotlin.math.roundToInt

class HomePage : BaseActivity() {

    private lateinit var streakBadge: TextView
    private lateinit var accountBalanceList: LinearLayout
    private lateinit var budgetRemainingList: LinearLayout
    private lateinit var transactionList: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.home)

        // Bind views (Android, 2025)
        val greetingText = findViewById<TextView>(R.id.greetingText)
        streakBadge           = findViewById(R.id.streakBadge)
        accountBalanceList    = findViewById(R.id.accountBalanceList)
        budgetRemainingList   = findViewById(R.id.budgetRemainingList)
        transactionList       = findViewById(R.id.transactionList)

        // Greet user

        val userId = SessionManager.getUserId(this) ?: return
        userId.let { id ->
            lifecycleScope.launch(Dispatchers.IO) {
                val user = AppDatabase.getDatabase(this@HomePage).userDao().getById(id)
                withContext(Dispatchers.Main) {
                    greetingText.text = "Hi, ${user?.firstName}"
                }
            }
        }

        // Streak badge
        val streak = StreakTracker.updateStreak(this)
        if (streak > 0) {
            streakBadge.visibility = View.VISIBLE
            streakBadge.text = streak.toString()
        } else {
            streakBadge.visibility = View.GONE
        }

        // Transaction card click (Ambitions, 2025)
        findViewById<View>(R.id.transactionCard).setOnClickListener { v ->
            v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(2)
                .withEndAction {
                    v.animate().scaleX(1f).scaleY(1f).setDuration(2).start()
                    startActivity(Intent(this, TransactionHistory::class.java))
                }.start()
        }

        // Insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.homePage)) { v, insets ->
            val sb = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(sb.left, sb.top, sb.right, sb.bottom)
            insets
        }

        // Navigation & actions (Ambitions, 2025)
        findViewById<ImageView>(R.id.backArrow).setOnClickListener { v ->
            v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(25)
                .withEndAction {
                    v.animate().scaleX(1f).scaleY(1f).setDuration(25).start()
                    onBackPressed()
                }.start()
        }
        findViewById<ImageView>(R.id.bellIcon).setOnClickListener {
            startActivity(Intent(this, Notification::class.java))
        }
        findViewById<ImageView>(R.id.nav_wallet).setOnClickListener { v ->
            setActiveNavIcon(v as ImageView)
            v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(25)
                .withEndAction {
                    v.animate().scaleX(1f).scaleY(1f).setDuration(25).start()
                    startActivity(Intent(this, WalletPage::class.java))
                }.start()
        }
        findViewById<ImageView>(R.id.nav_reports).setOnClickListener { v ->
            setActiveNavIcon(v as ImageView)
            startActivity(Intent(this, ReportsPage::class.java))
        }
        findViewById<ImageView>(R.id.nav_profile).setOnClickListener { v ->
            setActiveNavIcon(v as ImageView)
            startActivity(Intent(this, AccountPage::class.java))
        }
        val scrollView = findViewById<ScrollView>(R.id.scrollArea)
        val fabWrapper = findViewById<View>(R.id.fabWrapper)
        // FAB behavior
        setupFabScrollBehavior(scrollView, fabWrapper)

        findViewById<ImageView>(R.id.fabPlus)?.setOnClickListener {
            startActivity(Intent(this, TransactionActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        setActiveNavIcon(findViewById(R.id.nav_home))
        loadHomeData()
    }

    private fun loadHomeData() {
        val userId = SessionManager.getUserId(this) ?: return

        lifecycleScope.launch {
            // Fetch from Room + rates
            val (data, categoryMap) = withContext(Dispatchers.IO) {
                val db    = AppDatabase.getDatabase(this@HomePage)
                val aDao  = db.accountDao()
                val cDao  = db.categoryDao()
                val tDao  = db.transactionDao()
                val uDao  = db.userDao()

                // now includes initialBalance
                val balList = aDao.getBalancesForUser(userId)
                val catList = cDao.getByUserId(userId)
                val txList  = tDao.getByUserId(userId)
                    .sortedByDescending { it.date }
                    .take(5)
                val usr     = uDao.getById(userId)!!

                val categoryMap = catList.associate { it.categoryId to it.categoryName }
                val rateMap     = CurrencyManager.getRateMap(this@HomePage, "ZAR")

                (Quintuple(balList, catList, txList, usr, rateMap) to categoryMap)
            }

            // Unpack & formatter
            val (balances, categories, transactions, user, rateMap) = data
            val nf = NumberFormat.getCurrencyInstance().apply {
                currency = Currency.getInstance(user.currency)
            }

            // Render account balances with remaining vs total
            accountBalanceList.removeAllViews()
            balances.forEach { acct ->
                // currency-converted values
                val rate     = rateMap[user.currency] ?: 1.0
                val remaining = acct.balance * rate
                val total     = acct.initialBalance * rate

                val row = layoutInflater
                    .inflate(R.layout.item_balance_row, accountBalanceList, false)

                row.findViewById<TextView>(R.id.labelText).text = acct.accountName

                val pb = row.findViewById<ProgressBar>(R.id.progressBar)
                val maxVal = total.coerceAtLeast(1.0).roundToInt()
                val prog   = remaining.coerceIn(0.0, total).roundToInt()
                pb.max      = maxVal
                pb.progress = prog

                row.findViewById<TextView>(R.id.remainingText)
                    .text = "Remaining: ${nf.format(remaining)}"
                row.findViewById<TextView>(R.id.totalText)
                    .text = "Total: ${nf.format(total)}"

                row.findViewById<LinearLayout>(R.id.balanceRow)
                    .setOnClickListener {
                        val msg = "${acct.accountName}: ${nf.format(remaining)} of ${nf.format(total)}"
                        val t = Toast.makeText(this@HomePage, msg, Toast.LENGTH_SHORT)
                        t.show()
                        Handler(Looper.getMainLooper()).postDelayed({ t.cancel() }, 600)
                    }

                accountBalanceList.addView(row)
            }

            // Render budget remaining (unchanged) (Ambitions, 2025)
            budgetRemainingList.removeAllViews()
            categories.forEach { cat ->
                val converted = cat.budgetAmount * (rateMap[user.currency] ?: 1.0)
                val row = layoutInflater.inflate(R.layout.item_balance_row_budget, budgetRemainingList, false)
                row.findViewById<TextView>(R.id.labelText).text = cat.categoryName
                val pb = row.findViewById<ProgressBar>(R.id.progressBar)
                val convertedBudget = converted.coerceAtLeast(1.0)
                pb.max = convertedBudget.toInt()
                pb.progress = convertedBudget.toInt()

                row.findViewById<LinearLayout>(R.id.balanceRow).setOnClickListener {
                    val msg = "${cat.categoryName}: ${nf.format(convertedBudget)} of ${nf.format(convertedBudget)}"
                    val t = Toast.makeText(this@HomePage, msg, Toast.LENGTH_SHORT)
                    t.show()
                    Handler(Looper.getMainLooper()).postDelayed({ t.cancel() }, 600)
                }
                budgetRemainingList.addView(row)
            }

            // Render recent transactions (unchanged)
            transactionList.removeAllViews()
            transactions.forEach { tx ->
                val row = layoutInflater
                    .inflate(R.layout.item_transaction_row, transactionList, false)
                val catName = tx.categoryId?.let { categoryMap[it] } ?: "—"
                row.findViewById<TextView>(R.id.categoryText).text = catName
                row.findViewById<TextView>(R.id.nameText).text     = tx.description ?: ""
                val amt = tx.amount * (rateMap[user.currency] ?: 1.0)
                row.findViewById<TextView>(R.id.amountText).text   = nf.format(amt)
                transactionList.addView(row)
            }
        }
    }

    // Back behaviour
    override fun onBackPressed() {
        if (!isTaskRoot) {
            super.onBackPressed()
        } else {
            Toast.makeText(this,
                "Use the logout button to exit",
                Toast.LENGTH_SHORT).show()
        }
    }

    // Updates the navigation icons to show the active one and reset the previous one
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

    // Data class that holds layout (Android, 2025).
    private data class Quintuple<A, B, C, D, E>(
        val first: A,
        val second: B,
        val third: C,
        val fourth: D,
        val fifth: E
    )
}
