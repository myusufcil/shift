package com.cil.shift.feature.onboarding.presentation.walkthrough

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class WalkthroughViewModel : ViewModel() {
    private val _state = MutableStateFlow(WalkthroughState())
    val state: StateFlow<WalkthroughState> = _state.asStateFlow()

    fun onEvent(event: WalkthroughEvent) {
        when (event) {
            is WalkthroughEvent.PageChanged -> {
                _state.update { it.copy(currentPage = event.page) }
            }
            WalkthroughEvent.NextPage -> {
                val currentPage = _state.value.currentPage
                if (currentPage < _state.value.totalPages - 1) {
                    _state.update { it.copy(currentPage = currentPage + 1) }
                }
            }
        }
    }
}

sealed interface WalkthroughEvent {
    data class PageChanged(val page: Int) : WalkthroughEvent
    data object NextPage : WalkthroughEvent
}
