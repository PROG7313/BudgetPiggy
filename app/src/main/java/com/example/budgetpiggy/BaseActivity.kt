package com.example.budgetpiggy

import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

open class BaseActivity : AppCompatActivity() {
    fun updateNotificationBadgeGlobally(topBar: View?, count: Int) {
        val badge = topBar?.findViewById<TextView>(R.id.notificationBadge)
        if (badge != null) {
            if (count > 0) {
                badge.visibility = View.VISIBLE
                badge.text = count.toString()
            } else {
                badge.visibility = View.GONE
            }
        }
    }
}
