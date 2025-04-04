package com.example.budgetpiggy

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView

class TransactionHistory :  AppCompatActivity(){
    private lateinit var recyclerTransactions: RecyclerView
    private lateinit var btnSort: ImageButton
    private lateinit var btnFilter: ImageButton
    private lateinit var btnBack: ImageButton
    private lateinit var btnNotifications: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.transaction_history)

        // Initialize UI components
        recyclerTransactions = findViewById(R.id.recyclerTransactions)
        btnSort = findViewById(R.id.btnSort)
        btnFilter = findViewById(R.id.btnFilter)
        btnBack = findViewById(R.id.btnBack)
        btnNotifications = findViewById(R.id.btnNotifications)

        // Set up RecyclerView
        recyclerTransactions.layoutManager = LinearLayoutManager(this)
        val transactionList = getDummyTransactions()
        recyclerTransactions.adapter = TransactionAdapter(transactionList)

        // Set button actions
        btnBack.setOnClickListener { finish() }
        btnNotifications.setOnClickListener { showNotifications() }

        // Correct the OnClickListener for btnSort and btnFilter
        btnSort.setOnClickListener {
            sortTransactions()
        }

        btnFilter.setOnClickListener {
            filterTransactions()
        }

    }

    private fun getDummyTransactions(): List<TransactionEntity> {
        return listOf(
            TransactionEntity("1", "User1", "Account1", "Groceries", 5000.0, "Baby City", 1731241600000, "", ""),
            TransactionEntity("2", "User1", "Account1", "Clothing", 5000.0, "Mall Purchase", 1731241600000, "", ""),
            TransactionEntity("3", "User1", "Account1", "Motor", 5000.0, "Car Service", 1731241600000, "", "")
        )
    }

    private fun showNotifications() {
        // Handle notifications click
    }

    private fun sortTransactions() {
        // Handle sorting logic
    }

    private fun filterTransactions() {
        // Handle filtering logic
    }
}

data class TransactionEntity(
    val transactionId: String,
    val userId: String,
    val accountId: String,
    val categoryId: String?,
    val amount: Double,
    val description: String?,
    val date: Long,
    val receiptImageUrl: String?,
    val receiptLocalPath: String?
)

class TransactionAdapter(private val transactions: List<TransactionEntity>) :
    RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    inner class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtAmount: TextView = itemView.findViewById(R.id.txtAmount)
        val txtDate: TextView = itemView.findViewById(R.id.txtDate)
        val txtDescription: TextView = itemView.findViewById(R.id.txtDescription)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.transaction_item, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val transaction = transactions[position]
        holder.txtAmount.text = "R${transaction.amount}"
        holder.txtDate.text = java.text.SimpleDateFormat("dd MMM yyyy")
            .format(java.util.Date(transaction.date))
        holder.txtDescription.text = transaction.description ?: "No description"
    }

    override fun getItemCount(): Int = transactions.size
}