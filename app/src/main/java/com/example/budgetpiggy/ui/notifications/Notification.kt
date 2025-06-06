package com.example.budgetpiggy.ui.notifications

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.budgetpiggy.R
import com.example.budgetpiggy.data.database.AppDatabase
import com.example.budgetpiggy.data.entities.NotificationEntity
import com.example.budgetpiggy.ui.core.BaseActivity
import com.example.budgetpiggy.ui.wallet.WalletPage
import com.example.budgetpiggy.utils.SessionManager
import kotlin.collections.forEach
import com.example.budgetpiggy.data.repository.NotificationRepository
import com.example.budgetpiggy.ui.home.HomePage
import com.example.budgetpiggy.ui.reports.ReportsPage
import com.example.budgetpiggy.ui.settings.AccountPage
import com.example.budgetpiggy.ui.transaction.TransactionActivity
import com.example.budgetpiggy.ui.transaction.TransferFunds


class Notification : BaseActivity() {

    // Obtain the ViewModel, injecting current userId (Android, 2025)
    private val viewModel: NotificationViewModel by viewModels {
        val uid = SessionManager.getUserId(this)
            ?: throw IllegalStateException("No user logged in!")
        NotificationViewModelFactory(
            NotificationRepository(
                AppDatabase.getDatabase(this).notificationDao()
            ),
            uid
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.notification)

        // Hide unused top‐bar elements
        findViewById<ImageView>(R.id.piggyIcon).visibility   = View.GONE
        findViewById<TextView>(R.id.greetingText).visibility = View.GONE
        findViewById<ImageView>(R.id.streakIcon).visibility  = View.GONE

        // Show the screen title
        findViewById<TextView>(R.id.pageTitle).apply {
            visibility = View.VISIBLE
            text       = getString(R.string.notifications_1)
        }

        // Handle window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.NotificationPage)) { v, insets ->
            val sysBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(sysBars.left, sysBars.top, sysBars.right, sysBars.bottom)
            insets
        }

        // View references
        val topBar     = findViewById<View>(R.id.topBar)
        val backArrow  = findViewById<ImageView>(R.id.backArrow)
        val notifyList = findViewById<LinearLayout>(R.id.notificationList)
        val scrollView = findViewById<ScrollView>(R.id.scrollArea)
        val fabWrapper = findViewById<View>(R.id.fabWrapper)
        val clearAll   = findViewById<TextView>(R.id.clear_All)

        // Navigation & actions
        findViewById<ImageView>(R.id.nav_home).setOnClickListener { v ->
            setActiveNavIcon(v as ImageView)
            startActivity(Intent(this, HomePage::class.java))
        }

        findViewById<ImageView>(R.id.backArrow).setOnClickListener { v ->
            v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(25)
                .withEndAction {
                    v.animate().scaleX(1f).scaleY(1f).setDuration(25).start()
                    onBackPressed()
                }.start()
        }
        findViewById<ImageView>(R.id.bellIcon).setOnClickListener {
            startActivity(Intent(this, Notification::class.java))
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

        // Bottom nav wiring (example for Wallet) (Android, 2025)
        findViewById<ImageView>(R.id.nav_wallet).setOnClickListener { view ->
            setActiveNavIcon(view as ImageView)
            view.animate().scaleX(0.95f).scaleY(0.95f).setDuration(25).withEndAction {
                view.animate().scaleX(1f).scaleY(1f).setDuration(25).start()
                startActivity(Intent(this, WalletPage::class.java))
            }.start()
        }

        // FAB show/hide on scroll
        setupFabScrollBehavior(scrollView, fabWrapper)
        findViewById<ImageView>(R.id.fabPlus)?.setOnClickListener {
            startActivity(Intent(this, TransactionActivity::class.java))
        }
        // Initialize badge to zero
        updateNotificationBadgeGlobally(topBar, 0)

        // Observe real notifications from ViewModel
        viewModel.notifications.observe(this) { list ->
            // Clear out old cards
            notifyList.removeAllViews()

            //  Inflate a card for each notification
            list.forEach { notif: NotificationEntity ->
                val card = layoutInflater.inflate(
                    R.layout.item_notification_card,
                    notifyList,
                    false
                )

                //  look up the icon view as an ImageView
                val iconView = card.findViewById<ImageView>(R.id.notificationIcon)

                //  load using the real property, notif.iconUrl
                Glide.with(this)
                    .load(notif.iconUrl)
                    .placeholder(R.drawable.pic_piggy_money)
                    .into(iconView)


                //  Message text
                card.findViewById<TextView>(R.id.notificationMessage).text = notif.message

                //  Reward code if present (Android, 2025)
                val codeView = card.findViewById<TextView>(R.id.notificationCode)
                if (notif.rewardCodeId != null) {
                    codeView.apply {
                        text       = notif.rewardCodeId
                        visibility = View.VISIBLE
                        setOnClickListener {
                            // Copy to clipboard
                            val cm = getSystemService(CLIPBOARD_SERVICE)
                                    as ClipboardManager
                            cm.setPrimaryClip(
                                ClipData.newPlainText(
                                    "RewardCode",
                                    notif.rewardCodeId
                                )
                            )
                            Toast.makeText(
                                this@Notification,
                                "Code copied!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } else {
                    codeView.visibility = View.GONE
                }

                //  Dim if already read
                card.alpha = if (notif.isRead) 0.6f else 1f

                //  Mark-as-read on tap
                card.setOnClickListener {
                    viewModel.markAsRead(notif.notificationId)
                }

                notifyList.addView(card)
            }

            //  Update top-bar badge (show count of unread)
            val unreadCount = list.count { !it.isRead }
            updateNotificationBadgeGlobally(topBar, unreadCount)
        }

        // Clear all button
        clearAll.setOnClickListener {
            viewModel.clearAll()
        }
    }

    // Updates the icon tint based on active section
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

    // Clear nav icon state on resume to reflect current session
    override fun onResume() {
        super.onResume()
        clearNavIcons()

        com.example.budgetpiggy.utils.BadgeManager.clearBadge(this)
    }
}