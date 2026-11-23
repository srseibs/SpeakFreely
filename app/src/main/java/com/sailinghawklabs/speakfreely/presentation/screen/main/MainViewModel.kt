package com.sailinghawklabs.speakfreely.presentation.screen.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sailinghawklabs.speakfreely.domain.model.Language
import com.sailinghawklabs.speakfreely.domain.model.SpeechResult
import com.sailinghawklabs.speakfreely.domain.repository.LanguageIdentifierRepository
import com.sailinghawklabs.speakfreely.domain.repository.SttRepository
import com.sailinghawklabs.speakfreely.domain.repository.TranslationRepository
import com.sailinghawklabs.speakfreely.domain.repository.TtsRepository
import com.sailinghawklabs.speakfreely.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainViewModel(
    private val userPrefRepository: UserPreferencesRepository,
    private val ttsRepository: TtsRepository,
    private val sttRepository: SttRepository,
    private val translationRepository: TranslationRepository,
    private val languageIdentifierRepository: LanguageIdentifierRepository
) : ViewModel() {

    private val _state = MutableStateFlow(MainState())

    // Combine local UI state with repository.observe*() reactive Flow stream for real-time updates
    val state: StateFlow<MainState> = combine(
        _state,
        sttRepository.observeSpeechResults().onEach { speechResult ->
            if (speechResult is SpeechResult.FinalResult && speechResult.text.isNotBlank()) {
                val activePanel = if (_state.value.externalPanel.isListening)
                    PanelTarget.EXTERNAL
                else
                    PanelTarget.RESPONSE
                triggerTranslation(activePanel, speechResult.text)
            }
        }
    ) { localState, speechResult ->
        calculateState(localState, speechResult)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = MainState()
    )

    private fun calculateState(localState: MainState, speechResult: SpeechResult): MainState {
        return when (speechResult) {
            SpeechResult.Idle -> localState.copy(
                externalPanel = localState.externalPanel.copy(isListening = false),
                responsePanel = localState.responsePanel.copy(isListening = false)
            )

            SpeechResult.Listening -> {
                if (localState.externalPanel.isListening) {
                    localState.copy(externalPanel = localState.externalPanel.copy(isListening = true))
                } else if (localState.responsePanel.isListening) {
                    localState.copy(responsePanel = localState.responsePanel.copy(isListening = true))
                } else {
                    localState
                }
            }

            is SpeechResult.PartialResult -> {
                if (localState.externalPanel.isListening) {
                    localState.copy(externalPanel = localState.externalPanel.copy(spokenText = speechResult.text))
                } else if (localState.responsePanel.isListening) {
                    localState.copy(responsePanel = localState.responsePanel.copy(spokenText = speechResult.text))
                } else {
                    localState
                }
            }

            is SpeechResult.FinalResult -> {
                if (localState.externalPanel.isListening) {
                    localState.copy(
                        externalPanel = localState.externalPanel.copy(
                            spokenText = speechResult.text,
                            isListening = false
                        )
                    )
                } else if (localState.responsePanel.isListening) {
                    localState.copy(
                        responsePanel = localState.responsePanel.copy(
                            spokenText = speechResult.text,
                            isListening = false
                        )
                    )
                } else {
                    localState.copy(
                        externalPanel = localState.externalPanel.copy(isListening = false),
                        responsePanel = localState.responsePanel.copy(isListening = false)
                    )
                }
            }

            is SpeechResult.Error -> localState.copy(
                externalPanel = localState.externalPanel.copy(isListening = false),
                responsePanel = localState.responsePanel.copy(isListening = false)
            )
        }
    }

    init {
        getOneShotData()
    }

    private fun getOneShotData() {
        viewModelScope.launch {

        }
    }

    private fun triggerTranslation(target: PanelTarget, text: String) {
        if (text.isBlank()) {
            updatePanel(target) { panel -> panel.copy(translatedText = "") }
            return
        }

        viewModelScope.launch {
            val currentPanel = if (target == PanelTarget.EXTERNAL)
                _state.value.externalPanel
            else
                _state.value.responsePanel

            // 1. On EXTERNAL panel: Identify foreign language in ~10ms if Auto-Detect is enabled
            val sourceLang =
                if (target == PanelTarget.EXTERNAL && currentPanel.isAutoDetectEnabled) {
                    val detectedCode = languageIdentifierRepository.identifyLanguage(text)
                    val lang = Language.fromBcp47Code(detectedCode) ?: currentPanel.spokenLanguage

                    // Automatically sync foreign language across BOTH panels!
                    _state.update { state ->
                        state.copy(
                            externalPanel = state.externalPanel.copy(spokenLanguage = lang),
                            responsePanel = state.responsePanel.copy(targetLanguage = lang)
                        )
                    }
                    lang
                } else {
                    currentPanel.spokenLanguage
                }

            val targetLang = currentPanel.targetLanguage

            // 2. Perform ML Kit Translation
            val result = translationRepository.translate(
                text = text,
                sourceLanguage = sourceLang,
                targetLanguage = targetLang
            )

            result.getOrNull()?.let { translated ->
                updatePanel(target) { panel -> panel.copy(translatedText = translated) }
            }
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

            is MainScreenEvent.SpokenTextEdited -> {
                updatePanel(event.panel) { panel ->
                    panel.copy(spokenText = event.newText)
                }
                triggerTranslation(event.panel, event.newText)
            }

            is MainScreenEvent.TranslationTextEdited -> {
                updatePanel(event.panel) { panel ->
                    panel.copy(translatedText = event.newText)
                }
            }

            is MainScreenEvent.SpokenSpeedChanged -> {
                updatePanel(event.panel) { panel ->
                    panel.copy(spokenPlaybackSpeed = event.newSpeed)
                }
            }

            is MainScreenEvent.TranslationSpeedChanged -> {
                updatePanel(event.panel) { panel ->
                    panel.copy(translatedPlaybackSpeed = event.newSpeed)
                }
            }

            is MainScreenEvent.AutoDetectToggled -> {
                updatePanel(event.panel) { panel ->
                    panel.copy(isAutoDetectEnabled = event.enabled)
                }
            }

            is MainScreenEvent.SpokenLanguageSelected -> {
                updatePanel(event.panel) { panel ->
                    panel.copy(
                        spokenLanguage = event.newLanguage,
                        isAutoDetectEnabled = false // Lock when manually picked
                    )
                }
                triggerTranslation(
                    event.panel,
                    if (event.panel == PanelTarget.EXTERNAL) _state.value.externalPanel.spokenText else _state.value.responsePanel.spokenText
                )
            }

            is MainScreenEvent.TargetLanguageSelected -> {
                updatePanel(event.panel) { panel ->
                    panel.copy(targetLanguage = event.newLanguage)
                }
                triggerTranslation(
                    event.panel,
                    if (event.panel == PanelTarget.EXTERNAL) _state.value.externalPanel.spokenText else _state.value.responsePanel.spokenText
                )
            }


            else -> {}


        }
    }

    private fun updatePanel(
        target: PanelTarget,
        updateBlock: (PanelState) -> PanelState
    ) {
        _state.update { currentState ->
            when (target) {
                PanelTarget.EXTERNAL -> currentState.copy(
                    externalPanel = updateBlock(currentState.externalPanel)
                )

                PanelTarget.RESPONSE -> currentState.copy(
                    responsePanel = updateBlock(currentState.responsePanel)
                )
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
