package com.example.budgetpiggy.ui.category

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.children
import androidx.lifecycle.lifecycleScope
import com.example.budgetpiggy.ui.core.BaseActivity
import com.example.budgetpiggy.ui.home.HomePage
import com.example.budgetpiggy.ui.notifications.Notification
import com.example.budgetpiggy.R
import com.example.budgetpiggy.ui.wallet.WalletPage
import com.example.budgetpiggy.data.database.AppDatabase
import com.example.budgetpiggy.data.entities.CategoryEntity
import com.example.budgetpiggy.ui.reports.ReportsPage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class AddCategoryPage : BaseActivity() {
    private val REQUEST_ICON = 2001

    // either a built-in drawable name, or a stored URI
    private var selectedBuiltInIcon: String? = null
    private var selectedIconUri: Uri?        = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.addcategory)

        // hide home-only UI
        listOf(R.id.piggyIcon, R.id.greetingText, R.id.streakIcon)
            .forEach { findViewById<View>(it)?.visibility = View.GONE }

        // title + insets
        findViewById<TextView>(R.id.pageTitle).apply {
            visibility = View.VISIBLE
            text       = getString(R.string.add_category)
        }
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.addCategoryPage)) { v, insets ->
            val b = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(b.left, b.top, b.right, b.bottom)
            insets
        }

        // nav & back & bell
        findViewById<ImageView>(R.id.nav_home)?.setOnClickListener {
            setActiveNavIcon(it as ImageView)
            startActivity(Intent(this, HomePage::class.java))
        }
        findViewById<ImageView>(R.id.nav_wallet)?.setOnClickListener {
            setActiveNavIcon(it as ImageView)
            startActivity(Intent(this, WalletPage::class.java))
        }
        findViewById<ImageView>(R.id.nav_reports)?.setOnClickListener {
            setActiveNavIcon(it as ImageView)
            startActivity(Intent(this, ReportsPage::class.java))
        }
        findViewById<ImageView>(R.id.nav_profile)?.setOnClickListener {
            setActiveNavIcon(it as ImageView)
        }
        findViewById<ImageView>(R.id.backArrow)?.setOnClickListener { v ->
            v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(25)
                .withEndAction {
                    v.animate().scaleX(1f).scaleY(1f).setDuration(25).start()
                    onBackPressedDispatcher.onBackPressed()
                }.start()
        }
        findViewById<ImageView>(R.id.bellIcon)?.setOnClickListener {
            startActivity(Intent(this, Notification::class.java))
        }

        // form fields
        val nameInput       = findViewById<EditText>(R.id.categoryNameInput)
        val amountInput     = findViewById<EditText>(R.id.amountInput)
        val typeInput       = findViewById<EditText>(R.id.categoryTypeInput)
        val accountSpinner  = findViewById<Spinner>(R.id.spinnerLinkAccount)
        val builtInContainer= findViewById<LinearLayout>(R.id.builtInIconContainer)
        val btnAddIcon      = findViewById<ImageButton>(R.id.btnAddIcon)
        val btnConfirm      = findViewById<Button>(R.id.btnConfirm)

        // focus keyboard on amount
        amountInput.setOnFocusChangeListener { v, has ->
            if (has) (getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager)
                .showSoftInput(v, InputMethodManager.SHOW_IMPLICIT)
        }

        // load accounts
        val prefs  = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val userId = prefs.getString("logged_in_user_id", null) ?: run {
            Toast.makeText(this,"No logged-in user!",Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        lifecycleScope.launch {
            val names = withContext(Dispatchers.IO) {
                AppDatabase.getDatabase(this@AddCategoryPage)
                    .accountDao().getByUserId(userId).map { it.accountName }
            }
            accountSpinner.adapter = ArrayAdapter(
                this@AddCategoryPage,
                android.R.layout.simple_spinner_dropdown_item,
                names
            )
        }

        // built-in icon choices
        val builtin = listOf("vec_home","vec_food","vec_car","vec_gift","vec_clothes")
        builtin.forEach { iconName ->
            val resId = resources.getIdentifier(iconName, "drawable", packageName)
            ImageView(this).apply {
                setImageResource(resId)
                val pad = (8 * resources.displayMetrics.density).toInt()
                setPadding(pad,pad,pad,pad)
                alpha = 0.5f
                setOnClickListener {
                    selectedIconUri = null
                    builtInContainer.children.forEach { it.alpha = 0.5f }
                    alpha = 1f
                    selectedBuiltInIcon = iconName
                }
                builtInContainer.addView(this)
            }
        }
        // default-select first
        builtInContainer.children.firstOrNull()?.performClick()

        // upload override
        btnAddIcon.setOnClickListener {
            selectedBuiltInIcon = null
            builtInContainer.children.forEach { it.alpha = 0.5f }
            startActivityForResult(
                Intent(Intent.ACTION_GET_CONTENT).apply { type = "image/*" },
                REQUEST_ICON
            )
        }

        // confirm → Room insert
        btnConfirm.setOnClickListener {
            val name    = nameInput.text.toString().trim()
            val budget  = amountInput.text.toString().toDoubleOrNull() ?: 0.0
            val categoryType = typeInput.text.toString().trim()
            val linkAcc = accountSpinner.selectedItem as? String ?: ""

            if (name.isEmpty() || categoryType.isEmpty()) {
                Toast.makeText(this,"Fill name & type",Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val iconLocal = selectedIconUri?.toString()
            val iconName  = if (iconLocal == null) selectedBuiltInIcon else null

            val cat = CategoryEntity(
                categoryId        = UUID.randomUUID().toString(),
                userId            = userId,
                categoryName      = name,
                linkedAccountType = linkAcc,
                budgetAmount      = budget,
                iconName          = iconName,
                iconUrl           = null,
                iconLocalPath     = iconLocal,
                createdAt         = System.currentTimeMillis()
            )

            lifecycleScope.launch(Dispatchers.IO) {
                AppDatabase.getDatabase(this@AddCategoryPage)
                    .categoryDao().insert(cat)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@AddCategoryPage,
                        "Category added!", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_ICON && resultCode == Activity.RESULT_OK) {
            data?.data?.let { sourceUri ->
                val input = contentResolver.openInputStream(sourceUri)!!
                val destFile = File(filesDir, "cat_icon_${UUID.randomUUID()}.png")
                FileOutputStream(destFile).use { out ->
                    input.copyTo(out)
                }

                // <— LOG THE PATH HERE
                Log.d("AddCategory", "sourceUri=$sourceUri")
                Log.d("AddCategory", "wrote icon to: ${destFile.absolutePath}")

                selectedIconUri = Uri.fromFile(destFile)
                findViewById<ImageButton>(R.id.btnAddIcon).setImageURI(selectedIconUri)
            }
        }
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
