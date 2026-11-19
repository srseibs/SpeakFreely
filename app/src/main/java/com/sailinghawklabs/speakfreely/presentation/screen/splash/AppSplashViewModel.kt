package com.sailinghawklabs.speakfreely.presentation.screen.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sailinghawklabs.speakfreely.domain.repository.UserPreferencesRepository
import com.sailinghawklabs.speakfreely.presentation.screen.util.UiText
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

class AppSplashViewModel(
    private val repository: UserPreferencesRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AppSplashState())

    val state = _state
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = AppSplashState()
        )

    init {
        getOneShotData()
    }

    private fun getOneShotData() {
        viewModelScope.launch {
            updateStatus("Loading preferences...")
            preloadDataStore()

            updateStatus("Checking ML Kit models...")
            getStatusOfML()

            updateStatus("Warming up TTS engine...")
            warmUpTTS()

            // If permission is not yet granted, pause until isPermissionGranted becomes true
            if (!_state.value.isPermissionGranted) {
                _state.update {
                    it.copy(
                        showPermissionRationale = true,
                        statusMessage = UiText.DynamicString("Microphone permission required.")
                    )
                }
                _state.first { it.isPermissionGranted }
            }

            updateStatus("Initializing Speech Recognizer...")
            warmUpSpeechRecognizer()

            updateStatus("Ready!")
            delay(500.milliseconds)

            _state.update { it.copy(isReady = true) }
            send(AppSplashScreenCommand.NavigateToMainScreen)
        }
    }

    private suspend fun updateStatus(message: String) {
        _state.update { it.copy(statusMessage = UiText.DynamicString(message)) }
        delay(600.milliseconds)
    }

    private suspend fun preloadDataStore() {
        repository.getUserPreferences()
    }

    private suspend fun getStatusOfML() {
        // Query ML Kit downloaded models status
    }

    private suspend fun warmUpTTS() {
        // Bind and initialize TextToSpeech engine
    }

    private suspend fun warmUpSpeechRecognizer() {
        // Bind SpeechRecognizer engine
    }

    private val _commands = Channel<AppSplashScreenCommand>()
    val commandsForScreen = _commands.receiveAsFlow()

    private fun send(cmd: AppSplashScreenCommand) {
        viewModelScope.launch { _commands.send(cmd) }
    }

    fun onEvent(event: AppSplashScreenEvent) {
        when (event) {
            is AppSplashScreenEvent.PermissionIsChecked -> {
                if (event.isGranted) {
                    _state.update {
                        it.copy(
                            isPermissionGranted = true,
                            showPermissionRationale = false,
                            isPermissionPermanentlyDenied = false
                        )
                    }
                } else {
                    _state.update {
                        it.copy(
                            isPermissionGranted = false,
                            showPermissionRationale = true,
                            isPermissionPermanentlyDenied = event.isPermanentlyDenied,
                            statusMessage = UiText.DynamicString(
                                if (event.isPermanentlyDenied) "Microphone disabled in Settings."
                                else "Microphone permission required."
                            )
                        )
                    }
                }
            }
        }
    }
}
