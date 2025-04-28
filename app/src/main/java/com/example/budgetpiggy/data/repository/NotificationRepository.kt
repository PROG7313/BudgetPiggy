package com.example.budgetpiggy.data.repository

import com.example.budgetpiggy.data.dao.NotificationDao
import com.example.budgetpiggy.data.entities.NotificationEntity
import kotlinx.coroutines.flow.Flow

class NotificationRepository(private val dao: NotificationDao) {
    fun streamForUser(userId: String): Flow<List<NotificationEntity>> =
        dao.streamForUser(userId)

    fun notificationsFor(userId: String): Flow<List<NotificationEntity>> =
        dao.notificationsFor(userId)

    suspend fun insert(notification: NotificationEntity) =
        dao.insert(notification)

    suspend fun markAsRead(id: String) =
        dao.markAsRead(id)

    suspend fun clearAll(userId: String) =
        dao.clearAll(userId)
}
