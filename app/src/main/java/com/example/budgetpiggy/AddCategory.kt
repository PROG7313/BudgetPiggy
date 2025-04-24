package com.example.budgetpiggy

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class AddCategoryPage : BaseActivity() {
    private val REQUEST_ICON = 2001
    private var selectedIconUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.addcategory)

        // Insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.addCategoryPage)) { v, ins ->
            val b = ins.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(b.left, b.top, b.right, b.bottom)
            ins
        }

        // Hide home bits
        listOf(R.id.greetingText, R.id.piggyIcon, R.id.streakBadge).forEach {
            findViewById<View>(it)?.visibility = View.GONE
        }

        // Top bar
        findViewById<ImageView>(R.id.backArrow)?.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        findViewById<ImageView>(R.id.bellIcon)?.setOnClickListener {
            startActivity(Intent(this, Notification::class.java))
        }



        findViewById<ImageView>(R.id.nav_wallet)?.let { setActiveNavIcon(it) }

        // 1) Pick an icon
        findViewById<ImageButton>(R.id.btnAddIcon)?.setOnClickListener {
            // only allow images
            val pick = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "image/*"
            }
            startActivityForResult(pick, REQUEST_ICON)
        }

        // 2) Show system keyboard on amount focus
        findViewById<EditText>(R.id.amountInput).setOnFocusChangeListener { v, has ->
            if (has) {
                (getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager)
                    .showSoftInput(v, InputMethodManager.SHOW_IMPLICIT)
            }
        }

        // 3) Populate Link-to-Account spinner
        val accounts = listOf("Savings","Debit","Cheque")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, accounts)
        findViewById<Spinner>(R.id.spinnerLinkAccount).adapter = adapter

        // 4) Confirm
        findViewById<Button>(R.id.btnConfirm).setOnClickListener {
            // TODO: save CategoryEntity(...) with selectedIconUri (you can store URI or copy to local file)
            Toast.makeText(this, "Category added!", Toast.LENGTH_SHORT).show()
            finish()
        }
        val navWallet = findViewById<ImageView>(R.id.nav_wallet)
        val navReports = findViewById<ImageView>(R.id.nav_reports)
        val navProfile = findViewById<ImageView>(R.id.nav_profile)
        val backArrow = findViewById<ImageView>(R.id.backArrow)
        val bellIcon = findViewById<ImageView>(R.id.bellIcon)

        findViewById<ImageView>(R.id.piggyIcon)?.visibility = View.GONE
        findViewById<ImageView>(R.id.streakIcon)?.visibility = View.GONE
        findViewById<TextView>(R.id.greetingText)?.visibility = View.GONE


        val pageTitle = findViewById<TextView>(R.id.pageTitle)
        pageTitle.visibility = View.VISIBLE
        pageTitle.text = getString(R.string.add_category)

        backArrow.setOnClickListener {
                view ->

            view.animate()
                .scaleX(0.95f)
                .scaleY(0.95f)
                .setDuration(25)
                .withEndAction {
                    view.animate().scaleX(1f).scaleY(1f).setDuration(25).start()
                    onBackPressedDispatcher.onBackPressed()
                }.start()

        }

        bellIcon.setOnClickListener {
            startActivity(Intent(this, Notification::class.java))
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
        navReports.setOnClickListener {
            setActiveNavIcon(navReports)
            startActivity(Intent(this, ReportsPage::class.java))
        }
        navProfile.setOnClickListener {
            setActiveNavIcon(navProfile)
            startActivity(Intent(this, AccountPage::class.java))
        }
    }


override fun setActiveNavIcon(activeIcon: ImageView) {
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
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_ICON && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                selectedIconUri = uri
                // load into the button
                findViewById<ImageButton>(R.id.btnAddIcon)
                    .setImageURI(uri)
                // optionally: resize/crop to a square in code or use a cropping library
            }
        }
    }
}
