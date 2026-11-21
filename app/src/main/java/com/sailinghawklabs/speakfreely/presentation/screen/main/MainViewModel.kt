package com.sailinghawklabs.speakfreely.presentation.screen.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sailinghawklabs.speakfreely.domain.model.Language
import com.sailinghawklabs.speakfreely.domain.repository.SttRepository
import com.sailinghawklabs.speakfreely.domain.repository.TtsRepository
import com.sailinghawklabs.speakfreely.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


class MainViewModel(
    private val repository: UserPreferencesRepository,
    private val ttsRepository: TtsRepository,
    private val sttRepository: SttRepository
) : ViewModel() {

    private val _state = MutableStateFlow(MainState())

    // Combine local UI state with repository.observe*() reactive Flow stream for real-time updates
    val state =
        _state
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000L),
                initialValue = MainState()
            )


    init {
        // ViewModel init runs ONCE per screen instance, preventing re-fetching when navigating back
        getOneShotData()
    }

    // Handles the one-shot suspend repository fetches
    private fun getOneShotData() {
        viewModelScope.launch {

        }
    }

    private val _commands = Channel<MainScreenCommand>()
    val commandsForScreen = _commands.receiveAsFlow()

    private fun send(cmd: MainScreenCommand) {
        viewModelScope.launch { _commands.send(cmd) }
    }


    fun onEvent(event: MainScreenEvent) {
        when (event) {
            is MainScreenEvent.GoToSettings ->
                send(MainScreenCommand.NavigateToSettings)

            is MainScreenEvent.InputTextEdited -> {
                _state.update { it.copy(inputText = event.newText) }
            }

            is MainScreenEvent.TranslationTextEdited -> {
                _state.update { it.copy(translatedText = event.newText) }
            }

            MainScreenEvent.PlayAudioClicked -> {
                val currentState = _state.value
                val textToSpeak = currentState.translatedText
                if (textToSpeak.isNotBlank()) {
                    playText(
                        text = textToSpeak,
                        language = currentState.targetLanguage,
                        rate = currentState.playbackSpeed
                    )
                }
            }

            is MainScreenEvent.PlaybackSpeedChanged -> {
                _state.update { it.copy(playbackSpeed = event.newSpeed) }
            }

            MainScreenEvent.ReverseTranslationClicked -> {
                _state.value.detectedLanguage?.let { detected ->
                    _state.update {
                        it.copy(targetLanguage = detected)
                    }
                }
            }
        }
    }

    private fun playText(text: String, language: Language, rate: Float) {
        viewModelScope.launch {
            ttsRepository.speak(
                text = text,
                language = language,
                speechRate = rate
            )
        }

    }

}