package com.example.budgetpiggy.ui.transaction

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.budgetpiggy.ui.settings.AccountPage
import com.example.budgetpiggy.ui.core.BaseActivity
import com.example.budgetpiggy.ui.home.HomePage
import com.example.budgetpiggy.ui.notifications.Notification
import com.example.budgetpiggy.R
import com.example.budgetpiggy.ui.wallet.WalletPage
import com.example.budgetpiggy.data.database.AppDatabase
import com.example.budgetpiggy.data.entities.TransactionEntity
import com.example.budgetpiggy.ui.reports.ReportsPage
import com.example.budgetpiggy.utils.CurrencyManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.absoluteValue
import androidx.core.net.toUri

class TransactionHistory : BaseActivity() {

    private lateinit var transactionListLayout: LinearLayout
    private var allTransactions: List<TransactionEntity> = emptyList()
    private var filteredTransactions: List<TransactionEntity> = emptyList()

    private val selectedCategories = mutableSetOf<String>()
    private var startDate: Date? = null
    private var endDate: Date? = null
    private val sdf = SimpleDateFormat("yyyy/MM/dd", Locale.US)

    private lateinit var formatter: NumberFormat
    private var conversionRate: Double = 1.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.transaction_history)

        // Hide icons not needed for this page
        findViewById<ImageView>(R.id.piggyIcon).visibility = View.GONE
        findViewById<ImageView>(R.id.streakIcon).visibility = View.GONE
        // Set page title
        findViewById<TextView>(R.id.greetingText).apply {
            visibility = View.VISIBLE
            text = getString(R.string.transaction_history)
        }

        // Handle safe insets for immersive mode
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.transactionHistoryPage)) { v, insets ->
            val sb = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(sb.left, sb.top, sb.right, sb.bottom)
            insets
        }
        // FAB scroll behaviour
        val scrollView = findViewById<ScrollView>(R.id.scrollArea)
        val fabWrapper = findViewById<View>(R.id.fabWrapper)
        // FAB behavior
        setupFabScrollBehavior(scrollView, fabWrapper)

        findViewById<ImageView>(R.id.fabPlus)?.setOnClickListener {
            startActivity(Intent(this, TransactionActivity::class.java))
        }
        findViewById<ImageView>(R.id.bellIcon).setOnClickListener {
            startActivity(Intent(this, Notification::class.java))
        }

        findViewById<ImageView>(R.id.backArrow).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Bottom nav bar routing
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

        // Setup transaction list and filter/sort actions.
        transactionListLayout = findViewById(R.id.transactionList)
        findViewById<TextView>(R.id.sortByText).setOnClickListener { showSortDialog() }
        findViewById<TextView>(R.id.filterText).setOnClickListener { showFilterDialog() }

        // Load and render transacitons
        lifecycleScope.launch {
            val userId = getSharedPreferences("app_piggy_prefs", MODE_PRIVATE)
                .getString("logged_in_user_id", null) ?: return@launch

            val db = AppDatabase.getDatabase(this@TransactionHistory)
            val user = withContext(Dispatchers.IO) { db.userDao().getById(userId) } ?: return@launch

            val rateMap = CurrencyManager.getRateMap(this@TransactionHistory, "ZAR")

            conversionRate = rateMap[user.currency] ?: 1.0
            formatter = NumberFormat.getCurrencyInstance().apply {
                currency = Currency.getInstance(user.currency)
            }

            // Load and sort transaction from newest to oldest
            allTransactions = withContext(Dispatchers.IO) {
                db.transactionDao().getByUserId(userId).sortedByDescending { it.date }
            }

            filteredTransactions = allTransactions
            renderTransactions(formatter, conversionRate)
        }
    }

    // Renders each transaction row in the UI (Android, 2025) (Developers, 2025)
    private fun renderTransactions(formatter: NumberFormat, conversionRate: Double) {
        transactionListLayout.removeAllViews()

        for (tx in filteredTransactions) {
            val row = LayoutInflater.from(this)
                .inflate(R.layout.transaction_item_row, transactionListLayout, false)

            // View bingings
            val iconImage = row.findViewById<ImageView>(R.id.iconImage)
            val categoryText = row.findViewById<TextView>(R.id.categoryText)
            val accountText = row.findViewById<TextView>(R.id.accountText)
            val dateText = row.findViewById<TextView>(R.id.dateText)
            val eyeIcon = row.findViewById<ImageView>(R.id.eyeIcon)
            val expandedLayout = row.findViewById<LinearLayout>(R.id.expandedContent)
            val descText = row.findViewById<TextView>(R.id.descriptionText)
            val receiptImage = row.findViewById<ImageView>(R.id.receiptImage)
            val toggleArea = row.findViewById<View>(R.id.toggleRowArea)

            // Format amount with correct prefix and convection
            val convertedAmount = tx.amount * conversionRate
            val prefix = if (convertedAmount < 0) "-" else "+"
            val amtFormatted = formatter.format(convertedAmount.absoluteValue)

            // Initial display
            categoryText.text = "$prefix$amtFormatted"
            categoryText.setTextColor(
                if (tx.amount < 0) getColor(R.color.red)
                else getColor(R.color.black)
            )

            descText.text = tx.description ?: ""
            dateText.text = sdf.format(Date(tx.date))

            // Load receipt image if available
            if (!tx.receiptLocalPath.isNullOrBlank()) {
                val uri = tx.receiptLocalPath.toUri()
                receiptImage.visibility = View.VISIBLE
                Glide.with(this).load(uri).into(receiptImage)
                receiptImage.setOnClickListener { showReceiptFullScreen(uri) }
            } else {
                receiptImage.visibility = View.GONE
            }

            toggleArea.setOnClickListener {
                val expanded = expandedLayout.isVisible
                expandedLayout.visibility = if (expanded) View.GONE else View.VISIBLE
                eyeIcon.setImageResource(if (expanded) R.drawable.vec_eye_open else R.drawable.vec_eye_closed)
            }

            // Fetch category and accounts info for transacitons
            lifecycleScope.launch {
                val db = AppDatabase.getDatabase(this@TransactionHistory)
                val acct = withContext(Dispatchers.IO) { db.accountDao().getById(tx.accountId) }
                val cat = tx.categoryId?.let {
                    withContext(Dispatchers.IO) { db.categoryDao().getById(it) }
                }

                accountText.text = "Acct: ${acct?.accountName ?: "?"}"
                val isAllocation = tx.description?.startsWith("Allocated to category:") == true
                val categoryLabel = if (isAllocation) "[Allocated] ${cat?.categoryName}" else cat?.categoryName ?: "Unk"
                val adjustedPrefix = if (isAllocation) "" else prefix

                categoryText.text = "$categoryLabel  $adjustedPrefix$amtFormatted"
                categoryText.setTextColor(
                    if (!isAllocation && tx.amount < 0) getColor(R.color.red)
                    else getColor(R.color.black)
                )


                if (cat?.iconLocalPath != null) {
                    iconImage.setImageURI(cat.iconLocalPath.toUri())
                } else {
                    val resName = cat?.iconName ?: "vec_filter"
                    val resId = resources.getIdentifier(resName, "drawable", packageName)
                    iconImage.setImageResource(resId)
                }
            }

            transactionListLayout.addView(row)
        }
    }

    // Show receipt image in fullscreen dialog
    private fun showReceiptFullScreen(uri: Uri) {
        val dialogView = LayoutInflater.from(this)
            .inflate(R.layout.dialog_receipt_preview, null)
        val img = dialogView.findViewById<ImageView>(R.id.fullscreenReceiptImage)
        Glide.with(this).load(uri).into(img)
        val dlg = AlertDialog.Builder(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
            .setView(dialogView).create()
        img.setOnClickListener { dlg.dismiss() }
        dlg.show()
    }

    // Opens date picker for selecting filter dates
    private fun showFilterDialog() {
        lifecycleScope.launch {
            // Get database instance, fetch according information
            val db = AppDatabase.getDatabase(this@TransactionHistory)
            val catIds = allTransactions.mapNotNull { it.categoryId }.distinct()
            val catEntities = withContext(Dispatchers.IO) {
                catIds.mapNotNull { id -> db.categoryDao().getById(id) }
            }

            val nameToId = catEntities.associate { it.categoryName to it.categoryId }
            val names = nameToId.keys.toTypedArray()
            val checked = names.map { nameToId[it] in selectedCategories }.toBooleanArray()

            // Inflate the filter dialog layout
            val view = layoutInflater.inflate(R.layout.dialog_filter, null)
            val startDateLabel = view.findViewById<TextView>(R.id.startDateLabel)
            val endDateLabel = view.findViewById<TextView>(R.id.endDateLabel)
            val startDateTap = view.findViewById<TextView>(R.id.startDateTap)
            val endDateTap = view.findViewById<TextView>(R.id.endDateTap)

            if (startDate == null) startDate = Calendar.getInstance().time
            if (endDate == null) endDate = Calendar.getInstance().time

            startDateLabel.text = "Start Date: ${sdf.format(startDate!!)}"
            endDateLabel.text = "End Date: ${sdf.format(endDate!!)}"

            // Show date picker when start date field is tapped
            startDateTap.setOnClickListener {
                pickDate { date ->
                    startDate = date
                    startDateLabel.text = "Start Date: ${sdf.format(date)}"
                }
            }

            endDateTap.setOnClickListener {
                pickDate { date ->
                    endDate = date
                    endDateLabel.text = "End Date: ${sdf.format(date)}"
                }
            }

            // Show filter dialog with  multi choice category selection
            AlertDialog.Builder(this@TransactionHistory)
                .setTitle("Filter")
                .setView(view)
                .setMultiChoiceItems(names, checked) { _, i, isChecked ->
                    val id = nameToId[names[i]]
                    if (id != null) {
                        if (isChecked) selectedCategories += id else selectedCategories -= id
                    }
                }
                .setPositiveButton("Apply") { _, _ -> applyFilter() }
                .setNeutralButton("Reset") { _, _ ->
                    selectedCategories.clear()
                    startDate = null
                    endDate = null
                    filteredTransactions = allTransactions
                    renderTransactions(formatter, conversionRate)
                }
                .show()
        }
    }

    // Applies category and date filters to the transactions
    private fun applyFilter() {
        filteredTransactions = allTransactions.filter { tx ->
            val catOk = selectedCategories.isEmpty() || selectedCategories.contains(tx.categoryId)
            val date = sdf.parse(sdf.format(Date(tx.date)))!!
            val dateOk = (startDate?.let { !date.before(it) } ?: true) &&
                    (endDate?.let { !date.after(it) } ?: true)
            catOk && dateOk
        }
        renderTransactions(formatter, conversionRate)
    }

    // Opens date picker and returns the selected date through a callback (GeekforGeeks, 2022)
    private fun pickDate(callback: (Date) -> Unit) {
        val calendar = Calendar.getInstance()
        val dpd = DatePickerDialog(this,
            { _, year, month, day ->
                calendar.set(year, month, day)
                callback(calendar.time)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        dpd.show()
    }

    // Shows a sorting dialog for transactions
    private fun showSortDialog() {
        val opts = arrayOf("Date ↓", "Date ↑", "Category A→Z", "Category Z→A")
        AlertDialog.Builder(this)
            .setTitle("Sort by")
            .setItems(opts) { _, which ->
                filteredTransactions = when (which) {
                    0 -> filteredTransactions.sortedByDescending { it.date }
                    1 -> filteredTransactions.sortedBy { it.date }
                    2 -> filteredTransactions.sortedBy { it.categoryId }
                    else -> filteredTransactions.sortedByDescending { it.categoryId }
                }
                renderTransactions(formatter, conversionRate)
            }
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
}