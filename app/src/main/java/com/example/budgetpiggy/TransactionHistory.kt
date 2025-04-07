package com.example.budgetpiggy

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class TransactionHistory : AppCompatActivity() {

    private lateinit var transactionListLayout: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.transaction_history)

        transactionListLayout = findViewById(R.id.transactionList)

        // 🔹 Static sample data (no data class)
        val categories = listOf("Groceries", "Clothing", "Motor", "Charity", "Home", "Gift")
        val amounts = listOf("R5000", "R5000", "R500", "R1000", "R2000", "R5000")
        val accountTypes = listOf("Cheque", "Cheque", "Cheque", "Savings", "Cheque", "Cheque")
        val dates = List(categories.size) { "2025/03/13" } // same date for now
        val icons = listOf(
            R.drawable.vec_food_circle,
            R.drawable.vec_clothes_circle,
            R.drawable.vec_car,
            R.drawable.vec_donation_circle,
            R.drawable.vec_home_circle,
            R.drawable.vec_gift_circle
        )

        // 🔹 Loop through and inflate each row dynamically
        for (i in categories.indices) {
            val row = LayoutInflater.from(this).inflate(R.layout.transaction_item_row, transactionListLayout, false)

            val iconImage = row.findViewById<ImageView>(R.id.iconImage)
            val categoryText = row.findViewById<TextView>(R.id.categoryText)
            val accountText = row.findViewById<TextView>(R.id.accountText)
            val dateText = row.findViewById<TextView>(R.id.dateText)

            iconImage.setImageResource(icons[i])
            categoryText.text = "${categories[i]} ${amounts[i]}"
            accountText.text = "Account ${accountTypes[i]}"
            dateText.text = dates[i]

            transactionListLayout.addView(row)
        }
    }
}
