package com.example.budgetpiggy

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class TransactionActivity : BaseActivity() {

    private val REQUEST_CAMERA = 1001
    private val REQUEST_GALLERY = 1002

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.transaction_funds)
        findViewById<ImageView>(R.id.piggyIcon).visibility = View.GONE
        findViewById<TextView>(R.id.greetingText).visibility = View.GONE
        findViewById<ImageView>(R.id.streakIcon).visibility = View.GONE

        val pageTitle = findViewById<TextView>(R.id.pageTitle)
        pageTitle.visibility = View.VISIBLE
        pageTitle.text = getString(R.string.make_transaction)
        // Insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.transactionPage)) { v, insets ->
            val b = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(b.left, b.top, b.right, b.bottom)
            insets
        }



        // Top bar
        findViewById<ImageView>(R.id.backArrow)?.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        findViewById<ImageView>(R.id.bellIcon)?.setOnClickListener {
            startActivity(Intent(this, Notification::class.java))
        }

        // Bottom nav
        setupBottomNav()

        // Main toggle
        val btnTrans = findViewById<Button>(R.id.btnTransferFunds)
        val btnMake  = findViewById<Button>(R.id.btnMakeTransaction)
        btnTrans.setOnClickListener {
            setToggleButtons(btnTrans, btnMake)
            startActivity(Intent(this, TransferFunds::class.java))
        }
        btnMake.setOnClickListener { setToggleButtons(btnMake, btnTrans) }
        btnMake.performClick()

        // Amount
        val amt = findViewById<EditText>(R.id.amountInput)
        amt.setOnFocusChangeListener { v, has ->
            if (has) {
                (getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager)
                    .showSoftInput(v, InputMethodManager.SHOW_IMPLICIT)
            }
        }

        // Account-type toggles
        val btnSav = findViewById<Button>(R.id.btnTypeSavings)
        val btnDeb = findViewById<Button>(R.id.btnTypeDebit)
        val btnChe = findViewById<Button>(R.id.btnTypeCheque)
        listOf(btnSav, btnDeb, btnChe).forEach { b ->
            b.setOnClickListener {
                listOf(btnSav, btnDeb, btnChe).forEach { it.background = resources
                    .getDrawable(R.drawable.bg_toggle_inactive, null) }
                b.background = resources.getDrawable(R.drawable.bg_toggle_active, null)
            }
        }
        btnSav.performClick()

        // Description
        // (nothing extra to wire)

        // Date spinners: populate day/month/year
        val days   = (1..31).map { it.toString() }
        val months = listOf("Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec")
        val years  = (2020..2030).map { it.toString() }
        findViewById<Spinner>(R.id.spinnerDay).adapter   =
            ArrayAdapter(this, android.R.layout.simple_spinner_item, days)
        findViewById<Spinner>(R.id.spinnerMonth).adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_item, months)
        findViewById<Spinner>(R.id.spinnerYear).adapter  =
            ArrayAdapter(this, android.R.layout.simple_spinner_item, years)

        // Prev/Next arrows (just increment/decrement day for demo)
        findViewById<ImageView>(R.id.datePrev).setOnClickListener { moveDate(-1) }
        findViewById<ImageView>(R.id.dateNext).setOnClickListener { moveDate(+1) }

        // From Category icons
        val catFrom = findViewById<LinearLayout>(R.id.fromCategoryList)
        fillCategories(catFrom)

        // Add receipt button
        findViewById<Button>(R.id.btnAddReceipt).setOnClickListener {
            showReceiptChooser()
        }

        // Confirm
        findViewById<Button>(R.id.btnConfirm).setOnClickListener {
            Toast.makeText(this, "Transaction saved!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun moveDate(delta: Int) {
        // stub: you could read spinnerDay.currentPosition, add delta, clamp, setSelection(...)
    }

    private fun fillCategories(container: LinearLayout) {
        container.removeAllViews()
        listOf(R.drawable.vec_car, R.drawable.vec_food_circle, R.drawable.vec_gift_circle)
            .forEach { res ->
                val iv = ImageView(this).apply {
                    setImageResource(res)
                    val pad = (8 * resources.displayMetrics.density).toInt()
                    setPadding(pad,pad,pad,pad)
                }
                container.addView(iv)
            }
    }

    private fun showReceiptChooser() {
        val opts = arrayOf("Take Photo","Choose from Gallery")
        AlertDialog.Builder(this)
            .setTitle("Add Receipt")
            .setItems(opts) { _, which ->
                if (which == 0) {
                    val cam = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    startActivityForResult(cam, REQUEST_CAMERA)
                } else {
                    val pick = Intent(Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    startActivityForResult(pick, REQUEST_GALLERY)
                }
            }
            .show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK) return

        val imgUri: Uri? = when (requestCode) {
            REQUEST_CAMERA -> data?.extras?.get("data") as? Uri
            REQUEST_GALLERY -> data?.data
            else -> null
        }
        if (imgUri != null) {
            Toast.makeText(this, "Receipt: $imgUri", Toast.LENGTH_SHORT).show()
            // TODO: display thumbnail or save uri
        }
    }

    private fun setupBottomNav() {
        val navMap = mapOf(
            R.id.nav_home    to HomePage::class.java,
            R.id.nav_wallet  to WalletPage::class.java,
            R.id.nav_reports to ReportsPage::class.java
            // R.id.nav_profile to ProfilePage::class.java
        )
        navMap.forEach { (id, cls) ->
            findViewById<ImageView>(id)?.setOnClickListener { icon ->
                setActiveNavIcon(icon as ImageView)
                startActivity(Intent(this, cls))
            }
        }

    }
    private fun setToggleButtons(active: Button, inactive: Button) {
        active.background   = resources.getDrawable(R.drawable.bg_toggle_active, null)
        active.setTextColor(resources.getColor(android.R.color.white, null))
        inactive.background = resources.getDrawable(R.drawable.bg_toggle_inactive, null)
        inactive.setTextColor(resources.getColor(R.color.black, null))
    }

    override fun setActiveNavIcon(activeIcon: ImageView) {
        val nav = listOf(
            R.id.nav_home    to R.drawable.vec_home_inactive,
            R.id.nav_wallet  to R.drawable.vec_wallet_inactive,
            R.id.nav_reports to R.drawable.vec_reports_inactive,
            R.id.nav_profile to R.drawable.vec_profile_inactive
        )
        nav.forEach { (id, drawable) ->
            findViewById<ImageView>(id).setImageResource(drawable)
        }
        when (activeIcon.id) {
            R.id.nav_home    -> activeIcon.setImageResource(R.drawable.vec_home_active)
            R.id.nav_wallet  -> activeIcon.setImageResource(R.drawable.vec_wallet_active)
            R.id.nav_reports -> activeIcon.setImageResource(R.drawable.vec_reports_active)
            R.id.nav_profile -> activeIcon.setImageResource(R.drawable.vec_profile_active)
        }
    }
}
