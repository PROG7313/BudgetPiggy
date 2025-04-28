package com.example.budgetpiggy.ui.transaction

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.children
import androidx.lifecycle.lifecycleScope
import com.example.budgetpiggy.ui.settings.AccountPage
import com.example.budgetpiggy.ui.core.BaseActivity
import com.example.budgetpiggy.ui.home.HomePage
import com.example.budgetpiggy.ui.notifications.Notification
import com.example.budgetpiggy.R
import com.example.budgetpiggy.ui.wallet.WalletPage
import com.example.budgetpiggy.data.entities.TransactionEntity
import com.example.budgetpiggy.data.database.AppDatabase
import com.example.budgetpiggy.ui.reports.ReportsPage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.*
import kotlin.math.absoluteValue

class TransactionActivity : BaseActivity() {

    private val REQ_CAM = 1001
    private val REQ_GAL = 1002

    private var receiptUri: String? = null
    private var tempPhotoUri: Uri? = null
    private var pendingAccountId: String? = null
    private var pendingCategoryId: String? = null
    private var isExpense: Boolean = true  // NEW: Tracks if the transaction is an expense

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.transaction_funds)

        findViewById<ImageView>(R.id.piggyIcon).visibility = View.GONE
        findViewById<TextView>(R.id.greetingText).visibility = View.GONE
        findViewById<ImageView>(R.id.streakIcon).visibility = View.GONE
        findViewById<TextView>(R.id.pageTitle).apply {
            visibility = View.VISIBLE
            text = getString(R.string.make_transaction)
        }

        setupNavBar()
        setupInputs()
        setupIncomeExpenseToggle() // NEW
        setupDynamicAccounts()
        setupDynamicCategories()

        findViewById<Button>(R.id.btnAddReceipt).setOnClickListener { showReceiptChooser() }

        findViewById<Button>(R.id.btnConfirm).setOnClickListener {
            val amtInput = findViewById<EditText>(R.id.amountInput)
            val descInput = findViewById<EditText>(R.id.descriptionInput)
            val amt = amtInput.text.toString().toDoubleOrNull()
            if (amt == null || pendingAccountId == null) {
                Toast.makeText(this, "Enter amount & select account", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val d = findViewById<Spinner>(R.id.spinnerDay).selectedItem.toString().toInt()
            val m = findViewById<Spinner>(R.id.spinnerMonth).selectedItemPosition
            val y = findViewById<Spinner>(R.id.spinnerYear).selectedItem.toString().toInt()
            val cal = Calendar.getInstance().apply {
                set(y, m, d, 0, 0, 0)
                set(Calendar.MILLISECOND, 0)
            }

            val tx = TransactionEntity(
                transactionId = UUID.randomUUID().toString(),
                userId = getSharedPreferences("app_prefs", MODE_PRIVATE).getString("logged_in_user_id", "")!!,
                accountId = pendingAccountId!!,
                categoryId = pendingCategoryId,
                amount = if (isExpense) -amt.absoluteValue else amt.absoluteValue,
                description = descInput.text.toString().takeIf { it.isNotBlank() },
                date = cal.timeInMillis,
                receiptImageUrl = null,
                receiptLocalPath = receiptUri
            )

            lifecycleScope.launch(Dispatchers.IO) {
                AppDatabase.getDatabase(this@TransactionActivity).transactionDao().insert(tx)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@TransactionActivity, "Saved!", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }

    private fun setupIncomeExpenseToggle() {
        val btnExpense = findViewById<Button>(R.id.btnExpense)
        val btnIncome = findViewById<Button>(R.id.btnIncome)

        btnExpense.setOnClickListener {
            isExpense = true
            setToggleButtons(btnExpense, btnIncome)
        }

        btnIncome.setOnClickListener {
            isExpense = false
            setToggleButtons(btnIncome, btnExpense)
        }
    }

    private fun setupNavBar() {
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

        findViewById<ImageView>(R.id.backArrow).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        findViewById<ImageView>(R.id.bellIcon).setOnClickListener {
            startActivity(Intent(this, Notification::class.java))
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.transactionPage)) { v, insets ->
            val b = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(b.left, b.top, b.right, b.bottom)
            insets
        }
    }

    private fun setupInputs() {
        findViewById<Spinner>(R.id.spinnerDay).adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_item, (1..31).map { it.toString() })

        findViewById<Spinner>(R.id.spinnerMonth).adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_item, listOf(
                "Jan", "Feb", "Mar", "Apr", "May", "Jun",
                "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
            ))

        findViewById<Spinner>(R.id.spinnerYear).adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_item, (2020..2030).map { it.toString() })

        findViewById<ImageView>(R.id.datePrev).setOnClickListener { moveDate(-1, findViewById(R.id.spinnerDay)) }
        findViewById<ImageView>(R.id.dateNext).setOnClickListener { moveDate(+1, findViewById(R.id.spinnerDay)) }

        findViewById<Button>(R.id.btnTransferFunds).setOnClickListener {
            setToggleButtons(it as Button, findViewById(R.id.btnMakeTransaction))
            startActivity(Intent(this, TransferFunds::class.java))
        }

        findViewById<Button>(R.id.btnMakeTransaction).apply {
            isEnabled = false
            background = resources.getDrawable(R.drawable.bg_toggle_active, null)
            setTextColor(resources.getColor(android.R.color.white, null))
        }
    }

    private fun setupDynamicAccounts() {
        val acctContainer = findViewById<LinearLayout>(R.id.accountToggleContainer)
        lifecycleScope.launch {
            val userId = getSharedPreferences("app_prefs", MODE_PRIVATE).getString("logged_in_user_id", null) ?: return@launch
            val accts = withContext(Dispatchers.IO) {
                AppDatabase.getDatabase(this@TransactionActivity).accountDao().getByUserId(userId)
            }

            withContext(Dispatchers.Main) {
                acctContainer.removeAllViews()
                accts.forEachIndexed { idx, acct ->
                    val btn = Button(this@TransactionActivity).apply {
                        text = acct.accountName
                        tag = acct.accountId
                        val m = (8 * resources.displayMetrics.density).toInt()
                        setPadding(m, m, m, m)
                        background = resources.getDrawable(R.drawable.bg_toggle_inactive, null)
                        setOnClickListener {
                            acctContainer.children.forEach { (it as Button).apply {
                                background = resources.getDrawable(R.drawable.bg_toggle_inactive, null)
                                setTextColor(resources.getColor(R.color.black, null))
                            } }
                            background = resources.getDrawable(R.drawable.bg_toggle_active, null)
                            setTextColor(resources.getColor(android.R.color.white, null))
                            pendingAccountId = tag as String
                        }
                    }
                    acctContainer.addView(btn)
                    if (idx == 0) btn.performClick()
                }
            }
        }
    }

    private fun setupDynamicCategories() {
        val catContainer = findViewById<LinearLayout>(R.id.fromCategoryList)
        lifecycleScope.launch {
            val userId = getSharedPreferences("app_prefs", MODE_PRIVATE).getString("logged_in_user_id", null)
                ?: return@launch
            val cats = withContext(Dispatchers.IO) {
                AppDatabase.getDatabase(this@TransactionActivity).categoryDao().getByUserId(userId)
            }

            withContext(Dispatchers.Main) {
                cats.forEach { cat ->
                    val iv = ImageView(this@TransactionActivity).apply {
                        val padding = (8 * resources.displayMetrics.density).toInt()
                        setPadding(padding, padding, padding, padding)
                        alpha = 0.5f
                        tag = cat.categoryId

                        if (!cat.iconLocalPath.isNullOrBlank()) {
                            setImageURI(Uri.parse(cat.iconLocalPath))
                        } else {
                            val resName = cat.iconName ?: "vec_filter"
                            val resId = resources.getIdentifier(resName, "drawable", packageName)
                            setImageResource(resId)
                        }

                        setOnClickListener {
                            catContainer.children.forEach { it.alpha = 0.5f }
                            alpha = 1f
                            pendingCategoryId = cat.categoryId
                        }
                    }
                    catContainer.addView(iv)
                }
            }
        }
    }

    private fun showReceiptChooser() {
        val opts = arrayOf("Take Photo", "Choose from Gallery")
        AlertDialog.Builder(this)
            .setTitle("Add Receipt")
            .setItems(opts) { _, which ->
                if (which == 0) {
                    val photoFile = File.createTempFile("receipt_", ".jpg", cacheDir)
                    tempPhotoUri = FileProvider.getUriForFile(this, "$packageName.fileprovider", photoFile)
                    val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                        putExtra(MediaStore.EXTRA_OUTPUT, tempPhotoUri)
                        flags = Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION
                    }
                    packageManager.queryIntentActivities(intent, 0).forEach {
                        grantUriPermission(it.activityInfo.packageName, tempPhotoUri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    startActivityForResult(intent, REQ_CAM)
                } else {
                    val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    startActivityForResult(intent, REQ_GAL)
                }
            }.show()
    }

    override fun onActivityResult(req: Int, res: Int, data: Intent?) {
        super.onActivityResult(req, res, data)
        if (res != Activity.RESULT_OK) return

        when (req) {
            REQ_GAL -> {
                val sourceUri = data?.data ?: return
                try {
                    val inputStream = contentResolver.openInputStream(sourceUri)
                    val tempFile = File.createTempFile("receipt_gallery_", ".jpg", cacheDir)
                    tempFile.outputStream().use { output -> inputStream?.copyTo(output) }
                    receiptUri = Uri.fromFile(tempFile).toString()
                    Toast.makeText(this, "Gallery receipt saved", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this, "Failed to copy gallery image", Toast.LENGTH_SHORT).show()
                }
            }
            REQ_CAM -> {
                receiptUri = tempPhotoUri?.toString()
                Toast.makeText(this, "Camera receipt saved", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun moveDate(delta: Int, spinner: Spinner) {
        val newPos = spinner.selectedItemPosition + delta
        spinner.setSelection(newPos.coerceIn(0, spinner.count - 1))
    }

    private fun setToggleButtons(active: Button, inactive: Button) {
        active.background = resources.getDrawable(R.drawable.bg_toggle_active, null)
        active.setTextColor(resources.getColor(android.R.color.white, null))
        inactive.background = resources.getDrawable(R.drawable.bg_toggle_inactive, null)
        inactive.setTextColor(resources.getColor(R.color.black, null))
    }

    override fun setActiveNavIcon(activeIcon: ImageView) {
        listOf(
            R.id.nav_home to R.drawable.vec_home_inactive,
            R.id.nav_wallet to R.drawable.vec_wallet_inactive,
            R.id.nav_reports to R.drawable.vec_reports_inactive,
            R.id.nav_profile to R.drawable.vec_profile_inactive
        ).forEach { (id, drawable) ->
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
