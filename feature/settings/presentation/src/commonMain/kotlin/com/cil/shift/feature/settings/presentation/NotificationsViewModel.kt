package com.cil.shift.feature.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.cil.shift.core.common.currentTimestamp
import com.cil.shift.feature.habits.data.database.HabitsDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class NotificationItemUi(
    val id: String,
    val habitId: String,
    val habitName: String,
    val title: String,
    val message: String,
    val timestamp: Long,
    val isRead: Boolean,
    val actionTaken: String?
)

data class NotificationsState(
    val notifications: List<NotificationItemUi> = emptyList(),
    val isLoading: Boolean = false,
    val unreadCount: Int = 0
)

class NotificationsViewModel(
    private val database: HabitsDatabase
) : ViewModel() {

    private val _state = MutableStateFlow(NotificationsState())
    val state: StateFlow<NotificationsState> = _state.asStateFlow()

    init {
        loadNotifications()
    }

    private fun loadNotifications() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            database.habitQueries.getAllNotifications()
                .asFlow()
                .mapToList(Dispatchers.IO)
                .collect { notifications ->
                    val notificationItems = notifications.map { notification ->
                        NotificationItemUi(
                            id = notification.id,
                            habitId = notification.habit_id,
                            habitName = notification.habit_name,
                            title = notification.title,
                            message = notification.message,
                            timestamp = notification.timestamp,
                            isRead = notification.is_read == 1L,
                            actionTaken = notification.action_taken
                        )
                    }

                    val unreadCount = notificationItems.count { !it.isRead }

                    _state.update {
                        it.copy(
                            notifications = notificationItems,
                            isLoading = false,
                            unreadCount = unreadCount
                        )
                    }
                }
        }
    }

    fun markAsRead(notificationId: String) {
        viewModelScope.launch {
            database.habitQueries.markAsRead(notificationId)
        }
    }

    fun markAllAsRead() {
        viewModelScope.launch {
            database.habitQueries.markAllAsRead()
        }
    }

    fun deleteNotification(notificationId: String) {
        viewModelScope.launch {
            database.habitQueries.deleteNotification(notificationId)
        }
    }

    fun deleteOldNotifications() {
        viewModelScope.launch {
            // Delete notifications older than 30 days
            val thirtyDaysAgo = currentTimestamp() - (30L * 24 * 60 * 60 * 1000)
            database.habitQueries.deleteOldNotifications(thirtyDaysAgo)
        }
    }
}
