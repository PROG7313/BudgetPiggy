package com.example.budgetpiggy

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
import com.example.budgetpiggy.data.database.AppDatabase
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.utils.ColorTemplate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class ReportsPage : BaseActivity() {

    private lateinit var dateRangeButton: Button
    private lateinit var totalBalanceChart: PieChart
    private lateinit var earningsChart: PieChart
    private lateinit var spendingChart: PieChart
    private lateinit var categoryChart: PieChart
    private lateinit var balanceTrendChart: LineChart
    private var lastSelectedCategories = mutableListOf("Groceries","Home","Vehicle","Food","Gifts")

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.reports)

        findViewById<ImageView>(R.id.piggyIcon).visibility = View.GONE
        findViewById<TextView>(R.id.greetingText).visibility = View.GONE
        findViewById<ImageView>(R.id.streakIcon).visibility = View.GONE

        val pageTitle = findViewById<TextView>(R.id.pageTitle)
        pageTitle.visibility = View.VISIBLE
        pageTitle.text = getString(R.string.reports)

        // edge-to-edge insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.reportsPage)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        // bind views
        dateRangeButton   = findViewById(R.id.dateRangeButton)
        totalBalanceChart = findViewById(R.id.totalBalanceChart)
        earningsChart     = findViewById(R.id.earningsChart)
        spendingChart     = findViewById(R.id.spendingChart)
        categoryChart     = findViewById(R.id.categoryChart)
        balanceTrendChart = findViewById(R.id.balanceTrendChart)

        // hide elements only used on Home/Wallet pages
        listOf(R.id.greetingText, R.id.piggyIcon, R.id.streakBadge)
            .forEach { findViewById<View>(it)?.visibility = View.GONE }

        // top bar: back & notifications
        findViewById<ImageView>(R.id.backArrow)
            .setOnClickListener { onBackPressedDispatcher.onBackPressed() }
        findViewById<ImageView>(R.id.bellIcon)
            .setOnClickListener {
                startActivity(Intent(this, Notification::class.java))
            }

        // filter icon
        findViewById<ImageView>(R.id.filterIcon)
            .setOnClickListener { showCategoryFilterDialog() }

        // bottom nav
        setupBottomNav()

        // date picker
        dateRangeButton.text = "Last 30 days"
        dateRangeButton.setOnClickListener { pickDateRange() }

        // load charts
        loadAllSampleCharts()
    }

    override fun onResume() {
        super.onResume()
        // highlight the Reports icon
        setActiveNavIcon(findViewById(R.id.nav_reports))
    }

    private fun setupBottomNav() {
        val navHome    = findViewById<ImageView>(R.id.nav_home)
        val navWallet  = findViewById<ImageView>(R.id.nav_wallet)
        val navReports = findViewById<ImageView>(R.id.nav_reports)
        val navProfile = findViewById<ImageView>(R.id.nav_profile)

        navHome.setOnClickListener { v: View ->
            setActiveNavIcon(v as ImageView)
            startActivity(Intent(this, HomePage::class.java))
        }
        navWallet.setOnClickListener { v: View ->
            setActiveNavIcon(v as ImageView)
            startActivity(Intent(this, WalletPage::class.java))
        }
        navReports.setOnClickListener { v: View ->
            setActiveNavIcon(v as ImageView)
            // Already here, no navigation
        }
        navProfile.setOnClickListener { v: View ->
            setActiveNavIcon(v as ImageView)
            startActivity(Intent(this, AccountPage::class.java))
        }
    }

    // --- Date range picker ---
    private fun pickDateRange() {
        val cal = Calendar.getInstance()
        DatePickerDialog(this,
            { _, y, m, d ->
                val startCal = Calendar.getInstance().apply { set(y, m, d) }
                DatePickerDialog(this,
                    { _, y2, m2, d2 ->
                        val endCal = Calendar.getInstance().apply { set(y2, m2, d2) }
                        val fmt = SimpleDateFormat("MMM d", Locale.getDefault())
                        dateRangeButton.text = "${fmt.format(startCal.time)} – ${fmt.format(endCal.time)}"
                        loadAllSampleCharts()
                    },
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)
                ).show()
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    // --- Chart loading (demo data) ---
    private fun loadAllSampleCharts() {
        lifecycleScope.launch(Dispatchers.IO) {
            val accountData  = listOf(PieEntry(600f,"Savings"), PieEntry(580f,"FNB"))
            val earningsData = listOf(PieEntry(600f,"Savings"), PieEntry(580f,"FNB"))
            val spendingData = listOf(PieEntry(550f,"Utilities"), PieEntry(400f,"Food"))
            val categoryData = listOf(PieEntry(5000f,"Groceries"), PieEntry(5000f,"Home"), PieEntry(4000f,"Vehicle"))
            val trend        = (0..6).map { Entry(it.toFloat(), (1000 + it * 50).toFloat()) }

            withContext(Dispatchers.Main) {
                setupPie(totalBalanceChart, accountData)
                setupPie(earningsChart, earningsData)
                setupPie(spendingChart, spendingData)
                setupPie(categoryChart, categoryData)
                setupLine(balanceTrendChart, trend)
                populateCategoryList(categoryData)
            }
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
        val ds = LineDataSet(entries, "Balance").apply {
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

    private fun populateCategoryList(entries: List<PieEntry>) {
        val container = findViewById<LinearLayout>(R.id.categoryList)
        container.removeAllViews()
        entries.forEach { e ->
            val view = layoutInflater.inflate(R.layout.item_category_row, container, false)
            val icon = view.findViewById<ImageView>(R.id.categoryIcon)
            val label= view.findViewById<TextView>(R.id.categoryLabel)
            when (e.label.lowercase(Locale.getDefault())) {
                "vehicle"   -> icon.setImageResource(R.drawable.vec_car)
                "home"      -> icon.setImageResource(R.drawable.vec_gift_circle)
                "groceries" -> icon.setImageResource(R.drawable.vec_food_circle)
                "food"      -> icon.setImageResource(R.drawable.vec_food_circle)
                else        -> icon.setImageResource(R.drawable.vec_filter)
            }
            label.text = "${e.label}: ${e.value}"
            container.addView(view)
        }
    }

    // --- Category filter dialog ---
    private fun showCategoryFilterDialog() {
        val all = listOf("Groceries","Home","Vehicle","Food","Gifts")
        val sel = all.map { lastSelectedCategories.contains(it) }.toBooleanArray()

        AlertDialog.Builder(this)
            .setTitle("Filter Categories")
            .setMultiChoiceItems(all.toTypedArray(), sel) { _, idx, checked ->
                sel[idx] = checked
            }
            .setPositiveButton("Apply") { dialog, _ ->
                lastSelectedCategories = all.filterIndexed { i, _ -> sel[i] }.toMutableList()
                val filtered = listOf(
                    PieEntry(5000f,"Groceries"),
                    PieEntry(5000f,"Home"),
                    PieEntry(4000f,"Vehicle"),
                    PieEntry(2000f,"Food"),
                    PieEntry(1000f,"Gifts")
                ).filter { lastSelectedCategories.contains(it.label) }
                setupPie(categoryChart, filtered)
                populateCategoryList(filtered)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
