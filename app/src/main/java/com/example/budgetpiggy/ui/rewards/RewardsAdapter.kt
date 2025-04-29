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
        val icon: ImageView = v.findViewById(R.id.rewardIcon)
        val name: TextView  = v.findViewById(R.id.rewardName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(LayoutInflater.from(parent.context)
            .inflate(R.layout.item_reward, parent, false))

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val (code, unlocked) = items[position]
        holder.name.text = code.rewardName

        Glide.with(holder.icon)
            .load(code.rewardImageLocalPath ?: code.rewardImageUrl)
            .placeholder(R.drawable.pic_piggy_money)
            .into(holder.icon)

        val alpha = if (unlocked) 1f else 0.3f
        holder.icon.alpha = alpha
        holder.name.alpha = alpha
    }
}
