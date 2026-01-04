package com.cil.shift.feature.settings.presentation

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class SettingsViewModel : ViewModel() {

    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    fun onEvent(event: SettingsEvent) {
        when (event) {
            is SettingsEvent.ToggleNotifications -> {
                _state.update { it.copy(notificationsEnabled = event.enabled) }
                // TODO: Persist setting
            }
            is SettingsEvent.UpdateUserName -> {
                _state.update { it.copy(userName = event.name) }
                // TODO: Persist setting
            }
        }
    }
}

sealed interface SettingsEvent {
    data class ToggleNotifications(val enabled: Boolean) : SettingsEvent
    data class UpdateUserName(val name: String) : SettingsEvent
}
