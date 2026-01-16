package com.cil.shift.feature.onboarding.presentation.permission

import androidx.compose.runtime.Composable

/**
 * Platform-specific notification permission launcher.
 * On Android 13+, this will request the POST_NOTIFICATIONS runtime permission.
 * On iOS and older Android versions, this will use the NotificationManager directly.
 *
 * @param onResult Callback with the permission result (true if granted)
 * @return A function that triggers the permission request when called
 */
@Composable
expect fun rememberNotificationPermissionLauncher(onResult: (Boolean) -> Unit): () -> Unit
