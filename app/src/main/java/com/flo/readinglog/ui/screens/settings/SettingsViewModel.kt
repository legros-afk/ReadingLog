package com.flo.readinglog.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flo.readinglog.data.remote.WhatsAppNotifier
import com.flo.readinglog.domain.model.OpenClawSettings
import com.flo.readinglog.domain.repository.AuthRepository
import com.flo.readinglog.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface TestMessageState {
    data object Idle : TestMessageState
    data object Loading : TestMessageState
    data object Success : TestMessageState
    data class Error(val message: String) : TestMessageState
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val whatsAppNotifier: WhatsAppNotifier,
    private val authRepository: AuthRepository,
) : ViewModel() {

    val settings: StateFlow<OpenClawSettings> = settingsRepository.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), OpenClawSettings())

    private val _testState = MutableStateFlow<TestMessageState>(TestMessageState.Idle)
    val testState: StateFlow<TestMessageState> = _testState.asStateFlow()

    fun save(baseUrl: String, apiToken: String, parentNumber: String) {
        viewModelScope.launch {
            settingsRepository.save(OpenClawSettings(baseUrl.trim(), apiToken.trim(), parentNumber.trim()))
        }
    }

    fun sendTest(baseUrl: String, apiToken: String, parentNumber: String) {
        viewModelScope.launch {
            if (baseUrl.isBlank() || parentNumber.isBlank()) {
                _testState.value = TestMessageState.Error("Base URL and parent number are required")
                return@launch
            }
            _testState.value = TestMessageState.Loading
            val result = whatsAppNotifier.send(
                baseUrl = baseUrl.trim(),
                apiToken = apiToken.trim(),
                to = parentNumber.trim(),
                message = "Test message from ReadingLog!",
            )
            _testState.value = result.fold(
                onSuccess = { TestMessageState.Success },
                onFailure = { TestMessageState.Error(it.message ?: "Unknown error") },
            )
        }
    }

    fun clearTestState() {
        _testState.value = TestMessageState.Idle
    }

    fun signOut() {
        viewModelScope.launch { authRepository.signOut() }
    }
}
