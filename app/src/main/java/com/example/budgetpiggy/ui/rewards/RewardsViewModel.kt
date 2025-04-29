package com.example.budgetpiggy.ui.rewards

import androidx.lifecycle.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import com.example.budgetpiggy.data.entities.RewardCodeEntity
import com.example.budgetpiggy.data.entities.RewardEntity
import com.example.budgetpiggy.data.repository.RewardRepository

class RewardsViewModel(
    private val repo: RewardRepository,
    private val userId: String
) : ViewModel() {

    private val _rewards = MutableLiveData<List<Pair<RewardCodeEntity, Boolean>>>()
    val rewards: LiveData<List<Pair<RewardCodeEntity, Boolean>>> = _rewards

    private val _result = MutableLiveData<Result<Unit>>()
    val result: LiveData<Result<Unit>> = _result

    init {
        refresh()
    }

    fun refresh() = viewModelScope.launch {
        val codes = repo.allCodes().first()
        val unlocked = repo.userRewards(userId)
        val unlockedSet = unlocked.map { it.rewardId }.toSet()

        // Filter out locked ones
        val unlockedOnly = codes.filter { unlockedSet.contains(it.code) }
        _rewards.postValue(unlockedOnly.map { it to true })
    }


    fun confirm(code: String) = viewModelScope.launch {
        val result = repo.unlockCode(userId, code.trim())
        _result.postValue(result)
        if (result.isSuccess) refresh()
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
