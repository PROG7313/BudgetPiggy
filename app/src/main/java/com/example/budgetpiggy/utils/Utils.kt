package com.example.budgetpiggy.utils

import android.content.Context
import android.view.View
import android.widget.TextView
import com.example.budgetpiggy.R
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import androidx.core.content.edit
import me.leolin.shortcutbadger.ShortcutBadger

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

        prefs.edit {
            putString(LAST_ACTIVE_DATE, today.format(formatter))
                .putInt(STREAK_COUNT, streak)
        }

        return streak
    }
}

object SessionManager {
    private const val PREFS_NAME  = "app_piggy_prefs"
    private const val KEY_USER_ID = "logged_in_user_id"


    fun saveUserId(context: Context, userId: String) {
        context
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit { putString(KEY_USER_ID, userId) }
    }

    /** Anywhere you need the current user ID. */
    fun getUserId(context: Context): String? =
        context
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_USER_ID, null)

    fun logout(context: Context) {
        context
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit { remove(KEY_USER_ID) }
    }
}

object BadgeManager {
    private const val PREF_NAME = "badgePrefs"
    private const val BADGE_COUNT_KEY = "badgeCount"

    fun incrementBadge(context: Context) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val newCount = prefs.getInt(BADGE_COUNT_KEY, 0) + 1
        prefs.edit().putInt(BADGE_COUNT_KEY, newCount).apply()
        ShortcutBadger.applyCount(context, newCount)
    }

    fun clearBadge(context: Context) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt(BADGE_COUNT_KEY, 0).apply()
        ShortcutBadger.removeCount(context)
    }
}
