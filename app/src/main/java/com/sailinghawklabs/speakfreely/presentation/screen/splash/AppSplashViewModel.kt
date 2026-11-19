package com.sailinghawklabs.speakfreely.presentation.screen.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sailinghawklabs.speakfreely.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds


class AppSplashViewModel(
    private val repository: UserPreferencesRepository
) : ViewModel() {


    private val _state = MutableStateFlow(AppSplashState())

    // Combine local UI state with repository.observe*() reactive Flow stream for real-time updates
    val state = _state
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = AppSplashState()
        )


    init {
        // ViewModel init runs ONCE per screen instance, preventing re-fetching when navigating back
        getOneShotData()
    }

    // Handles the one-shot suspend repository fetches
    private fun getOneShotData() {
        viewModelScope.launch {

            // Minimum splash display timer (1.5s) for animation smoothness
            val minDisplayTimer = async {
                delay(1500L.milliseconds)
            }

            // Real background initialization & engine warm-up
            val initializationWork = async {
                preloadDataStore()
                getStatusOfML()
                warmUpTTS()
                warmUpSpeechRecognizer()
                verifyPermissions()
            }

            // Wait for BOTH the timer AND real initialization work to finish
            awaitAll(minDisplayTimer, initializationWork)

            // Transition seamlessly to Destination.Main
            send(AppSplashScreenCommand.NavigateToMainScreen)
        }
    }

    private suspend fun preloadDataStore() {
        // Load DataStore preferences into memory
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

    private suspend fun verifyPermissions() {
        // Audit RECORD_AUDIO permission state
    }

    private val _commands = Channel<AppSplashScreenCommand>()
    val commandsForScreen = _commands.receiveAsFlow()

    private fun send(cmd: AppSplashScreenCommand) {
        viewModelScope.launch { _commands.send(cmd) }
    }

    fun onEvent(event: AppSplashScreenEvent) {
        when (event) {
            else -> TODO("Handle events")
        }
    }

}