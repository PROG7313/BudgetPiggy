package com.example.budgetpiggy.data.repository

import com.example.budgetpiggy.data.dao.NotificationDao
import com.example.budgetpiggy.data.dao.RewardCodeDao
import com.example.budgetpiggy.data.dao.RewardDao
import com.example.budgetpiggy.data.entities.NotificationEntity
import com.example.budgetpiggy.data.entities.RewardEntity
import kotlinx.coroutines.flow.flow
import java.util.UUID

class RewardRepository(
    private val rewardDao: RewardDao,
    private val codeDao:   RewardCodeDao,
    private val notifDao:  NotificationDao
) {
    fun allCodes() = flow { emit(codeDao.getAll()) }               // Flow<List<RewardCodeEntity>>
    suspend fun userRewards(userId: String) = rewardDao.getByUserId(userId) // suspend → List<RewardEntity>>

    suspend fun unlockCode(userId: String, code: String): Result<Unit> {
        val rc = codeDao.getByCode(code) ?: return Result.failure(Exception("Invalid code"))
        // already claimed?
        if ( rewardDao.getByUserId(userId).any { it.rewardId == rc.code } )
            return Result.failure(Exception("Already claimed"))

        // insert reward
        rewardDao.insert(
            RewardEntity(
                rewardId   = rc.code,
                userId     = userId,
                rewardName = rc.rewardName,
                unlockedAt = System.currentTimeMillis()
            )
        )

        // fire a notification for this new reward
        notifDao.insert(
            NotificationEntity(
                notificationId = UUID.randomUUID().toString(),
                userId         = userId,
                message        = "You unlocked “${rc.rewardName}”! 🎉",
                timestamp      = System.currentTimeMillis(),
                isRead         = false,
                iconUrl        = rc.rewardImageUrl,
                rewardCodeId   = rc.code
            )
        )

        return Result.success(Unit)
    }


}
