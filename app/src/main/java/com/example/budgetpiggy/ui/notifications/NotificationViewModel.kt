package com.example.budgetpiggy.ui.notifications

import androidx.lifecycle.*
import com.example.budgetpiggy.data.entities.NotificationEntity
import com.example.budgetpiggy.data.repository.NotificationRepository
import kotlinx.coroutines.launch

class NotificationViewModel(
    private val repo: NotificationRepository,
    private val userId: String
) : ViewModel() {

    // Live data stream of notifications for the current user (Android, 2025).
    val notifications: LiveData<List<NotificationEntity>> =
        repo.streamForUser(userId).asLiveData()

    // Mark a single notification as read
    fun markAsRead(id: String) = viewModelScope.launch {
        repo.markAsRead(id)
    }

    // Clear all notification for the current user
    fun clearAll() = viewModelScope.launch {
        repo.clearAll(userId)
    }
}

class NotificationViewModelFactory(
    private val repo: NotificationRepository,
    private val userId: String
) : ViewModelProvider.Factory {

    // Create an instance of NotificationViewModel with dependencies injected
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NotificationViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NotificationViewModel(repo, userId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel: $modelClass")
    }
}
