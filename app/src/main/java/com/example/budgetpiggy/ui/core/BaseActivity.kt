package com.example.budgetpiggy.ui.core

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.budgetpiggy.R
import com.example.budgetpiggy.data.database.AppDatabase
import com.example.budgetpiggy.data.repository.NotificationRepository
import com.example.budgetpiggy.utils.SessionManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

open class BaseActivity : AppCompatActivity() {

    // 1) Lazy-init your repo
    private val notificationRepo by lazy {
        NotificationRepository(
            AppDatabase.getDatabase(this).notificationDao()
        )
    }

    // 2) Every time your activity resumes, fetch the unread count & update the badge
    override fun onResume() {
        super.onResume()

        // get the logged-in user or bail out
        val uid = SessionManager.getUserId(this) ?: return

        lifecycleScope.launch {
            // pull the first (current) snapshot of notifications
            val list = notificationRepo.notificationsFor(uid).first()
            val unreadCount = list.count { !it.isRead }

            // find your top‐bar include and update its badge
            val topBar = findViewById<View>(R.id.topBar)
            updateNotificationBadgeGlobally(topBar, unreadCount)
        }
    }

    // your existing nav-icon helper
    protected open fun setActiveNavIcon(activeIcon: ImageView) {
        val navIcons = listOf(
            R.id.nav_home    to R.drawable.vec_home_inactive,
            R.id.nav_wallet  to R.drawable.vec_wallet_inactive,
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

    // your existing badge-updater
    fun updateNotificationBadgeGlobally(topBar: View?, count: Int) {
        val badge = topBar
            ?.findViewById<TextView>(R.id.notificationBadge)
        badge?.let {
            if (count > 0) {
                it.visibility = View.VISIBLE
                it.text = count.toString()
            } else {
                it.visibility = View.GONE
            }
        }
    }
}
