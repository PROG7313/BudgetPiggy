package com.example.budgetpiggy.ui.reports

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
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
import com.example.budgetpiggy.ui.wallet.WalletPage
import com.example.budgetpiggy.data.database.AppDatabase
import com.example.budgetpiggy.utils.SessionManager
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.utils.ColorTemplate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.absoluteValue

class ReportsPage : BaseActivity() {
    private lateinit var dateRangeButton: Button
    private lateinit var totalBalanceChart: PieChart
    private lateinit var earningsChart: PieChart
    private lateinit var spendingChart: PieChart
    private lateinit var categoryChart: PieChart
    private lateinit var balanceTrendChart: LineChart

    private var startDate: Long = 0L
    private var endDate: Long = 0L
    private var allCategoryNames: List<String> = emptyList()
    private var lastSelectedCategories = mutableListOf<String>()

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.reports)

        findViewById<ImageView>(R.id.piggyIcon).visibility = View.GONE
        findViewById<TextView>(R.id.greetingText).visibility = View.GONE
        findViewById<ImageView>(R.id.streakIcon).visibility = View.GONE

        findViewById<TextView>(R.id.pageTitle).apply {
            visibility = View.VISIBLE
            text = getString(R.string.reports)
        }

        listOf(
            R.id.nav_home to HomePage::class.java,
            R.id.nav_wallet to WalletPage::class.java,
            R.id.nav_reports to ReportsPage::class.java,
            R.id.nav_profile to AccountPage::class.java
        ).forEach { (id, cls) ->
            findViewById<ImageView>(id).setOnClickListener {
                setActiveNavIcon(findViewById(id))
                startActivity(Intent(this, cls))
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.reportsPage)) { v, insets ->
            val b = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(b.left, b.top, b.right, b.bottom)
            insets
        }

        dateRangeButton   = findViewById(R.id.dateRangeButton)
        totalBalanceChart = findViewById(R.id.totalBalanceChart)
        earningsChart     = findViewById(R.id.earningsChart)
        spendingChart     = findViewById(R.id.spendingChart)
        categoryChart     = findViewById(R.id.categoryChart)
        balanceTrendChart = findViewById(R.id.balanceTrendChart)

        findViewById<ImageView>(R.id.backArrow).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        findViewById<ImageView>(R.id.bellIcon).setOnClickListener {
            startActivity(Intent(this, Notification::class.java))
        }
        findViewById<ImageView>(R.id.filterIcon).setOnClickListener {
            showCategoryFilterDialog()
        }

        val now = Calendar.getInstance()
        endDate = now.timeInMillis
        now.add(Calendar.DAY_OF_YEAR, -30)
        startDate = now.timeInMillis
        val fmt = SimpleDateFormat("MMM d", Locale.getDefault())
        dateRangeButton.text = "${fmt.format(startDate)} – ${fmt.format(endDate)}"
        dateRangeButton.setOnClickListener { pickDateRange() }

        loadAllCharts()
    }

    override fun onResume() {
        super.onResume()
        setActiveNavIcon(findViewById(R.id.nav_reports))
    }

    private fun pickDateRange() {
        val cal = Calendar.getInstance()
        DatePickerDialog(this, { _, y, m, d ->
            val sCal = Calendar.getInstance().apply {
                set(y, m, d, 0, 0, 0); set(Calendar.MILLISECOND, 0)
            }
            DatePickerDialog(this, { _, y2, m2, d2 ->
                val eCal = Calendar.getInstance().apply {
                    set(y2, m2, d2, 23, 59, 59); set(Calendar.MILLISECOND, 999)
                }
                startDate = sCal.timeInMillis
                endDate = eCal.timeInMillis
                val fmt = SimpleDateFormat("MMM d", Locale.getDefault())
                dateRangeButton.text = "${fmt.format(startDate)} – ${fmt.format(endDate)}"
                loadAllCharts()
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun loadAllCharts() {
        val userId = SessionManager.getUserId(this) ?: return

        lifecycleScope.launch {
            val (balances, categories, txList, user, rateMap) = withContext(Dispatchers.IO) {
                val db = AppDatabase.getDatabase(this@ReportsPage)
                val aDao = db.accountDao()
                val cDao = db.categoryDao()
                val tDao = db.transactionDao()

                val balList = aDao.getBalancesForUser(userId)
                val catList = cDao.getByUserId(userId)
                val allTx = tDao.getByUserId(userId)
                val filteredTx = allTx.filter { it.date in startDate..endDate }

                allCategoryNames = catList.map { it.categoryName }
                lastSelectedCategories = allCategoryNames.toMutableList()

                val json = URL("https://api.exchangerate-api.com/v4/latest/ZAR")
                    .openConnection().run {
                        connect()
                        BufferedReader(InputStreamReader(inputStream)).use { it.readText() }
                    }
                val ratesObj = JSONObject(json).getJSONObject("rates")
                val rateMap = ratesObj.keys().asSequence()
                    .associateWith { ratesObj.getDouble(it) }

                val user = db.userDao().getById(userId)!!

                Quadruple(balList, catList, filteredTx, user, rateMap)
            }

            val userCurrency = Currency.getInstance(user.currency)
            val zarToUserRate = rateMap[user.currency] ?: 1.0
            val nf = NumberFormat.getCurrencyInstance().apply { currency = userCurrency }

            setupPie(totalBalanceChart, balances.map {
                PieEntry((it.balance * zarToUserRate).toFloat(), it.accountName)
            })

            val earningMap = txList.filter { it.amount > 0 }
                .groupBy { it.accountId }
                .mapNotNull { (acc, txs) ->
                    val name = balances.find { it.accountName == acc }?.accountName
                    name?.let { PieEntry(txs.sumOf { it.amount * zarToUserRate }.toFloat(), it) }
                }
            setupPie(earningsChart, earningMap)

            val spendingMap = txList.filter { it.amount < 0 }
                .groupBy { it.categoryId }
                .mapNotNull { (catId, txs) ->
                    val name = categories.find { it.categoryId == catId }?.categoryName
                    name?.let { PieEntry(txs.sumOf { it.amount.absoluteValue * zarToUserRate }.toFloat(), it) }
                }
            setupPie(spendingChart, spendingMap)

            val budgetEntries = categories.map {
                PieEntry((it.budgetAmount * zarToUserRate).toFloat(), it.categoryName)
            }
            setupPie(categoryChart, budgetEntries)
            populateCategoryList(budgetEntries, nf)

            val daySums = txList.groupBy {
                val c = Calendar.getInstance().apply { timeInMillis = it.date }
                c.set(Calendar.HOUR_OF_DAY, 0); c.set(Calendar.MINUTE, 0)
                c.set(Calendar.SECOND, 0); c.set(Calendar.MILLISECOND, 0)
                c.timeInMillis
            }.mapValues { it.value.sumOf { tx -> tx.amount * zarToUserRate }.toFloat() }

            val days = mutableListOf<Long>()
            val cal2 = Calendar.getInstance().apply { timeInMillis = startDate }
            while (cal2.timeInMillis <= endDate) {
                days.add(cal2.timeInMillis)
                cal2.add(Calendar.DATE, 1)
            }

            var cum = 0f
            val lineEntries = days.mapIndexed { idx, day ->
                cum += daySums[day] ?: 0f
                Entry(idx.toFloat(), cum)
            }
            setupLine(balanceTrendChart, lineEntries)
        }
    }

    private fun setupPie(chart: PieChart, entries: List<PieEntry>) {
        val ds = PieDataSet(entries, "").apply {
            colors = ColorTemplate.MATERIAL_COLORS.toList()
            valueTextSize = 14f
        }
        chart.apply {
            data = PieData(ds)
            description.isEnabled = false
            legend.isEnabled = false
            invalidate()
        }
    }

    private fun setupLine(chart: LineChart, entries: List<Entry>) {
        val ds = LineDataSet(entries, "").apply {
            setDrawValues(false)
            lineWidth = 2f
            circleRadius = 4f
        }
        chart.apply {
            data = LineData(ds)
            description.isEnabled = false
            animateX(500)
            invalidate()
        }
    }

    private fun populateCategoryList(entries: List<PieEntry>, nf: NumberFormat) {
        val container = findViewById<LinearLayout>(R.id.categoryList)
        container.removeAllViews()
        entries.forEach { e ->
            val view = layoutInflater.inflate(R.layout.item_category_row, container, false)
            val icon = view.findViewById<ImageView>(R.id.categoryIcon)
            val label = view.findViewById<TextView>(R.id.categoryLabel)
            when (e.label.lowercase(Locale.getDefault())) {
                "vehicle" -> icon.setImageResource(R.drawable.vec_car)
                "home" -> icon.setImageResource(R.drawable.vec_home)
                "groceries", "food" -> icon.setImageResource(R.drawable.vec_food)
                else -> icon.setImageResource(R.drawable.vec_filter)
            }
            label.text = "${e.label}: ${nf.format(e.value.toDouble())}"
            container.addView(view)
        }
    }

    private fun showCategoryFilterDialog() {
        val all = allCategoryNames
        val sel = all.map { lastSelectedCategories.contains(it) }.toBooleanArray()

        AlertDialog.Builder(this)
            .setTitle("Filter Categories")
            .setMultiChoiceItems(all.toTypedArray(), sel) { _, idx, checked -> sel[idx] = checked }
            .setPositiveButton("Apply") { dialog, _ ->
                lastSelectedCategories = all.filterIndexed { i, _ -> sel[i] }.toMutableList()
                loadAllCharts()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun setActiveNavIcon(activeIcon: ImageView) {
        listOf(
            R.id.nav_home to R.drawable.vec_home_inactive,
            R.id.nav_wallet to R.drawable.vec_wallet_inactive,
            R.id.nav_reports to R.drawable.vec_reports_inactive,
            R.id.nav_profile to R.drawable.vec_profile_inactive
        ).forEach { (id, dr) ->
            findViewById<ImageView>(id).setImageResource(dr)
        }
        when (activeIcon.id) {
            R.id.nav_home -> activeIcon.setImageResource(R.drawable.vec_home_active)
            R.id.nav_wallet -> activeIcon.setImageResource(R.drawable.vec_wallet_active)
            R.id.nav_reports -> activeIcon.setImageResource(R.drawable.vec_reports_active)
            R.id.nav_profile -> activeIcon.setImageResource(R.drawable.vec_profile_active)
        }
    }

    private data class Quadruple<A, B, C, D, E>(val first: A, val second: B, val third: C, val fourth: D, val fifth: E)
}
