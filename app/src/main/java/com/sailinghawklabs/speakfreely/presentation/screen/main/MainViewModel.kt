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
                val activePanel = _state.value.activeListeningPanel ?: PanelTarget.EXTERNAL
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

    private fun calculateState(
        localState: MainState,
        speechResult: SpeechResult
    ): MainState {
        val activePanel = localState.activeListeningPanel ?: return localState

        return when (speechResult) {
            SpeechResult.Idle -> localState.copy(activeListeningPanel = null)
            SpeechResult.Listening -> localState
            is SpeechResult.PartialResult -> {
                updatePanelState(localState, activePanel) { panel ->
                    panel.copy(spokenText = speechResult.text)
                }
            }

            is SpeechResult.FinalResult -> {
                updatePanelState(localState, activePanel) { panel ->
                    panel.copy(spokenText = speechResult.text)
                }.copy(activeListeningPanel = null)
            }

            is SpeechResult.Error -> localState.copy(activeListeningPanel = null)
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
                if (event.newText.isBlank()) {
                    sttRepository.stopListening()
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
                _state.update { state ->
                    if (event.panel == PanelTarget.EXTERNAL) {
                        state.copy(
                            externalPanel = state.externalPanel.copy(
                                spokenLanguage = event.newLanguage,
                                isAutoDetectEnabled = false
                            ),
                            responsePanel = state.responsePanel.copy(targetLanguage = event.newLanguage)
                        )
                    } else {
                        state.copy(
                            responsePanel = state.responsePanel.copy(spokenLanguage = event.newLanguage)
                        )
                    }
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

            is MainScreenEvent.PlaySpokenClicked -> {
                val targetPanel = if (event.panel == PanelTarget.EXTERNAL) {
                    _state.value.externalPanel
                } else {
                    _state.value.responsePanel
                }

                if (targetPanel.spokenText.isNotBlank()) {
                    playText(
                        text = targetPanel.spokenText,
                        language = targetPanel.spokenLanguage,
                        rate = targetPanel.spokenPlaybackSpeed
                    )
                }
            }

            is MainScreenEvent.PlayTranslationClicked -> {
                val targetPanel = if (event.panel == PanelTarget.EXTERNAL) {
                    _state.value.externalPanel
                } else {
                    _state.value.responsePanel
                }

                if (targetPanel.translatedText.isNotBlank()) {
                    playText(
                        text = targetPanel.translatedText,
                        language = targetPanel.targetLanguage,
                        rate = targetPanel.translatedPlaybackSpeed
                    )
                }
            }

            is MainScreenEvent.MicButtonClicked -> {
                val isTappedPanelActive = _state.value.activeListeningPanel == event.panel

                sttRepository.stopListening()

                if (isTappedPanelActive) {
                    // Stop active recording
                    _state.update { it.copy(activeListeningPanel = null) }
                } else {
                    // Start recording on tapped panel
                    _state.update { it.copy(activeListeningPanel = event.panel) }
                    val targetPanel =
                        if (event.panel == PanelTarget.EXTERNAL) _state.value.externalPanel else _state.value.responsePanel
                    sttRepository.startListening(targetPanel.spokenLanguage)
                }
            }
        }
    }

    private fun updatePanel(
        target: PanelTarget,
        updateBlock: (PanelState) -> PanelState
    ) {
        _state.update { currentState ->
            updatePanelState(currentState, target, updateBlock)
        }
    }

    private fun updatePanelState(
        state: MainState,
        target: PanelTarget,
        updateBlock: (PanelState) -> PanelState
    ): MainState {
        return when (target) {
            PanelTarget.EXTERNAL -> state.copy(
                externalPanel = updateBlock(state.externalPanel)
            )

            PanelTarget.RESPONSE -> state.copy(
                responsePanel = updateBlock(state.responsePanel)
            )
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
