package com.example.budgetpiggy

import android.content.Context
import android.view.View
import android.widget.TextView
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

fun updateNotificationBadge(view: View, count: Int) {
    val badge = view.findViewById<TextView>(R.id.notificationBadge)
    if (count > 0) {
        badge.text = count.toString()
        badge.visibility = View.VISIBLE
    } else {
        badge.visibility = View.GONE
    }
}

// Object that handles tracking and updating the user's daily streak
object StreakTracker {
    private const val PREF_NAME = "streakPrefs"
    private const val LAST_ACTIVE_DATE = "lastActiveDate"
    private const val STREAK_COUNT = "streakCount"

    fun updateStreak(context: Context): Int {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

        val today = LocalDate.now()
        val formatter = DateTimeFormatter.ISO_DATE

        val lastDateString = prefs.getString(LAST_ACTIVE_DATE, null)
        val lastDate = lastDateString?.let { LocalDate.parse(it, formatter) }

        val streak = when {
            lastDate == null || ChronoUnit.DAYS.between(lastDate, today) > 1 -> 1
            ChronoUnit.DAYS.between(lastDate, today) == 1L -> prefs.getInt(STREAK_COUNT, 0) + 1
            else -> prefs.getInt(STREAK_COUNT, 1)
        }

        prefs.edit()
            .putString(LAST_ACTIVE_DATE, today.format(formatter))
            .putInt(STREAK_COUNT, streak)
            .apply()

        return streak
    }
}
