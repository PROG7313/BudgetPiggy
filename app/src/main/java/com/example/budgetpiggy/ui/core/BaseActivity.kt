package com.example.budgetpiggy.ui.core

import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.ScrollView
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

    //  Lazy-init your repo
    private val notificationRepo by lazy {
        NotificationRepository(
            AppDatabase.getDatabase(this).notificationDao()
        )
    }

    //  Every time the activity resumes, fetch the unread count & update the badge
    override fun onResume() {
        super.onResume()

        // Clear the app icon badge (e.g., on the launcher)
        com.example.budgetpiggy.utils.BadgeManager.clearBadge(this)

        val uid = SessionManager.getUserId(this) ?: return

        lifecycleScope.launch {
            val list = notificationRepo.notificationsFor(uid).first()
            val unreadCount = list.count { !it.isRead }

            val topBar = findViewById<View>(R.id.topBar)
            updateNotificationBadgeGlobally(topBar, unreadCount)
        }
    }

    protected fun setupFabScrollBehavior(scrollView: ScrollView, fabWrapper: View) {
        var lastY = 0
        scrollView.viewTreeObserver.addOnScrollChangedListener {
            val y = scrollView.scrollY
            val show = y < lastY || !scrollView.canScrollVertically(-1) || !scrollView.canScrollVertically(1)
            if (show) {
                fabWrapper.visibility = View.VISIBLE
                fabWrapper.alpha = 1f
            } else {
                fabWrapper.animate().alpha(0f).setDuration(150)
                    .withEndAction { fabWrapper.visibility = View.GONE }
                    .start()
            }
            lastY = y
        }
    }

    protected fun clearNavIcons() {
        val navIcons = listOf(
            R.id.nav_home to R.drawable.vec_home_inactive,
            R.id.nav_wallet to R.drawable.vec_wallet_inactive,
            R.id.nav_reports to R.drawable.vec_reports_inactive,
            R.id.nav_profile to R.drawable.vec_profile_inactive
        )
        navIcons.forEach { (id, drawable) ->
            findViewById<ImageView>(id)?.setImageResource(drawable)
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (currentFocus != null) {
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            if (imm.isAcceptingText) {
                val v = currentFocus
                if (v is EditText) {
                    val outLocation = IntArray(2)
                    v.getLocationOnScreen(outLocation)
                    val x = ev.rawX + v.left - outLocation[0]
                    val y = ev.rawY + v.top - outLocation[1]
                    if (x < v.left || x > v.right || y < v.top || y > v.bottom) {
                        v.clearFocus()
                        imm.hideSoftInputFromWindow(v.windowToken, 0)
                    }
                }
            }
        }
        return super.dispatchTouchEvent(ev)
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

    //  existing badge-updater
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
