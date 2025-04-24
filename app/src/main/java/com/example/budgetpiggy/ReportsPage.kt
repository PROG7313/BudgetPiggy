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
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.utils.ColorTemplate
import com.example.budgetpiggy.data.database.AppDatabase
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

        // window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.reportsPage)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        // bind views
        dateRangeButton    = findViewById(R.id.dateRangeButton)
        totalBalanceChart  = findViewById(R.id.totalBalanceChart)
        earningsChart      = findViewById(R.id.earningsChart)
        spendingChart      = findViewById(R.id.spendingChart)
        categoryChart      = findViewById(R.id.categoryChart)
        balanceTrendChart  = findViewById(R.id.balanceTrendChart)

        findViewById<ImageView>(R.id.filterIcon)?.setOnClickListener { showCategoryFilterDialog() }

        // hide home-only
        listOf(R.id.greetingText, R.id.piggyIcon, R.id.streakBadge).forEach {
            findViewById<View>(it)?.visibility = View.GONE
        }

        // top bar
        findViewById<ImageView>(R.id.backArrow)?.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        findViewById<ImageView>(R.id.bellIcon)?.setOnClickListener {
            startActivity(Intent(this, Notification::class.java))
        }

        setupBottomNav()

        // date picker
        dateRangeButton.setOnClickListener { pickDateRange() }
        dateRangeButton.text = "Last 30 days"

        loadAllSampleCharts()
    }

    private fun setupBottomNav() {
        // Map each nav icon to its target Activity
        val navMap = mapOf(
            R.id.nav_home    to HomePage::class.java,
            R.id.nav_wallet  to WalletPage::class.java,
            R.id.nav_reports to ReportsPage::class.java
            // R.id.nav_profile to ProfilePage::class.java
        )

        navMap.forEach { (navId, dest) ->
            findViewById<ImageView>(navId)?.setOnClickListener { iconView ->
                // 1) Highlight the clicked icon
                setActiveNavIcon(iconView as ImageView)
                // 2) Navigate
                startActivity(Intent(this, dest))
            }
        }

        // Make sure the Reports icon is highlighted on this page
        findViewById<ImageView>(R.id.nav_reports)
            ?.let { setActiveNavIcon(it) }
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
    private fun pickDateRange() {
        val cal = Calendar.getInstance()
        // Start picker
        DatePickerDialog(this,
            { _, y, m, d ->
                val startCal = Calendar.getInstance().apply { set(y, m, d) }
                // End picker
                DatePickerDialog(this,
                    { _, y2, m2, d2 ->
                        val endCal = Calendar.getInstance().apply { set(y2, m2, d2) }
                        val fmt = SimpleDateFormat("MMM d", Locale.getDefault())
                        dateRangeButton.text = "${fmt.format(startCal.time)} – ${fmt.format(endCal.time)}"
                        // TODO: call real DAO with startCal.timeInMillis, endCal.timeInMillis
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

    private fun loadAllSampleCharts() {
        lifecycleScope.launch(Dispatchers.IO) {
            val accountData = listOf(
                PieEntry(600f,"Savings"), PieEntry(580f,"FNB")
            )
            val earningsData = listOf(
                PieEntry(600f,"Savings"), PieEntry(580f,"FNB")
            )
            val spendingData = listOf(
                PieEntry(550f,"Utilities"), PieEntry(400f,"Food")
            )
            val categoryData = listOf(
                PieEntry(5000f,"Groceries"),
                PieEntry(5000f,"Home"),
                PieEntry(4000f,"Vehicle")
            )
            // trend
            val trend = (0..6).map { Entry(it.toFloat(), (1000 + it*50).toFloat()) }

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
        val ds = PieDataSet(entries,"").apply {
            colors = ColorTemplate.MATERIAL_COLORS.toList()
            valueTextSize = 14f
        }
        chart.data = PieData(ds)
        chart.description.isEnabled = false
        chart.legend.isEnabled = false
        chart.invalidate()
    }

    private fun setupLine(chart: LineChart, entries: List<Entry>) {
        val ds = LineDataSet(entries,"Balance").apply {
            setDrawValues(false)
            lineWidth = 2f
            circleRadius = 4f
        }
        chart.data = LineData(ds)
        chart.description.isEnabled = false
        chart.animateX(500)
        chart.invalidate()
    }

    private fun populateCategoryList(entries: List<PieEntry>) {
        val container = findViewById<LinearLayout>(R.id.categoryList)
        container.removeAllViews()
        entries.forEach { e ->
            val view = layoutInflater.inflate(R.layout.item_category_row, container, false)
            val icon = view.findViewById<ImageView>(R.id.categoryIcon)
            val label= view.findViewById<TextView>(R.id.categoryLabel)
            when(e.label.lowercase(Locale.getDefault())) {
                "vehicle"   -> icon.setImageResource(R.drawable.vec_car)
                "home"      -> icon.setImageResource(R.drawable.vec_gift_circle)
                "groceries"-> icon.setImageResource(R.drawable.vec_food_circle)
                "food"     -> icon.setImageResource(R.drawable.vec_food_circle)
                else       -> icon.setImageResource(R.drawable.vec_filter)
            }
            label.text = "${e.label}: ${e.value}"
            container.addView(view)
        }
    }

    private fun showCategoryFilterDialog() {
        val all = listOf("Groceries","Home","Vehicle","Food","Gifts")
        val sel = all.map{ lastSelectedCategories.contains(it) }.toBooleanArray()
        AlertDialog.Builder(this)
            .setTitle("Filter Categories")
            .setMultiChoiceItems(all.toTypedArray(), sel) { _, idx, checked ->
                sel[idx] = checked
            }
            .setPositiveButton("Apply") { d,_->
                val chosen = all.filterIndexed{ i,_-> sel[i] }
                lastSelectedCategories = chosen.toMutableList()
                val demo = listOf(
                    PieEntry(5000f,"Groceries"),
                    PieEntry(5000f,"Home"),
                    PieEntry(4000f,"Vehicle"),
                    PieEntry(2000f,"Food"),
                    PieEntry(1000f,"Gifts")
                ).filter{ chosen.contains(it.label) }
                setupPie(categoryChart, demo)
                populateCategoryList(demo)
                d.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
