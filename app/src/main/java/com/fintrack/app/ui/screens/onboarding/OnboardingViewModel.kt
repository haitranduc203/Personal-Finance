package com.fintrack.app.ui.screens.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fintrack.app.data.repository.PreferencesRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

sealed interface OnboardingUiEvent {
    data object NavigateToHome : OnboardingUiEvent
}

class OnboardingViewModel(
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    private val _eventChannel = Channel<OnboardingUiEvent>(Channel.BUFFERED)
    val events = _eventChannel.receiveAsFlow()

    fun completeOnboarding() {
        viewModelScope.launch {
            preferencesRepository.setOnboardingCompleted(true)
            _eventChannel.send(OnboardingUiEvent.NavigateToHome)
        }
    }
}
