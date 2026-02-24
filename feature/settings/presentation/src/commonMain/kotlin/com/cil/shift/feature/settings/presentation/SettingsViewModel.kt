package com.cil.shift.feature.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cil.shift.core.common.auth.AuthManager
import com.cil.shift.core.common.onboarding.OnboardingPreferences
import com.cil.shift.core.common.settings.SettingsPreferences
import com.cil.shift.feature.habits.domain.repository.HabitRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsPreferences: SettingsPreferences,
    private val onboardingPreferences: OnboardingPreferences,
    private val authManager: AuthManager,
    private val habitRepository: HabitRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        val userName = onboardingPreferences.getUserName().ifBlank { "User" }
        val userEmail = authManager.getCurrentUserEmail() ?: ""
        val notificationsEnabled = settingsPreferences.isNotificationsEnabled()

        _state.update {
            it.copy(
                userName = userName,
                userEmail = userEmail,
                notificationsEnabled = notificationsEnabled
            )
        }
    }

    fun onEvent(event: SettingsEvent) {
        when (event) {
            is SettingsEvent.ToggleNotifications -> {
                _state.update { it.copy(notificationsEnabled = event.enabled) }
                settingsPreferences.setNotificationsEnabled(event.enabled)
            }
            is SettingsEvent.UpdateUserName -> {
                _state.update { it.copy(userName = event.name) }
                onboardingPreferences.setUserName(event.name)
            }
            is SettingsEvent.DeleteAllHabits -> {
                viewModelScope.launch {
                    habitRepository.deleteAllHabits()
                }
            }
        }
    }
}

sealed interface SettingsEvent {
    data class ToggleNotifications(val enabled: Boolean) : SettingsEvent
    data class UpdateUserName(val name: String) : SettingsEvent
    data object DeleteAllHabits : SettingsEvent
}
