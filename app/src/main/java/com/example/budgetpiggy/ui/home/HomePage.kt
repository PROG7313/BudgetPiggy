package com.example.budgetpiggy.ui.home

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.core.content.edit
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.budgetpiggy.R
import com.example.budgetpiggy.data.database.AppDatabase
import com.example.budgetpiggy.data.entities.TransactionEntity
import com.example.budgetpiggy.ui.core.BaseActivity
import com.example.budgetpiggy.ui.notifications.Notification
import com.example.budgetpiggy.ui.reports.ReportsPage
import com.example.budgetpiggy.ui.settings.AccountPage
import com.example.budgetpiggy.ui.transaction.TransactionActivity
import com.example.budgetpiggy.ui.transaction.TransactionHistory
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
    private lateinit var prefs: SharedPreferences

    private lateinit var tvCurrentExpense: TextView
    private lateinit var expenseGoalsProgress: ProgressBar
    private lateinit var tvMinExpenseGoal: TextView
    private lateinit var tvMaxExpenseGoal: TextView
    private lateinit var btnSetExpenseGoals: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.home)

        // Bind views
        val greetingText        = findViewById<TextView>(R.id.greetingText)
        streakBadge             = findViewById(R.id.streakBadge)
        accountBalanceList      = findViewById(R.id.accountBalanceList)
        budgetRemainingList     = findViewById(R.id.budgetRemainingList)
        transactionList         = findViewById(R.id.transactionList)

        tvCurrentExpense        = findViewById(R.id.tvCurrentExpense)
        expenseGoalsProgress    = findViewById(R.id.expenseGoalsProgress)
        tvMinExpenseGoal        = findViewById(R.id.tvMinExpenseGoal)
        tvMaxExpenseGoal        = findViewById(R.id.tvMaxExpenseGoal)
        btnSetExpenseGoals      = findViewById(R.id.btnSetExpenseGoals)


        val sessionPrefs = getSharedPreferences("app_piggy_prefs", Context.MODE_PRIVATE)
        val userId = sessionPrefs.getString("logged_in_user_id", null)
            ?: throw IllegalStateException("No user logged in!")


        prefs = getSharedPreferences("app_piggy_prefs_$userId", Context.MODE_PRIVATE)


        // Initialize goal labels/bar
        updateGoalViews()

        // Set-goals button
        btnSetExpenseGoals.setOnClickListener {
            showSetGoalsDialog()
        }

        // Greet user
        SessionManager.getUserId(this)?.let { id ->
            lifecycleScope.launch(Dispatchers.IO) {
                val user = AppDatabase.getDatabase(this@HomePage)
                    .userDao().getById(id)
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

        // Transaction history card
        findViewById<View>(R.id.transactionCard).setOnClickListener { v ->
            v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(25)
                .withEndAction {
                    v.animate().scaleX(1f).scaleY(1f).setDuration(25).start()
                    startActivity(Intent(this, TransactionHistory::class.java))
                }.start()
        }

        // Window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.homePage)) { v, insets ->
            val sb = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(sb.left, sb.top, sb.right, sb.bottom)
            insets
        }

        // Bottom nav
        findViewById<ImageView>(R.id.nav_home).setOnClickListener { /* no-op */ }
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

        // FAB
        setupFabScrollBehavior(
            findViewById(R.id.scrollArea),
            findViewById(R.id.fabWrapper)
        )
        findViewById<ImageView>(R.id.fabPlus)?.setOnClickListener {
            startActivity(Intent(this, TransactionActivity::class.java))
        }

        // Bell icon
        findViewById<ImageView>(R.id.bellIcon).setOnClickListener {
            startActivity(Intent(this, Notification::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        setActiveNavIcon(findViewById(R.id.nav_home))
        loadHomeData()
    }

    // ─── Expense-Goal Helpers ─────────────────────────────────────────

    private fun updateGoalViews() {
        val minGoal = prefs.getInt("min_expense_goal", 0)
        val maxGoal = prefs.getInt("max_expense_goal", 0)

        tvMinExpenseGoal.text = "Min: R${minGoal}"
        tvMaxExpenseGoal.text = "Max: R${maxGoal}"

        // reset bar until loadHomeData runs
        expenseGoalsProgress.max               = maxGoal.coerceAtLeast(1)
        expenseGoalsProgress.secondaryProgress = 0
        expenseGoalsProgress.progress          = 0
    }

    private fun showSetGoalsDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_set_goals, null)
        val etMin = dialogView.findViewById<EditText>(R.id.etMinGoal)
        val etMax = dialogView.findViewById<EditText>(R.id.etMaxGoal)

        etMin.setText(prefs.getInt("min_expense_goal", 0).toString())
        etMax.setText(prefs.getInt("max_expense_goal", 0).toString())

        AlertDialog.Builder(this)
            .setTitle("Set Expense Goals")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val newMin = etMin.text.toString().toIntOrNull() ?: 0
                val newMax = etMax.text.toString().toIntOrNull() ?: 0

                prefs.edit {
                    putInt("min_expense_goal", newMin)
                    putInt("max_expense_goal", newMax)
                }

                // redraw labels AND bar/current
                updateGoalViews()
                loadHomeData()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // ─── Main Data Loading ────────────────────────────────────────────

    private fun loadHomeData() {
        val userId = SessionManager.getUserId(this) ?: return

        lifecycleScope.launch {
            // ─── 1) Load balances, categories, ALL transactions, user & rates ───────────────
            val (balances, categories, allTxs, user, rateMap) = withContext(Dispatchers.IO) {
                val db      = AppDatabase.getDatabase(this@HomePage)
                val balList = db.accountDao().getBalancesForUser(userId)
                val catList = db.categoryDao().getByUserId(userId)
                val txList  = db.transactionDao()
                    .getByUserId(userId)
                    .sortedByDescending { it.date }    // load ALL, sorted
                val usr     = db.userDao().getById(userId)!!
                val rates   = CurrencyManager.getRateMap(this@HomePage, "ZAR")

                Quintuple(balList, catList, txList, usr, rates)
            }

            // ─── 2) Build helper maps ───────────────────────────────────────────────────────
            val categoryMap = categories.associate { it.categoryId to it.categoryName }
            val accountMap: Map<String, String> = withContext(Dispatchers.IO) {
                AppDatabase.getDatabase(this@HomePage)
                    .accountDao()
                    .getByUserId(userId)
                    .associate { it.accountId to it.accountName }
            }

            // ─── 3) Expense‐goal UI ─────────────────────────────────────────────────────────
            val expenseTotalRaw = allTxs
                .filter { it.amount < 0 }
                .sumOf   { -it.amount }               // positive expense sum
            val rate            = rateMap[user.currency] ?: 1.0
            val expenseTotal    = (expenseTotalRaw * rate).roundToInt()
            val minGoal         = prefs.getInt("min_expense_goal", 0)
            val maxGoal         = prefs.getInt("max_expense_goal", 0)
            val nf              = NumberFormat.getCurrencyInstance().apply {
                currency = Currency.getInstance(user.currency)
            }

            tvCurrentExpense.text = "Current: ${nf.format(expenseTotalRaw * rate)}"
            tvMinExpenseGoal.text  = "Min: R${minGoal}"
            tvMaxExpenseGoal.text  = "Max: R${maxGoal}"

            expenseGoalsProgress.max               = maxGoal.coerceAtLeast(1)
            expenseGoalsProgress.secondaryProgress = minGoal.coerceIn(0, maxGoal)
            expenseGoalsProgress.progress          = expenseTotal.coerceIn(0, maxGoal)

            // ─── 4) Account balances UI ───────────────────────────────────────────────────
            accountBalanceList.removeAllViews()
            balances.forEach { acct ->
                val remaining = acct.balance * rate
                val total     = acct.initialBalance * rate

                val row = layoutInflater.inflate(
                    R.layout.item_balance_row,
                    accountBalanceList,
                    false
                )
                row.findViewById<TextView>(R.id.labelText).text = acct.accountName

                val pb = row.findViewById<ProgressBar>(R.id.progressBar)
                pb.max      = total.coerceAtLeast(1.0).roundToInt()
                pb.progress = remaining.coerceIn(0.0, total).roundToInt()

                row.findViewById<TextView>(R.id.remainingText).text =
                    "Remaining: ${nf.format(remaining)}"
                row.findViewById<TextView>(R.id.totalText).text =
                    "Total: ${nf.format(total)}"

                row.findViewById<LinearLayout>(R.id.balanceRow)
                    .setOnClickListener {
                        val msg = "${acct.accountName}: ${nf.format(remaining)} of ${nf.format(total)}"
                        val t   = Toast.makeText(this@HomePage, msg, Toast.LENGTH_SHORT)
                        t.show()
                        Handler(Looper.getMainLooper()).postDelayed({ t.cancel() }, 600)
                    }

                accountBalanceList.addView(row)
            }

            // ─── 5) Budget remaining UI ───────────────────────────────────────────────────
            budgetRemainingList.removeAllViews()
            categories.forEach { cat ->
                val totalBudget      = cat.budgetAmount
                val spent            = allTxs.filter { it.categoryId == cat.categoryId }
                    .sumOf { it.amount }
                val absoluteSpent    = -spent.coerceAtMost(0.0)
                val remainingBudget  = (totalBudget - absoluteSpent).coerceAtLeast(0.0)

                val convertedTotal     = (totalBudget * rate).roundToInt()
                val convertedSpent     = (absoluteSpent * rate).roundToInt()
                val convertedRemaining = (remainingBudget * rate).roundToInt()

                val row = layoutInflater.inflate(
                    R.layout.item_balance_row,
                    budgetRemainingList,
                    false
                )
                row.findViewById<TextView>(R.id.labelText).text = cat.categoryName

                val pb2 = row.findViewById<ProgressBar>(R.id.progressBar)
                pb2.max      = convertedTotal.coerceAtLeast(1)
                pb2.progress = convertedSpent.coerceIn(0, convertedTotal)

                row.findViewById<TextView>(R.id.remainingText).text =
                    "Remaining: ${nf.format(remainingBudget * rate)}"
                row.findViewById<TextView>(R.id.totalText).text =
                    "Total:     ${nf.format(totalBudget * rate)}"

                row.findViewById<LinearLayout>(R.id.balanceRow)
                    .setOnClickListener {
                        val msg = "${cat.categoryName}: " +
                                "${nf.format(remainingBudget * rate)} of " +
                                "${nf.format(totalBudget * rate)}"
                        Toast.makeText(this@HomePage, msg, Toast.LENGTH_SHORT).show()
                    }

                budgetRemainingList.addView(row)
            }

            // ─── 6) Recent Transactions + Transfers ────────────────────────────────────────
            val transfers = withContext(Dispatchers.IO) {
                AppDatabase.getDatabase(this@HomePage)
                    .transferDao()
                    .getByUserId(userId)
            }
            val transferTxs = transfers.flatMap { tr ->
                listOf(
                    // debit
                    TransactionEntity(
                        transactionId    = UUID.randomUUID().toString(),
                        userId           = tr.userId,
                        accountId        = tr.fromAccountId!!,
                        categoryId       = null,
                        amount           = -tr.amount,
                        date             = tr.date,
                        description      = "Transfer to ${tr.toAccountId}",
                        receiptLocalPath = null
                    ),
                    // credit
                    TransactionEntity(
                        transactionId    = UUID.randomUUID().toString(),
                        userId           = tr.userId,
                        accountId        = tr.toAccountId!!,
                        categoryId       = null,
                        amount           = tr.amount,
                        date             = tr.date,
                        description      = "Transfer from ${tr.fromAccountId}",
                        receiptLocalPath = null
                    )
                )
            }

            val recentCombined = (allTxs + transferTxs)
                .sortedByDescending { it.date }
                .take(5)

            transactionList.removeAllViews()
            recentCombined.forEach { tx ->
                val row = layoutInflater.inflate(
                    R.layout.item_transaction_row,
                    transactionList,
                    false
                )

                // show friendly name and keep the ID
                val acctName = accountMap[tx.accountId] ?: tx.accountId
                row.findViewById<TextView>(R.id.nameText)
                    .text = "To Account: $acctName "

                // category or “Transfer”
                val catLabel = categoryMap[tx.categoryId] ?: "Transfer"
                row.findViewById<TextView>(R.id.categoryText).text = catLabel

                val dispAmt = tx.amount * (rateMap[user.currency] ?: 1.0)
                row.findViewById<TextView>(R.id.amountText).text = nf.format(dispAmt)

                transactionList.addView(row)
            }
        }
    }



    override fun onBackPressed() {
        if (!isTaskRoot) {
            super.onBackPressed()
        } else {
            Toast.makeText(this,
                "Use the logout button to exit",
                Toast.LENGTH_SHORT).show()
        }
    }

    override fun setActiveNavIcon(activeIcon: ImageView) {
        val navIcons = listOf(
            R.id.nav_home    to R.drawable.vec_home_inactive,
            R.id.nav_wallet  to R.drawable.vec_wallet_inactive,
            R.id.nav_reports to R.drawable.vec_reports_inactive,
            R.id.nav_profile to R.drawable.vec_profile_inactive
        )
        navIcons.forEach { (id, drawable) ->
            findViewById<ImageView>(id).setImageResource(drawable)
        }
        when (activeIcon.id) {
            R.id.nav_home    -> activeIcon.setImageResource(R.drawable.vec_home_active)
            R.id.nav_wallet  -> activeIcon.setImageResource(R.drawable.vec_wallet_active)
            R.id.nav_reports -> activeIcon.setImageResource(R.drawable.vec_reports_active)
            R.id.nav_profile -> activeIcon.setImageResource(R.drawable.vec_profile_active)
        }
    }

    private data class Quintuple<A, B, C, D, E>(
        val first: A, val second: B, val third: C, val fourth: D, val fifth: E
    )
}
