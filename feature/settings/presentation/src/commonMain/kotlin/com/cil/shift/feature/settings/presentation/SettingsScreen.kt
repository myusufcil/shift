package com.cil.shift.feature.settings.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cil.shift.core.common.theme.AppTheme
import com.cil.shift.core.common.theme.LocalThemeManager
import org.koin.compose.koinInject

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = koinInject()
) {
    val state by viewModel.state.collectAsState()
    val scrollState = rememberScrollState()
    val themeManager = LocalThemeManager.current
    val currentTheme by themeManager.currentTheme.collectAsState()
    val uriHandler = LocalUriHandler.current

    // GitHub Pages URLs
    val privacyPolicyUrl = "https://myusufcil.github.io/shift/privacy.html"
    val termsOfServiceUrl = "https://myusufcil.github.io/shift/terms.html"

    val backgroundColor = MaterialTheme.colorScheme.background
    val textColor = MaterialTheme.colorScheme.onBackground
    val cardColor = MaterialTheme.colorScheme.surface

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Header
            Text(
                text = "Settings",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )

            // Profile Section
            ProfileSection(
                userName = state.userName,
                userEmail = state.userEmail,
                textColor = textColor,
                cardColor = cardColor
            )

            HorizontalDivider(color = textColor.copy(alpha = 0.1f))

            // Theme Selection
            Text(
                text = "Appearance",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = textColor.copy(alpha = 0.7f)
            )

            ThemeSelector(
                currentTheme = currentTheme,
                onThemeSelected = { theme ->
                    themeManager.setTheme(theme)
                }
            )

            HorizontalDivider(color = textColor.copy(alpha = 0.1f))

            // Notifications
            SettingItem(
                title = "Notifications",
                subtitle = "Enable daily reminders",
                cardColor = cardColor,
                textColor = textColor,
                trailing = {
                    Switch(
                        checked = state.notificationsEnabled,
                        onCheckedChange = {
                            viewModel.onEvent(SettingsEvent.ToggleNotifications(it))
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF00D9FF),
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = cardColor
                        )
                    )
                }
            )

            HorizontalDivider(color = textColor.copy(alpha = 0.1f))

            // Account Settings
            Text(
                text = "Account",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = textColor.copy(alpha = 0.7f)
            )

            SettingItem(
                title = "Edit Profile",
                subtitle = "Change your name and email",
                cardColor = cardColor,
                textColor = textColor,
                onClick = { /* TODO: Navigate to edit profile */ }
            )

            SettingItem(
                title = "Privacy Policy",
                subtitle = "View our privacy policy",
                cardColor = cardColor,
                textColor = textColor,
                onClick = { uriHandler.openUri(privacyPolicyUrl) }
            )

            SettingItem(
                title = "Terms of Service",
                subtitle = "View terms of service",
                cardColor = cardColor,
                textColor = textColor,
                onClick = { uriHandler.openUri(termsOfServiceUrl) }
            )

            HorizontalDivider(color = textColor.copy(alpha = 0.1f))

            // About Section
            Text(
                text = "About",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = textColor.copy(alpha = 0.7f)
            )

            SettingItem(
                title = "Version",
                subtitle = state.appVersion,
                cardColor = cardColor,
                textColor = textColor,
                onClick = null
            )

            SettingItem(
                title = "Delete All Habits",
                subtitle = "Remove all your habit data",
                cardColor = cardColor,
                textColor = textColor,
                onClick = { /* TODO: Show confirmation dialog */ },
                titleColor = Color(0xFFFF6B6B)
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun ProfileSection(
    userName: String,
    userEmail: String,
    textColor: Color,
    cardColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(cardColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Profile",
                tint = textColor,
                modifier = Modifier.size(40.dp)
            )
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = userName,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
            Text(
                text = userEmail,
                fontSize = 14.sp,
                color = textColor.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun ThemeSelector(
    currentTheme: AppTheme,
    onThemeSelected: (AppTheme) -> Unit
) {
    val cardColor = MaterialTheme.colorScheme.surface
    val textColor = MaterialTheme.colorScheme.onBackground

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AppTheme.entries.forEach { theme ->
            val isSelected = theme == currentTheme
            val themeIcon = when (theme) {
                AppTheme.DARK -> "🌙"
                AppTheme.LIGHT -> "☀️"
                AppTheme.SYSTEM -> "📱"
            }
            val themeLabel = when (theme) {
                AppTheme.DARK -> "Dark"
                AppTheme.LIGHT -> "Light"
                AppTheme.SYSTEM -> "System"
            }

            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onThemeSelected(theme) },
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) Color(0xFF00D9FF).copy(alpha = 0.2f) else cardColor
                ),
                shape = RoundedCornerShape(12.dp),
                border = if (isSelected) {
                    androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF00D9FF))
                } else null
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = themeIcon,
                        fontSize = 24.sp
                    )
                    Text(
                        text = themeLabel,
                        fontSize = 14.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) Color(0xFF00D9FF) else textColor
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingItem(
    title: String,
    subtitle: String,
    cardColor: Color = MaterialTheme.colorScheme.surface,
    textColor: Color = MaterialTheme.colorScheme.onBackground,
    onClick: (() -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    titleColor: Color? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = cardColor
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (onClick != null) Modifier.clickable(onClick = onClick)
                    else Modifier
                )
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = titleColor ?: textColor
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = textColor.copy(alpha = 0.5f)
                )
            }

            if (trailing != null) {
                trailing()
            }
        }
    }
}
