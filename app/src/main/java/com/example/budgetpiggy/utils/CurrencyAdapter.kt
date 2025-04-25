package com.example.budgetpiggy.utils

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.budgetpiggy.R

/**
 * Simple list adapter: displays a list of currency codes,
 * highlights selection on click via the provided callback.
 */
class CurrencyAdapter(
    private val items: List<String>,
    private val onClick: (String) -> Unit
) : RecyclerView.Adapter<CurrencyAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvCode: TextView = view.findViewById(R.id.tvCurrencyCode)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_currency, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val code = items[position]
        holder.tvCode.text = code

        holder.itemView.setOnClickListener {
            onClick(code)
        }
    }

    override fun getItemCount(): Int = items.size
}
