package com.example.budgetpiggy.ui.notifications

import androidx.lifecycle.*
import com.example.budgetpiggy.data.entities.NotificationEntity
import com.example.budgetpiggy.data.repository.NotificationRepository
import kotlinx.coroutines.launch

class NotificationViewModel(
    private val repo: NotificationRepository,
    private val userId: String
) : ViewModel() {

    val notifications: LiveData<List<NotificationEntity>> =
        repo.streamForUser(userId).asLiveData()

    fun markAsRead(id: String) = viewModelScope.launch {
        repo.markAsRead(id)
    }

    fun clearAll() = viewModelScope.launch {
        repo.clearAll(userId)
    }
}

class NotificationViewModelFactory(
    private val repo: NotificationRepository,
    private val userId: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NotificationViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NotificationViewModel(repo, userId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel: $modelClass")
    }
}
