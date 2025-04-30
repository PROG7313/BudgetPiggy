package com.example.budgetpiggy.ui.category

import android.app.Activity
import androidx.core.content.edit
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
import com.example.budgetpiggy.data.entities.NotificationEntity
import com.example.budgetpiggy.data.repository.RewardRepository
import com.example.budgetpiggy.ui.reports.ReportsPage
import com.example.budgetpiggy.utils.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import androidx.core.graphics.scale
import androidx.core.graphics.createBitmap

class AddCategoryPage : BaseActivity() {
    private val REQUEST_ICON = 2001

    // Either a built-in drawable name, or a stored URI
    private var selectedBuiltInIcon: String? = null
    private var selectedIconUri: Uri?        = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.addcategory)

        // Hide home-only UI
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

        // Nav, Back & Bell handlers
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

        // Form fields
        val nameInput       = findViewById<EditText>(R.id.categoryNameInput)
        val amountInput     = findViewById<EditText>(R.id.amountInput)
        val typeInput       = findViewById<EditText>(R.id.categoryTypeInput)
        val accountSpinner  = findViewById<Spinner>(R.id.spinnerLinkAccount)
        val builtInContainer= findViewById<LinearLayout>(R.id.builtInIconContainer)
        val btnAddIcon      = findViewById<ImageButton>(R.id.btnAddIcon)
        val btnConfirm      = findViewById<Button>(R.id.btnConfirm)

        // Focus keyboard on amount
        amountInput.setOnFocusChangeListener { v, has ->
            if (has) (getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager)
                .showSoftInput(v, InputMethodManager.SHOW_IMPLICIT)
        }

        // Fetches logged-in user ID using session manager. Aborts if no session exists (GeeksforGeeks, 2022)
        val userId = SessionManager.getUserId(this) ?: run {
            Toast.makeText(this,"No logged-in user!",Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        lifecycleScope.launch {
            val names = withContext(Dispatchers.IO) {
                AppDatabase.getDatabase(this@AddCategoryPage)
                    .accountDao().getByUserId(userId).map { it.accountName }
            }

            // Dynamically loads linked user accounts for dropdown selection
            accountSpinner.adapter = ArrayAdapter(
                this@AddCategoryPage,
                android.R.layout.simple_spinner_dropdown_item,
                names
            )
        }

        // Loads built-in drawable icons
        val builtin = listOf("vec_home", "vec_food", "vec_car", "vec_gift", "vec_clothes")
        builtin.forEach { iconName ->
            val resId = resources.getIdentifier(iconName, "drawable", packageName)
            ImageView(this).apply {
                setImageResource(resId)
                val pad = (8 * resources.displayMetrics.density).toInt()
                setPadding(pad, pad, pad, pad)
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

        // Load any saved user-uploaded icons (Android, 2025)
        val userIcons = filesDir.listFiles()?.filter { it.name.startsWith("cat_icon_") && it.name.endsWith(".png") } ?: emptyList()

        userIcons.forEach { file ->
            ImageView(this).apply {
                setImageURI(Uri.fromFile(file))
                val pad = (8 * resources.displayMetrics.density).toInt()
                setPadding(pad, pad, pad, pad)
                alpha = 0.5f
                setOnClickListener {
                    selectedBuiltInIcon = null
                    builtInContainer.children.forEach { it.alpha = 0.5f }
                    alpha = 1f
                    selectedIconUri = Uri.fromFile(file)
                }
                builtInContainer.addView(this)
            }
        }

        // Default-select first item
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
                val db = AppDatabase.getDatabase(this@AddCategoryPage)

                // Insert category
                db.categoryDao().insert(cat)

                // Try unlock reward (built-in method handles checks + notification)
                val rewardRepo = RewardRepository(
                    rewardDao = db.rewardDao(),
                    codeDao   = db.rewardCodeDao(),
                    notifDao  = db.notificationDao()
                )
                rewardRepo.unlockCode(userId, "FIRSTCAT2025")

                // Finish on UI thread
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@AddCategoryPage, "Category added!", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }

        }
            }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_ICON && resultCode == Activity.RESULT_OK) {
            data?.data?.let { sourceUri ->
                val inputStream = contentResolver.openInputStream(sourceUri) ?: return

                // Decode the original image into a Bitmap (Android, 2025)
                val originalBitmap = android.graphics.BitmapFactory.decodeStream(inputStream)

                // Resize it to 48dp x 48dp
                val sizeInPx = (48 * resources.displayMetrics.density).toInt()
                val resizedBitmap = originalBitmap.scale(sizeInPx, sizeInPx)

                val output = createBitmap(sizeInPx, sizeInPx)
                val canvas = android.graphics.Canvas(output)
                val paint = android.graphics.Paint().apply {
                    isAntiAlias = true
                }
                val rect = android.graphics.Rect(0, 0, sizeInPx, sizeInPx)
                val rectF = android.graphics.RectF(rect)
                canvas.drawOval(rectF, paint)
                paint.xfermode = android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.SRC_IN)
                canvas.drawBitmap(resizedBitmap, rect, rect, paint)

                // Save to file
                val destFile = File(filesDir, "cat_icon_${UUID.randomUUID()}.png")
                FileOutputStream(destFile).use { out ->
                    output.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, out)
                }

                // Update UI
                selectedIconUri = Uri.fromFile(destFile)
                findViewById<ImageButton>(R.id.btnAddIcon).setImageURI(selectedIconUri)

                Log.d("AddCategory", "sourceUri=$sourceUri")
                Log.d("AddCategory", "saved resized circle icon to: ${destFile.absolutePath}")
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
