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
import com.example.budgetpiggy.data.entities.CategoryEntity
import com.example.budgetpiggy.data.entities.TransactionEntity
import com.example.budgetpiggy.data.database.AppDatabase
import com.example.budgetpiggy.ui.notifications.Notification
import com.example.budgetpiggy.ui.reports.ReportsPage
import com.example.budgetpiggy.ui.transaction.TransactionHistory
import com.example.budgetpiggy.ui.transaction.TransferFunds
import com.example.budgetpiggy.ui.wallet.WalletPage
import com.example.budgetpiggy.utils.SessionManager
import com.example.budgetpiggy.utils.StreakTracker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL
import java.text.NumberFormat
import java.util.*

class HomePage : BaseActivity() {

    private lateinit var streakBadge: TextView
    private lateinit var accountBalanceList: LinearLayout
    private lateinit var budgetRemainingList: LinearLayout
    private lateinit var transactionList: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.home)

        // Bind views
        val greetingText = findViewById<TextView>(R.id.greetingText)
        streakBadge           = findViewById(R.id.streakBadge)
        accountBalanceList    = findViewById(R.id.accountBalanceList)
        budgetRemainingList   = findViewById(R.id.budgetRemainingList)
        transactionList       = findViewById(R.id.transactionList)

        // Greet user

        val userId = SessionManager.getUserId(this) ?: return
        userId?.let { id ->
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

        // Transaction card click
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

        // Navigation & actions
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

        // FAB behavior
        setupFabScrollBehavior()
        findViewById<ImageView>(R.id.fabPlus)?.setOnClickListener {
            startActivity(Intent(this, TransferFunds::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        setActiveNavIcon(findViewById(R.id.nav_home))
        loadHomeData()
    }

    private fun setupFabScrollBehavior() {
        val scrollView = findViewById<ScrollView>(R.id.scrollArea)
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

    private fun loadHomeData() {
        val prefs  = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val userId = prefs.getString("logged_in_user_id", null) ?: return

        lifecycleScope.launch {
            // Fetch everything from Room + rates
            val (data, categoryMap) = withContext(Dispatchers.IO) {
                val db     = AppDatabase.getDatabase(this@HomePage)
                val aDao   = db.accountDao()
                val cDao   = db.categoryDao()
                val tDao   = db.transactionDao()
                val uDao   = db.userDao()

                val balList = aDao.getBalancesForUser(userId)
                val catList = cDao.getByUserId(userId)
                val txList  = tDao.getByUserId(userId)
                    .sortedByDescending { it.date }
                    .take(5)
                val usr     = uDao.getById(userId)!!

                // Map categoryId -> categoryName
                val categoryMap = catList.associate { it.categoryId to it.categoryName }

                // Fetch rates
                val json = URL("https://api.exchangerate-api.com/v4/latest/ZAR")
                    .openConnection().run {
                        connect()
                        BufferedReader(InputStreamReader(inputStream)).use { it.readText() }
                    }
                val ratesObj = JSONObject(json).getJSONObject("rates")
                val rateMap = ratesObj.keys().asSequence()
                    .associateWith { ratesObj.getDouble(it) }

                (Quintuple(balList, catList, txList, usr, rateMap) to categoryMap)
            }

            // Unpack
            val (balances, categories, transactions, user, rateMap) = data
            val nf = NumberFormat.getCurrencyInstance().apply {
                currency = Currency.getInstance(user.currency)
            }

            // Render account balances
            accountBalanceList.removeAllViews()
            balances.forEach { acct ->
                val converted = acct.balance * (rateMap[user.currency] ?: 1.0)
                val row = layoutInflater.inflate(R.layout.item_balance_row, accountBalanceList, false)
                row.findViewById<TextView>(R.id.labelText).text = acct.accountName
                val pb = row.findViewById<ProgressBar>(R.id.progressBar)
                val convertedBalance = acct.balance * (rateMap[user.currency] ?: 1.0)
                pb.max = convertedBalance.toInt().coerceAtLeast(1)
                pb.progress = convertedBalance.toInt()

                row.findViewById<LinearLayout>(R.id.balanceRow).setOnClickListener {
                    val msg = "${acct.accountName}: ${nf.format(converted)}"
                    val t = Toast.makeText(this@HomePage, msg, Toast.LENGTH_SHORT)
                    t.show()
                    Handler(Looper.getMainLooper()).postDelayed({ t.cancel() }, 600)
                }
                accountBalanceList.addView(row)
            }

            // Render budget remaining
            budgetRemainingList.removeAllViews()
            categories.forEach { cat: CategoryEntity ->
                val converted = cat.budgetAmount * (rateMap[user.currency] ?: 1.0)
                val row = layoutInflater.inflate(R.layout.item_balance_row, budgetRemainingList, false)
                row.findViewById<TextView>(R.id.labelText).text = cat.categoryName
                val pb = row.findViewById<ProgressBar>(R.id.progressBar)
                val convertedBudget = cat.budgetAmount * (rateMap[user.currency] ?: 1.0)
                pb.max = convertedBudget.toInt().coerceAtLeast(1)
                pb.progress = convertedBudget.toInt()


                row.findViewById<LinearLayout>(R.id.balanceRow).setOnClickListener {
                    val msg = "${cat.categoryName}: ${nf.format(convertedBudget)} of ${nf.format(convertedBudget)}"
                    val t = Toast.makeText(this@HomePage, msg, Toast.LENGTH_SHORT)
                    t.show()
                    Handler(Looper.getMainLooper()).postDelayed({ t.cancel() }, 600)
                }
                budgetRemainingList.addView(row)
            }

            // Render recent transactions
            transactionList.removeAllViews()
            transactions.forEach { tx: TransactionEntity ->
                val row = layoutInflater.inflate(R.layout.item_transaction_row, transactionList, false)
                // Lookup category name
                val catName = tx.categoryId?.let { categoryMap[it] } ?: "—"
                row.findViewById<TextView>(R.id.categoryText).text = catName
                row.findViewById<TextView>(R.id.nameText).text     = tx.description ?: ""
                val amount = tx.amount * (rateMap[user.currency] ?: 1.0)
                row.findViewById<TextView>(R.id.amountText).text   = nf.format(amount)
                transactionList.addView(row)
            }
        }
    }

    override fun onBackPressed() {
        if (!isTaskRoot) {
            super.onBackPressed()
        } else {
            Toast.makeText(this,
                "Use the menu or logout button to exit",
                Toast.LENGTH_SHORT).show()
        }
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

    private data class Quintuple<A, B, C, D, E>(
        val first: A, val second: B, val third: C, val fourth: D, val fifth: E
    )
}
