package com.cil.shift.feature.settings.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.cil.shift.core.common.currentTimestamp
import com.cil.shift.core.common.localization.LocalizationManager
import com.cil.shift.core.common.localization.StringResources
import org.koin.compose.getKoin
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val koin = getKoin()
    val viewModel = koin.get<NotificationsViewModel>()
    val localizationManager = koin.get<LocalizationManager>()
    val currentLanguage by localizationManager.currentLanguage.collectAsState()
    val state by viewModel.state.collectAsState()

    val colorScheme = MaterialTheme.colorScheme

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = StringResources.notifications.get(currentLanguage),
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onBackground
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = StringResources.back.get(currentLanguage),
                            tint = colorScheme.onBackground
                        )
                    }
                },
                actions = {
                    if (state.unreadCount > 0) {
                        TextButton(
                            onClick = { viewModel.markAllAsRead() }
                        ) {
                            Icon(
                                imageVector = Icons.Default.DoneAll,
                                contentDescription = null,
                                tint = colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = StringResources.markAllAsRead.get(currentLanguage),
                                color = colorScheme.primary,
                                fontSize = 14.sp
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorScheme.background
                )
            )
        },
        containerColor = colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = colorScheme.primary)
            }
        } else if (state.notifications.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(
                                color = colorScheme.primary.copy(alpha = 0.1f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            tint = colorScheme.primary.copy(alpha = 0.5f)
                        )
                    }
                    Text(
                        text = StringResources.noNotifications.get(currentLanguage),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colorScheme.onBackground
                    )
                    Text(
                        text = StringResources.notificationHistoryDescription.get(currentLanguage),
                        fontSize = 14.sp,
                        color = colorScheme.onBackground.copy(alpha = 0.6f),
                        modifier = Modifier.widthIn(max = 280.dp)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.notifications, key = { it.id }) { notification ->
                    NotificationCard(
                        notification = notification,
                        currentLanguage = currentLanguage,
                        onMarkAsRead = { viewModel.markAsRead(notification.id) }
                    )
                }

                // Footer spacing
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun NotificationCard(
    notification: NotificationItemUi,
    currentLanguage: com.cil.shift.core.common.localization.Language,
    onMarkAsRead: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (notification.isRead)
                    colorScheme.surfaceVariant.copy(alpha = 0.5f)
                else
                    colorScheme.surfaceVariant
            )
            .clickable(enabled = !notification.isRead) {
                onMarkAsRead()
            }
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Indicator dot
        Box(
            modifier = Modifier
                .padding(top = 4.dp)
                .size(8.dp)
                .clip(CircleShape)
                .background(
                    if (notification.isRead) Color.Transparent
                    else colorScheme.primary
                )
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = notification.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (notification.isRead)
                        colorScheme.onSurface.copy(alpha = 0.6f)
                    else
                        colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = formatTimestamp(notification.timestamp, currentLanguage),
                    fontSize = 11.sp,
                    color = colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }

            Text(
                text = notification.message,
                fontSize = 13.sp,
                color = if (notification.isRead)
                    colorScheme.onSurface.copy(alpha = 0.5f)
                else
                    colorScheme.onSurface.copy(alpha = 0.7f),
                lineHeight = 18.sp
            )

            // Habit name tag
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(colorScheme.primary.copy(alpha = 0.2f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = notification.habitName,
                    fontSize = 11.sp,
                    color = colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

private fun formatTimestamp(timestamp: Long, language: com.cil.shift.core.common.localization.Language): String {
    val now = currentTimestamp()
    val diff = now - timestamp
    val seconds = diff / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24

    return when {
        seconds < 60 -> when (language) {
            com.cil.shift.core.common.localization.Language.ENGLISH -> "Just now"
            com.cil.shift.core.common.localization.Language.TURKISH -> "Şimdi"
            com.cil.shift.core.common.localization.Language.SPANISH -> "Ahora"
        }
        minutes < 60 -> when (language) {
            com.cil.shift.core.common.localization.Language.ENGLISH -> "$minutes min ago"
            com.cil.shift.core.common.localization.Language.TURKISH -> "$minutes dk önce"
            com.cil.shift.core.common.localization.Language.SPANISH -> "Hace $minutes min"
        }
        hours < 24 -> when (language) {
            com.cil.shift.core.common.localization.Language.ENGLISH -> "$hours hours ago"
            com.cil.shift.core.common.localization.Language.TURKISH -> "$hours saat önce"
            com.cil.shift.core.common.localization.Language.SPANISH -> "Hace $hours horas"
        }
        days < 7 -> when (language) {
            com.cil.shift.core.common.localization.Language.ENGLISH -> "$days days ago"
            com.cil.shift.core.common.localization.Language.TURKISH -> "$days gün önce"
            com.cil.shift.core.common.localization.Language.SPANISH -> "Hace $days días"
        }
        else -> when (language) {
            com.cil.shift.core.common.localization.Language.ENGLISH -> "${days / 7} weeks ago"
            com.cil.shift.core.common.localization.Language.TURKISH -> "${days / 7} hafta önce"
            com.cil.shift.core.common.localization.Language.SPANISH -> "Hace ${days / 7} semanas"
        }
    }
}
