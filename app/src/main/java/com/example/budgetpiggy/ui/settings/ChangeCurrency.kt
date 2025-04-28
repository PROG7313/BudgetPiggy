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
import com.example.budgetpiggy.utils.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class ChangeCurrency : BaseActivity() {

    private lateinit var tvCurrent: TextView
    private lateinit var spinnerChoose: Spinner
    private lateinit var btnSave: Button

    // will hold the newly selected code until “Save”
    private var pendingSelection: String? = null

    // list of all fetched currency codes
    private var allCodes = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.change_currency)

        // hide unused top‐bar bits
        findViewById<ImageView>(R.id.piggyIcon).visibility = View.GONE
        findViewById<TextView>(R.id.greetingText).visibility = View.GONE
        findViewById<ImageView>(R.id.streakIcon).visibility = View.GONE

        // page title
        findViewById<TextView>(R.id.pageTitle).apply {
            visibility = View.VISIBLE
            text = getString(R.string.change_currency)
        }

        // edge-to-edge padding
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.currencyPage)) { v, insets ->
            val b = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(b.left, b.top, b.right, b.bottom)
            insets
        }

        // bottom nav
        findViewById<ImageView>(R.id.nav_home).setOnClickListener { v ->
            setActiveNavIcon(v as ImageView)
            startActivity(Intent(this, HomePage::class.java))
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

        // back & bell
        findViewById<ImageView>(R.id.backArrow)
            .setOnClickListener { onBackPressedDispatcher.onBackPressed() }
        findViewById<ImageView>(R.id.bellIcon)
            .setOnClickListener { startActivity(Intent(this, Notification::class.java)) }

        // bind views
        tvCurrent     = findViewById(R.id.tvCurrentCurrency)
        spinnerChoose = findViewById(R.id.spinnerChooseCurrency)
        btnSave       = findViewById(R.id.btnSaveCurrency)

        // load current user ID from prefs
        val userId = SessionManager.getUserId(this) ?: return
        if (userId == null) {
            tvCurrent.text = "—"
            Toast.makeText(this, "No logged-in user!", Toast.LENGTH_SHORT).show()
            return
        }

        // fetch live rates + user data
        lifecycleScope.launch {
            // 1) fetch rates JSON on IO dispatcher
            val json = withContext(Dispatchers.IO) {
                try {
                    val conn = (URL("https://api.exchangerate-api.com/v4/latest/USD")
                        .openConnection() as HttpURLConnection).apply {
                        requestMethod = "GET"
                        connectTimeout = 5_000
                        readTimeout = 5_000
                    }
                    BufferedReader(InputStreamReader(conn.inputStream)).use { it.readText() }
                        .also { conn.disconnect() }
                } catch (e: Exception) {
                    Log.e("ChangeCurrency", "Failed to fetch rates", e)
                    null
                }
            }

            if (json == null) {
                Toast.makeText(this@ChangeCurrency,
                    "Could not load currency rates", Toast.LENGTH_SHORT).show()
                return@launch
            }

            // 2) log full JSON for debugging
            Log.d("ChangeCurrency", "Rates JSON: $json")

            // 3) parse out currency codes
            val ratesObj = JSONObject(json).getJSONObject("rates")
            allCodes = ratesObj.keys().asSequence().toMutableList().apply { sort() }

            // 4) fetch user from Room
            val dao  = AppDatabase.getDatabase(this@ChangeCurrency).userDao()
            val user = dao.getById(userId) ?: return@launch

            withContext(Dispatchers.Main) {
                // show existing currency + its live rate
                val currentCode = user.currency
                val currentRate = ratesObj.optDouble(currentCode, Double.NaN)
                tvCurrent.text = if (currentRate.isNaN()) {
                    "$currentCode (rate unavailable)"
                } else {
                    "$currentCode = $currentRate"
                }

                // populate spinner
                spinnerChoose.adapter = ArrayAdapter(
                    this@ChangeCurrency,
                    android.R.layout.simple_spinner_dropdown_item,
                    allCodes
                )
                // select current
                val idx = allCodes.indexOf(currentCode).coerceAtLeast(0)
                spinnerChoose.setSelection(idx)
                pendingSelection = currentCode

                // listen for selection changes & log them
                spinnerChoose.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onNothingSelected(parent: AdapterView<*>?) { /* no-op */ }
                    override fun onItemSelected(
                        parent: AdapterView<*>, view: View?, position: Int, id: Long
                    ) {
                        pendingSelection = allCodes[position]
                        val rate = ratesObj.optDouble(pendingSelection, Double.NaN)
                        Log.d("ChangeCurrency",
                            "Selected $pendingSelection → rate ${if (rate.isNaN()) "N/A" else rate}")
                    }
                }
            }
        }

        // Save button: update Room if changed
        btnSave.setOnClickListener {
            val newCode = pendingSelection ?: return@setOnClickListener
            lifecycleScope.launch(Dispatchers.IO) {
                val dao  = AppDatabase.getDatabase(this@ChangeCurrency).userDao()
                val user = dao.getById(userId) ?: return@launch

                if (user.currency != newCode) {
                    dao.update(user.copy(currency = newCode))
                    Log.d("ChangeCurrency", "Saved new currency: $newCode")
                    withContext(Dispatchers.Main) {
                        tvCurrent.text = newCode
                        Toast.makeText(this@ChangeCurrency,
                            "Currency changed to $newCode", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@ChangeCurrency,
                            "Currency unchanged", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    override fun setActiveNavIcon(activeIcon: ImageView) {
        val navIcons = listOf(
            R.id.nav_home to R.drawable.vec_home_inactive,
            R.id.nav_wallet to R.drawable.vec_wallet_inactive,
            R.id.nav_reports to R.drawable.vec_reports_inactive,
            R.id.nav_profile to R.drawable.vec_profile_inactive
        )

        // reset all to inactive
        navIcons.forEach { (id, drawable) ->
            findViewById<ImageView>(id).setImageResource(drawable)
        }

        // set the selected one to active
        when (activeIcon.id) {
            R.id.nav_home -> activeIcon.setImageResource(R.drawable.vec_home_active)
            R.id.nav_wallet -> activeIcon.setImageResource(R.drawable.vec_wallet_active)
            R.id.nav_reports -> activeIcon.setImageResource(R.drawable.vec_reports_active)
            R.id.nav_profile -> activeIcon.setImageResource(R.drawable.vec_profile_active)
        }
    }
}
