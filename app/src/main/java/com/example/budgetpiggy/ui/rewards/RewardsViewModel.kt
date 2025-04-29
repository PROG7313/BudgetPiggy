package com.example.budgetpiggy.ui.rewards

import androidx.lifecycle.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import com.example.budgetpiggy.data.entities.RewardCodeEntity
import com.example.budgetpiggy.data.entities.RewardEntity
import com.example.budgetpiggy.data.repository.RewardRepository

class RewardsViewModel(
    private val repo:   RewardRepository,
    private val userId: String
) : ViewModel() {
    // combine all codes + which are unlocked
    val rewards = liveData {
        val codes    = repo.allCodes().first()          // List<RewardCodeEntity>
        val unlocked = repo.userRewards(userId)         // List<RewardEntity>
        val unlockedSet = unlocked.map { it.rewardId }.toSet()
        emit( codes.map { it to unlockedSet.contains(it.code) } )
    }

    private val _result = MutableLiveData<Result<Unit>>()
    val result: LiveData<Result<Unit>> = _result

    fun confirm(code: String) = viewModelScope.launch {
        _result.postValue(repo.unlockCode(userId, code.trim()))
    }
}

class RewardsViewModelFactory(
    private val repo: RewardRepository,
    private val userId: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RewardsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RewardsViewModel(repo, userId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
