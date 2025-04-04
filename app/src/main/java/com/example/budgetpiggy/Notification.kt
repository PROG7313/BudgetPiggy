package com.example.budgetpiggy
import android.view.View

import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Notification : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.notification)
        findViewById<ImageView>(R.id.piggyIcon).visibility = View.GONE
        findViewById<TextView>(R.id.greetingText).visibility = View.GONE
        findViewById<ImageView>(R.id.streakIcon).visibility = View.GONE

        val pageTitle = findViewById<TextView>(R.id.pageTitle)
        pageTitle.visibility = View.VISIBLE
        pageTitle.text = getString(R.string.notifications_1)

        // Fixes system bars (safe area padding)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainLayout)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Safely access views
        val backArrow = findViewById<ImageView>(R.id.backArrow)
        val notifyList = findViewById<LinearLayout>(R.id.notificationList)

        // Go back on click
        backArrow.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        val scrollView = findViewById<ScrollView>(R.id.scrollArea)
        val fabWrapper = findViewById<View>(R.id.fabWrapper)

        var lastScrollY = 0

        scrollView.viewTreeObserver.addOnScrollChangedListener {
            val scrollY = scrollView.scrollY
            val isScrollingUp = scrollY < lastScrollY
            val isAtTop = !scrollView.canScrollVertically(-1)
            val isAtBottom = !scrollView.canScrollVertically(1)

            when {
                isScrollingUp || isAtTop || isAtBottom -> {
                    fabWrapper.visibility = View.VISIBLE
                    fabWrapper.animate().alpha(1f).setDuration(1).start()
                }

                else -> {
                    fabWrapper.animate()
                        .alpha(0f)
                        .setDuration(1)
                        .withEndAction { fabWrapper.visibility = View.GONE }
                        .start()
                }
            }

            lastScrollY = scrollY
        }

        // Sample data
        val sampleNotifs = listOf(
            "🎉 Well Done! You earned your sign-up Starsucks voucher.\nDiscount Code: STARSUCK111",
            "🎉 Well Done! You earned your sign-up Starsucks voucher.\nDiscount Code: STARSUCK111",
            "🎉 Well Done! You earned your sign-up Starsucks voucher.\nDiscount Code: STARSUCK111",
            "🎉 Well Done! You earned your sign-up Starsucks voucher.\nDiscount Code: STARSUCK111",
            "🎉 Well Done! You earned your sign-up Starsucks voucher.\nDiscount Code: STARSUCK111",
            "🎉 Well Done! You earned your sign-up Starsucks voucher.\nDiscount Code: STARSUCK111",
            "🎉 Well Done! You earned your sign-up Starsucks voucher.\nDiscount Code: STARSUCK111",
            "🎉 Well Done! You earned your sign-up Starsucks voucher.\nDiscount Code: STARSUCK111",
            "🎉 Well Done! You earned your sign-up Starsucks voucher.\nDiscount Code: STARSUCK111",
            "🎉 Well Done! You earned your sign-up Starsucks voucher.\nDiscount Code: STARSUCK111",
            "🎉 Well Done! You earned your sign-up Starsucks voucher.\nDiscount Code: STARSUCK111",
            "🎉 Well Done! You earned your sign-up Starsucks voucher.\nDiscount Code: STARSUCK111",
            "🎉 Well Done! You earned your sign-up Starsucks voucher.\nDiscount Code: STARSUCK111",
            "🎉 Well Done! You earned your sign-up Starsucks voucher.\nDiscount Code: STARSUCK111",

            "🚀 No more physical budgeting needed! Use CODE111 to earn your first Rewards Badge"
        )

        // Add notifications dynamically
        for (message in sampleNotifs) {
            val notifItem = layoutInflater.inflate(R.layout.item_notification_card, notifyList, false)
            val msgView = notifItem.findViewById<TextView>(R.id.notificationMessage)
            msgView.text = message
            notifyList.addView(notifItem)
        }
    }
}
