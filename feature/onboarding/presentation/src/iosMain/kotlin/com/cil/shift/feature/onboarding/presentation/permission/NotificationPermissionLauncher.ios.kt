package com.cil.shift.feature.onboarding.presentation.permission

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import com.cil.shift.core.common.notification.NotificationManager
import kotlinx.coroutines.launch
import org.koin.compose.getKoin

/**
 * iOS implementation of notification permission launcher.
 * Uses UNUserNotificationCenter to request notification authorization.
 */
@Composable
actual fun rememberNotificationPermissionLauncher(onResult: (Boolean) -> Unit): () -> Unit {
    val scope = rememberCoroutineScope()
    val koin = getKoin()
    val notificationManager = koin.get<NotificationManager>()

    return {
        scope.launch {
            val granted = notificationManager.requestNotificationPermission()
            onResult(granted)
        }
    }
}
