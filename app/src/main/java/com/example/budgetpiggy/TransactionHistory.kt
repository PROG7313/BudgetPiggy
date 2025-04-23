package com.example.budgetpiggy

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.text.SimpleDateFormat
import java.util.*
import androidx.core.view.isVisible

class TransactionHistory : BaseActivity() {

    private lateinit var transactionListLayout: LinearLayout
    private lateinit var transactions: MutableList<Map<String, Any>>
    private val selectedCategories = mutableListOf<String>()
    private var startDate: Date? = null
    private var endDate: Date? = null
    private val sdf = SimpleDateFormat("yyyy/MM/dd", Locale.US)
    private var filteredTransactions: List<Map<String, Any>> = listOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.transaction_history)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.transactionHistoryPage)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        transactionListLayout = findViewById(R.id.transactionList)
        val navHome = findViewById<ImageView>(R.id.nav_home)
        val navWallet = findViewById<ImageView>(R.id.nav_wallet)
        val navReports = findViewById<ImageView>(R.id.nav_reports)
        val navProfile = findViewById<ImageView>(R.id.nav_profile)
        val sortByText = findViewById<TextView>(R.id.sortByText)
        val filterText = findViewById<TextView>(R.id.filterText)

        val categories = List(30) { listOf("Groceries", "Clothing", "Motor", "Charity", "Home", "Gift")[it % 6] }
        val amounts = List(30) { listOf("R5000", "R5000", "R500", "R1000", "R2000", "R5000")[it % 6] }
        val accountTypes = List(30) { listOf("Cheque", "Cheque", "Cheque", "Savings", "Cheque", "Cheque")[it % 6] }
        val dates = List(30) { listOf("2025/04/07", "2025/04/06", "2025/04/05", "2025/04/04", "2025/04/03", "2025/04/02")[it % 6] }
        val icons = List(30) {
            listOf(
                R.drawable.vec_food_circle,
                R.drawable.vec_clothes_circle,
                R.drawable.vec_car,
                R.drawable.vec_donation_circle,
                R.drawable.vec_home_circle,
                R.drawable.vec_gift_circle
            )[it % 6]
        }

        transactions = List(30) {
            mapOf(
                "category" to categories[it],
                "amount" to amounts[it],
                "account" to accountTypes[it],
                "date" to dates[it],
                "icon" to icons[it]
            )
        }.toMutableList()

        sortByText.setOnClickListener { showSortDialog() }
        filterText.setOnClickListener { showFilterDialog() }

        navHome.setOnClickListener {
                view ->

            setActiveNavIcon(navHome)
            view.animate()
                .scaleX(0.95f)
                .scaleY(0.95f)
                .setDuration(25)
                .withEndAction {
                    view.animate().scaleX(1f).scaleY(1f).setDuration(25).start()
                    startActivity(Intent(this, HomePage::class.java))
                }.start()


        }
        navWallet.setOnClickListener {

            view ->
            setActiveNavIcon(navWallet)

            view.animate()
                .scaleX(0.95f)
                .scaleY(0.95f)
                .setDuration(25)
                .withEndAction {
                    view.animate().scaleX(1f).scaleY(1f).setDuration(25).start()
                    startActivity(Intent(this, WalletPage::class.java))
                }.start()

        }
        navReports.setOnClickListener { setActiveNavIcon(navReports) }
        navProfile.setOnClickListener { setActiveNavIcon(navProfile) }

        val scrollView = findViewById<ScrollView>(R.id.scrollArea)
        val fabWrapper = findViewById<View>(R.id.fabWrapper)

        var lastScrollY = 0
        scrollView.viewTreeObserver.addOnScrollChangedListener {
            val scrollY = scrollView.scrollY
            val isScrollingUp = scrollY < lastScrollY
            val isAtTop = !scrollView.canScrollVertically(-1)
            val isAtBottom = !scrollView.canScrollVertically(1)

            when {
                isScrollingUp || isAtTop || isAtBottom -> {
                    fabWrapper.visibility = View.VISIBLE
                    fabWrapper.animate().alpha(1f).setDuration(1).start()
                }
                else -> {
                    fabWrapper.animate().alpha(0f).setDuration(1)
                        .withEndAction { fabWrapper.visibility = View.GONE }.start()
                }
            }
            lastScrollY = scrollY
        }

        filteredTransactions = transactions
        renderTransactions(filteredTransactions)
    }

    private fun showFilterDialog() {
        val categories = arrayOf("Groceries", "Clothing", "Motor", "Charity", "Home", "Gift")
        val checkedItems = categories.map { selectedCategories.contains(it) }.toBooleanArray()

        val dialogView = layoutInflater.inflate(R.layout.dialog_filter, null)
        val startDateLabel = dialogView.findViewById<TextView>(R.id.startDateLabel)
        val endDateLabel = dialogView.findViewById<TextView>(R.id.endDateLabel)
        val startDateTap = dialogView.findViewById<TextView>(R.id.startDateTap)
        val endDateTap = dialogView.findViewById<TextView>(R.id.endDateTap)

        val today = Calendar.getInstance().time
        if (startDate == null) startDate = today
        if (endDate == null) endDate = today

        startDateLabel.text = "Start Date: ${sdf.format(startDate!!)}"
        endDateLabel.text = "End Date: ${sdf.format(endDate!!)}"

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

        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Filter by Category & Date")
            .setView(dialogView)
            .setMultiChoiceItems(categories, checkedItems) { _, which, isChecked ->
                if (isChecked) selectedCategories.add(categories[which])
                else selectedCategories.remove(categories[which])
            }
            .setPositiveButton("Apply") { _, _ -> applyCombinedFilter() }
            .setNeutralButton("Reset") { _, _ ->
                selectedCategories.clear()
                startDate = null
                endDate = null
                filteredTransactions = transactions
                renderTransactions(filteredTransactions)
            }
            .setNegativeButton("Cancel", null)

        builder.show()
    }

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

    private fun applyCombinedFilter() {
        filteredTransactions = transactions.filter {
            val catMatch = selectedCategories.isEmpty() || selectedCategories.contains(it["category"])
            val date = sdf.parse(it["date"].toString())
            val dateMatch = (startDate == null || !date.before(startDate)) &&
                    (endDate == null || !date.after(endDate))
            catMatch && dateMatch
        }
        renderTransactions(filteredTransactions)
    }

    private fun renderTransactions(data: List<Map<String, Any>>) {
        transactionListLayout.removeAllViews()

        for (item in data) {
            val row = LayoutInflater.from(this).inflate(R.layout.transaction_item_row, transactionListLayout, false)

            val iconImage = row.findViewById<ImageView>(R.id.iconImage)
            val categoryText = row.findViewById<TextView>(R.id.categoryText)
            val accountText = row.findViewById<TextView>(R.id.accountText)
            val dateText = row.findViewById<TextView>(R.id.dateText)
            val eyeIcon = row.findViewById<ImageView>(R.id.eyeIcon)
            val expandedContent = row.findViewById<LinearLayout>(R.id.expandedContent)
            val descriptionText = row.findViewById<TextView>(R.id.descriptionText)
            val receiptImage = row.findViewById<ImageView>(R.id.receiptImage)
            val toggleRow = row.findViewById<View>(R.id.toggleRowArea)

            iconImage.setImageResource(item["icon"] as Int)
            categoryText.text = "${item["category"]} ${item["amount"]}"
            accountText.text = "Account ${item["account"]}"
            dateText.text = item["date"].toString()
            descriptionText.text = "Baby City\n2 Items: Card Payment"
            receiptImage.setImageResource(R.drawable.pic_receipt_1)

            receiptImage.setOnClickListener {
                showReceiptFullScreen(R.drawable.pic_receipt_1)
            }

            toggleRow.setOnClickListener {
                val isVisible = expandedContent.isVisible
                expandedContent.visibility = if (isVisible) View.GONE else View.VISIBLE
                eyeIcon.setImageResource(
                    if (isVisible) R.drawable.vec_eye_open else R.drawable.vec_eye_closed
                )
            }

            transactionListLayout.addView(row)
        }
    }


    private fun showReceiptFullScreen(drawableRes: Int) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_receipt_preview, null)
        val imageView = dialogView.findViewById<ImageView>(R.id.fullscreenReceiptImage)
        imageView.setImageResource(drawableRes)

        val dialog = android.app.AlertDialog.Builder(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
            .setView(dialogView)
            .create()

        imageView.setOnClickListener { dialog.dismiss() } // Tap to close
        dialog.show()
    }


    private fun showSortDialog() {
        val options = arrayOf("Date: Newest First", "Date: Oldest First", "Category: A-Z", "Category: Z-A")
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle("Sort Transactions By")
        builder.setItems(options) { _, which ->
            when (which) {
                0 -> sortTransactionsByDate(true)
                1 -> sortTransactionsByDate(false)
                2 -> sortTransactionsByCategory(true)
                3 -> sortTransactionsByCategory(false)
            }
        }
        builder.show()
    }

    private fun sortTransactionsByDate(descending: Boolean) {
        val sorted = if (descending) {
            filteredTransactions.sortedByDescending { it["date"].toString() }
        } else {
            filteredTransactions.sortedBy { it["date"].toString() }
        }
        renderTransactions(sorted)
    }

    private fun sortTransactionsByCategory(ascending: Boolean) {
        val sorted = if (ascending) {
            filteredTransactions.sortedBy { it["category"].toString() }
        } else {
            filteredTransactions.sortedByDescending { it["category"].toString() }
        }
        renderTransactions(sorted)
    }

    private fun setActiveNavIcon(activeIcon: ImageView) {
        val navIcons = listOf(
            R.id.nav_home to R.drawable.vec_home_inactive,
            R.id.nav_wallet to R.drawable.vec_wallet_inactive,
            R.id.nav_reports to R.drawable.vec_reports_inactive,
            R.id.nav_profile to R.drawable.vec_profile_inactive
        )
        for ((id, drawable) in navIcons) {
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
