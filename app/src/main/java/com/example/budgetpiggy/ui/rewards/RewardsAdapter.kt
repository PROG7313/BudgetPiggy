package com.example.budgetpiggy.ui.rewards

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.budgetpiggy.R
import com.example.budgetpiggy.data.entities.RewardCodeEntity

class RewardsAdapter(
    private val items: List<Pair<RewardCodeEntity, Boolean>>
) : RecyclerView.Adapter<RewardsAdapter.VH>() {

    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        val icon = v.findViewById<ImageView>(R.id.rewardIcon)
        val name = v.findViewById<TextView>(R.id.rewardName)
    }

    override fun onCreateViewHolder(p: ViewGroup, t: Int) =
        VH(LayoutInflater.from(p.context).inflate(R.layout.item_reward, p, false))

    override fun getItemCount() = items.size

    override fun onBindViewHolder(h: VH, i: Int) {
        val (code, _) = items[i]
        h.name.text = code.rewardName
        Glide.with(h.icon.context)
            .load(code.rewardImageUrl)
            .into(h.icon)

        h.icon.alpha = 1f
        h.name.alpha = 1f
    }
}
