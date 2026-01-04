package com.cil.shift.feature.settings.presentation

data class SettingsState(
    val userName: String = "Alex",
    val userEmail: String = "alex@example.com",
    val notificationsEnabled: Boolean = true,
    val appVersion: String = "1.0.0"
)
