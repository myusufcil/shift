package com.cil.shift.feature.onboarding.presentation.permission

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.cil.shift.core.common.notification.NotificationManager
import kotlinx.coroutines.launch
import org.koin.compose.getKoin

/**
 * Android implementation of notification permission launcher.
 * On Android 13+ (API 33+), requests the POST_NOTIFICATIONS runtime permission.
 * On older Android versions, notifications are allowed by default.
 */
@Composable
actual fun rememberNotificationPermissionLauncher(onResult: (Boolean) -> Unit): () -> Unit {
    val scope = rememberCoroutineScope()
    val koin = getKoin()
    val notificationManager = koin.get<NotificationManager>()

    // Only Android 13+ requires runtime permission
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val launcher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            onResult(isGranted)
        }

        return remember(launcher) {
            { launcher.launch(Manifest.permission.POST_NOTIFICATIONS) }
        }
    } else {
        // Pre-Android 13: notifications allowed by default
        return remember {
            {
                scope.launch {
                    val hasPermission = notificationManager.hasNotificationPermission()
                    onResult(hasPermission)
                }
                Unit
            }
        }
    }
}
