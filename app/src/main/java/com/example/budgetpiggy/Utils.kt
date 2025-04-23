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
    // Constants used as keys for SharedPreferences
    private const val PREF_NAME = "streakPrefs"              // Name of the SharedPreferences file
    private const val LAST_ACTIVE_DATE = "lastActiveDate"    // Key for storing last activity date
    private const val STREAK_COUNT = "streakCount"           // Key for storing current streak count

    // Function to update and return the current streak count
    fun updateStreak(context: Context): Int {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

        // Get today's date
        val today = LocalDate.now()
        val formatter = DateTimeFormatter.ISO_DATE // Format used for saving/loading dates

        // Retrieve the last active date as a string from SharedPreferences
        val lastDateString = prefs.getString(LAST_ACTIVE_DATE, null)
        val lastDate = lastDateString?.let { LocalDate.parse(it, formatter) } // Convert to LocalDate

        // Determine the new streak value based on date difference
        val streak = when {
            lastDate == null || ChronoUnit.DAYS.between(lastDate, today) > 1 -> {
                // Case 1: No previous date or more than 1 day has passed – reset streak
                1
            }
            ChronoUnit.DAYS.between(lastDate, today) == 1L -> {
                // Case 2: Exactly 1 day passed – increment streak
                prefs.getInt(STREAK_COUNT, 0) + 1
            }
            else -> {
                // Case 3: Same day or already updated today – return current streak without increment
                prefs.getInt(STREAK_COUNT, 1)
            }
        }

        // Save the updated streak and today's date back into SharedPreferences
        prefs.edit()
            .putString(LAST_ACTIVE_DATE, today.format(formatter))
            .putInt(STREAK_COUNT, streak)
            .apply()

        // Return the updated streak value
        return streak
    }
}
