package com.example.budgetpiggy

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

open class BaseActivity : AppCompatActivity() {


    protected open fun setActiveNavIcon(activeIcon: ImageView) {
        val navIcons = listOf(
            R.id.nav_home    to R.drawable.vec_home_inactive,
            R.id.nav_wallet  to R.drawable.vec_wallet_inactive,
            R.id.nav_reports to R.drawable.vec_reports_inactive,
            R.id.nav_profile to R.drawable.vec_profile_inactive
        )

        // Reset all to inactive state
        navIcons.forEach { (id, drawableRes) ->
            findViewById<ImageView>(id).setImageResource(drawableRes)
        }

        // Activate the selected one
        when (activeIcon.id) {
            R.id.nav_home    -> activeIcon.setImageResource(R.drawable.vec_home_active)
            R.id.nav_wallet  -> activeIcon.setImageResource(R.drawable.vec_wallet_active)
            R.id.nav_reports -> activeIcon.setImageResource(R.drawable.vec_reports_active)
            R.id.nav_profile -> activeIcon.setImageResource(R.drawable.vec_profile_active)
        }
    }


    fun updateNotificationBadgeGlobally(topBar: View?, count: Int) {
        val badge = topBar?.findViewById<TextView>(R.id.notificationBadge)
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
