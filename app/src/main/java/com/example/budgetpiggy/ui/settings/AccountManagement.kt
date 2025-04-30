package com.example.budgetpiggy.ui.settings

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.budgetpiggy.R
import com.example.budgetpiggy.data.dao.UserDao
import com.example.budgetpiggy.data.database.AppDatabase
import com.example.budgetpiggy.data.entities.UserEntity
import com.example.budgetpiggy.ui.auth.LoginPage
import com.example.budgetpiggy.ui.core.BaseActivity
import com.example.budgetpiggy.ui.home.HomePage
import com.example.budgetpiggy.ui.notifications.Notification
import com.example.budgetpiggy.ui.reports.ReportsPage
import com.example.budgetpiggy.ui.wallet.WalletPage
import com.example.budgetpiggy.utils.PasswordUtils
import com.example.budgetpiggy.utils.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import androidx.core.content.edit

class AccountManagement : BaseActivity() {

    private val REQ_CAM = 3001
    private val REQ_GAL = 3002

    private var tempPhotoUri: Uri? = null
    private var profileImagePath: String? = null

    private lateinit var profileImage: ImageView
    private lateinit var firstNameEditText: EditText
    private lateinit var lastNameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var saveButton: Button
    private lateinit var deleteButton: Button

    private val userDao: UserDao by lazy { AppDatabase.getDatabase(this).userDao() }
    private lateinit var currentUser: UserEntity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.account_management)

        // Apply system bar insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.accountManagementPage)) { v, insets ->
            val sb = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(sb.left, sb.top, sb.right, sb.bottom)
            insets
        }

        // Hide unused header icons
        findViewById<ImageView>(R.id.piggyIcon)?.visibility   = View.GONE
        findViewById<ImageView>(R.id.streakIcon)?.visibility  = View.GONE
        findViewById<TextView>(R.id.greetingText)?.visibility = View.GONE

        // Page title
        findViewById<TextView>(R.id.pageTitle).apply {
            visibility = View.VISIBLE
            text = getString(R.string.edit_account)
        }

        // Back arrow
        findViewById<ImageView>(R.id.backArrow).setOnClickListener { v ->
            v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(25)
                .withEndAction {
                    v.animate().scaleX(1f).scaleY(1f).setDuration(25).start()
                    onBackPressed()
                }.start()
        }

        // Bell → Notifications
        findViewById<ImageView>(R.id.bellIcon).setOnClickListener {
            startActivity(Intent(this, Notification::class.java))
        }

        // Bottom nav
        findViewById<ImageView>(R.id.nav_home).setOnClickListener { v ->
            setActiveNavIcon(v as ImageView)
            v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(25)
                .withEndAction {
                    v.animate().scaleX(1f).scaleY(1f).setDuration(25).start()
                    startActivity(Intent(this, HomePage::class.java))
                }.start()
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

        // Bind views
        profileImage     = findViewById(R.id.profileImage)
        firstNameEditText= findViewById(R.id.firstNameEditText)
        lastNameEditText = findViewById(R.id.lastNameEditText)
        emailEditText    = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        saveButton       = findViewById(R.id.saveButton)
        deleteButton     = findViewById(R.id.deleteAccountButton)

        //  Load current user (CodingStuff, 2024).
        val userId = SessionManager.getUserId(this) ?: return
        lifecycleScope.launch {
            currentUser = withContext(Dispatchers.IO) {
                userDao.getById(userId)!!
            }

            // Populate text fields
            firstNameEditText.setText(currentUser.firstName)
            lastNameEditText.setText(currentUser.lastName)
            emailEditText.setText(currentUser.email)
            passwordEditText.hint = "••••••••••"

            // Populate profile picture if stored locally
            currentUser.profilePictureLocalPath?.let { path ->
                profileImage.setImageURI(path.toUri())
                profileImagePath = path
            }
        }

        //  Tapping the circle → choose or take photo
        profileImage.setOnClickListener { showProfileImageChooser() }

        //  Save updates
        saveButton.setOnClickListener {
            val newFirst = firstNameEditText.text.toString().trim()
            val newLast  = lastNameEditText.text.toString().trim()
            val newEmail = emailEditText.text.toString().trim()
            val newPwd   = passwordEditText.text.toString()

            // Only re-hash if user entered something
            val newHash = if (newPwd.isNotEmpty()) {
                PasswordUtils.hashPassword(newPwd)
            } else {
                currentUser.passwordHash
            }

            val updated = currentUser.copy(
                firstName                = newFirst,
                lastName                 = newLast,
                email                    = newEmail,
                passwordHash             = newHash,
                profilePictureLocalPath  = profileImagePath
            )

            lifecycleScope.launch(Dispatchers.IO) {
                userDao.update(updated)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@AccountManagement,
                        "Profile saved", Toast.LENGTH_SHORT).show()
                    currentUser = updated
                    passwordEditText.setText("")
                }
            }
        }

        //  Delete account (Android, 2025)
        deleteButton.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Delete account?")
                .setMessage("This action cannot be undone. Are you sure?")
                .setPositiveButton("Delete") { _, _ ->
                    lifecycleScope.launch(Dispatchers.IO) {
                        //  remove user from database
                        userDao.delete(currentUser)

                        //  clear all saved prefs so login state is reset
                        val prefs = getSharedPreferences("app_piggy_prefs", Context.MODE_PRIVATE)
                        prefs.edit { clear() }

                        //  return to login (or simply close everything)
                        withContext(Dispatchers.Main) {
                            startActivity(Intent(this@AccountManagement, LoginPage::class.java))
                            finishAffinity()
                        }
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

    }

    // Show camera/gallery chooser
    private fun showProfileImageChooser() {
        val opts = arrayOf("Take Photo", "Choose from Gallery")
        AlertDialog.Builder(this)
            .setTitle("Change Profile Picture")
            .setItems(opts) { _, which ->
                if (which == 0) {
                    // Camera
                    val tmpFile = File.createTempFile("profile_", ".jpg", cacheDir)
                    tempPhotoUri = FileProvider.getUriForFile(
                        this,
                        "$packageName.fileprovider",
                        tmpFile
                    )
                    val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                        .putExtra(MediaStore.EXTRA_OUTPUT, tempPhotoUri)
                    packageManager.queryIntentActivities(intent, 0).forEach {
                        grantUriPermission(
                            it.activityInfo.packageName,
                            tempPhotoUri!!,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION
                        )
                    }
                    startActivityForResult(intent, REQ_CAM)
                } else {
                    // Gallery
                    val pick = Intent(
                        Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    )
                    startActivityForResult(pick, REQ_GAL)
                }
            }
            .show()
    }

    // Handle camera/gallery result
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK) return

        when (requestCode) {
            REQ_CAM -> {
                profileImagePath = tempPhotoUri?.toString()
                profileImage.setImageURI(tempPhotoUri)
            }
            REQ_GAL -> {
                val uri = data?.data ?: return
                contentResolver.openInputStream(uri)?.use { input ->
                    val tmp = File.createTempFile("profile_gal_", ".jpg", cacheDir)
                    tmp.outputStream().use { output -> input.copyTo(output) }
                    profileImagePath = Uri.fromFile(tmp).toString()
                    profileImage.setImageURI(Uri.fromFile(tmp))
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        setActiveNavIcon(findViewById(R.id.nav_profile))
    }

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
            R.id.nav_home    -> activeIcon.setImageResource(R.drawable.vec_home_active)
            R.id.nav_wallet  -> activeIcon.setImageResource(R.drawable.vec_wallet_active)
            R.id.nav_reports -> activeIcon.setImageResource(R.drawable.vec_reports_active)
            R.id.nav_profile -> activeIcon.setImageResource(R.drawable.vec_profile_active)
        }
    }
}