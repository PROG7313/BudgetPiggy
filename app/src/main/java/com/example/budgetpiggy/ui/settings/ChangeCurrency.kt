package com.example.budgetpiggy.ui.settings

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.budgetpiggy.ui.core.BaseActivity
import com.example.budgetpiggy.ui.home.HomePage
import com.example.budgetpiggy.ui.notifications.Notification
import com.example.budgetpiggy.R
import com.example.budgetpiggy.ui.reports.ReportsPage
import com.example.budgetpiggy.ui.wallet.WalletPage
import com.example.budgetpiggy.data.database.AppDatabase
import com.example.budgetpiggy.utils.CurrencyManager
import com.example.budgetpiggy.utils.NetworkUtils
import com.example.budgetpiggy.utils.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.NumberFormat
import java.util.Currency

class ChangeCurrency : BaseActivity() {

    private lateinit var tvCurrent: TextView
    private lateinit var spinnerChoose: Spinner
    private lateinit var btnSave: Button

    // Will store the currency code selected from the Spinner but not yet saved (Android, 2025).
    private var pendingSelection: String? = null

    // List of all currency codes retrieved from the exchange rate map (GeeksforGeeks, 2022).
    private var allCodes = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // Allows full screen layout (tech, 2023).
        setContentView(R.layout.change_currency)

        // Hide unused icons in the top bar (Developers, 2025).
        findViewById<ImageView>(R.id.piggyIcon).visibility = View.GONE
        findViewById<TextView>(R.id.greetingText).visibility = View.GONE
        findViewById<ImageView>(R.id.streakIcon).visibility = View.GONE

        // Set the page title (Developers, 2025).
        findViewById<TextView>(R.id.pageTitle).apply {
            visibility = View.VISIBLE
            text = getString(R.string.change_currency)
        }

        // Handle system bars with proper padding (tech, 2023).
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.currencyPage)) { v, insets ->
            val b = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(b.left, b.top, b.right, b.bottom)
            insets
        }

        // Bottom navigation actions (Main, 2025).
        findViewById<ImageView>(R.id.nav_home).setOnClickListener {
            setActiveNavIcon(it as ImageView)
            startActivity(Intent(this, HomePage::class.java))
        }

        findViewById<ImageView>(R.id.nav_wallet).setOnClickListener {
            setActiveNavIcon(it as ImageView)
            it.animate().scaleX(0.95f).scaleY(0.95f).setDuration(25).withEndAction {
                it.animate().scaleX(1f).scaleY(1f).setDuration(25).start()
                startActivity(Intent(this, WalletPage::class.java))
            }.start()
        }

        findViewById<ImageView>(R.id.nav_reports).setOnClickListener {
            setActiveNavIcon(it as ImageView)
            startActivity(Intent(this, ReportsPage::class.java))
        }

        findViewById<ImageView>(R.id.nav_profile).setOnClickListener {
            setActiveNavIcon(it as ImageView)
            startActivity(Intent(this, AccountPage::class.java))
        }

        // Navigation for back and notifications (Developers, 2025).
        findViewById<ImageView>(R.id.backArrow).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        findViewById<ImageView>(R.id.bellIcon).setOnClickListener {
            startActivity(Intent(this, Notification::class.java))
        }

        // Bind views
        tvCurrent = findViewById(R.id.tvCurrentCurrency)
        spinnerChoose = findViewById(R.id.spinnerChooseCurrency)
        btnSave = findViewById(R.id.btnSaveCurrency)

        // Load user ID from session (CodingStuff, 2024).
        val userId = SessionManager.getUserId(this) ?: return

        // Launch coroutine to load and display data (Gaur, 2025).
        lifecycleScope.launch {
            val rateMap = withContext(Dispatchers.IO) {
                // Fetches cached or fresh exchange rates for ZAR (Ambitions, 2025).
                CurrencyManager.getRateMap(this@ChangeCurrency, "ZAR")
            }

            // Show message if offline (Developers, 2025).
            if (!NetworkUtils.isInternetAvailable(this@ChangeCurrency)) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@ChangeCurrency,
                        "No internet: showing last saved rates",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            // Load the logged-in user from Room DB (CodingStuff, 2024).
            val user = withContext(Dispatchers.IO) {
                AppDatabase.getDatabase(this@ChangeCurrency).userDao().getById(userId)
            } ?: return@launch

            withContext(Dispatchers.Main) {
                // Display current currency and rate
                val currentCode = user.currency
                val currentRate = rateMap[currentCode] ?: 1.0

                tvCurrent.text = "$currentCode = ${
                    NumberFormat.getCurrencyInstance().apply {
                        currency = Currency.getInstance(currentCode)
                    }.format(currentRate)
                }"

                // Populate the Spinner with available currency codes (GeeksforGeeks, 2022).
                allCodes = rateMap.keys.sorted().toMutableList()

                spinnerChoose.adapter = ArrayAdapter(
                    this@ChangeCurrency,
                    android.R.layout.simple_spinner_dropdown_item,
                    allCodes
                )

                spinnerChoose.setSelection(allCodes.indexOf(currentCode).coerceAtLeast(0))
                pendingSelection = currentCode

                // Spinner listener (Main, 2025).
                spinnerChoose.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onNothingSelected(parent: AdapterView<*>?) {}
                    override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                        pendingSelection = allCodes[position]
                        val rate = rateMap[pendingSelection] ?: Double.NaN
                        Log.d("ChangeCurrency", "Selected $pendingSelection → rate ${if (rate.isNaN()) "N/A" else rate}")
                    }
                }
            }
        }

        // Save button handler (Gaur, 2025).
        btnSave.setOnClickListener {
            val newCode = pendingSelection ?: return@setOnClickListener
            lifecycleScope.launch(Dispatchers.IO) {
                val dao = AppDatabase.getDatabase(this@ChangeCurrency).userDao()
                val user = dao.getById(userId) ?: return@launch

                // Only update database if the currency changed (CodingStuff, 2024).
                if (user.currency != newCode) {
                    dao.update(user.copy(currency = newCode))
                    Log.d("ChangeCurrency", "Saved new currency: $newCode")
                    withContext(Dispatchers.Main) {
                        tvCurrent.text = newCode
                        Toast.makeText(this@ChangeCurrency, "Currency changed to $newCode", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@ChangeCurrency, "Currency unchanged", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    // Sets the correct nav icon to active and resets others (Main, 2025).
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
}
