package com.example.budgetpiggy

import android.view.View
import android.widget.TextView

fun updateNotificationBadge(view: View, count: Int) {
    val badge = view.findViewById<TextView>(R.id.notificationBadge)
    if (count > 0) {
        badge.text = count.toString()
        badge.visibility = View.VISIBLE
    } else {
        badge.visibility = View.GONE
    }
}
