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

    val state: StateFlow<MainState> = combine(
        _state,
        sttRepository.observeSpeechResults().onEach { speechResult ->
            if (speechResult is SpeechResult.FinalResult && speechResult.text.isNotBlank()) {
                val activeTarget =
                    if (_state.value.activeMic != TalkerTarget.NONE) _state.value.activeMic else TalkerTarget.TOP
                triggerTranslation(activeTarget, speechResult.text)
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
        val activeTarget = localState.activeMic
        if (activeTarget == TalkerTarget.NONE) return localState

        return when (speechResult) {
            SpeechResult.Idle -> localState.copy(activeMic = TalkerTarget.NONE)
            SpeechResult.Listening -> localState
            is SpeechResult.PartialResult -> {
                updateCardState(localState, activeTarget) { card ->
                    card.copy(text = speechResult.text)
                }
            }

            is SpeechResult.FinalResult -> {
                updateCardState(localState, activeTarget) { card ->
                    card.copy(text = speechResult.text)
                }.copy(activeMic = TalkerTarget.NONE)
            }

            is SpeechResult.Error -> localState.copy(activeMic = TalkerTarget.NONE)
        }
    }

    init {
        getOneShotData()
    }

    private fun getOneShotData() {
        viewModelScope.launch {

        }
    }

    private fun triggerTranslation(sourceTarget: TalkerTarget, text: String) {
        val targetTarget =
            if (sourceTarget == TalkerTarget.TOP) TalkerTarget.BOTTOM else TalkerTarget.TOP
        val flowDir =
            if (sourceTarget == TalkerTarget.TOP) TranslationDirection.DOWN else TranslationDirection.UP

        if (text.isBlank()) {
            updateCard(targetTarget) { card -> card.copy(text = "") }
            _state.update { it.copy(translationDirection = TranslationDirection.IDLE) }
            return
        }

        viewModelScope.launch {
            val sourceCard =
                if (sourceTarget == TalkerTarget.TOP) _state.value.topCard else _state.value.bottomCard
            val targetCard =
                if (sourceTarget == TalkerTarget.TOP) _state.value.bottomCard else _state.value.topCard

            // 1. Identify foreign language on-device in ~10ms if Auto-Detect is enabled on sourceCard
            val sourceLang =
                if (sourceTarget == TalkerTarget.TOP && sourceCard.isAutoDetectEnabled) {
                    val detectedCode = languageIdentifierRepository.identifyLanguage(text)
                    val lang = Language.fromBcp47Code(detectedCode) ?: sourceCard.language

                    // Automatically update top card language
                    _state.update { state ->
                        state.copy(
                            topCard = state.topCard.copy(language = lang)
                        )
                    }
                    lang
                } else {
                    sourceCard.language
                }

            val targetLang = targetCard.language

            // 2. Perform ML Kit Translation
            val result = translationRepository.translate(
                text = text,
                sourceLanguage = sourceLang,
                targetLanguage = targetLang
            )

            result.getOrNull()?.let { translated ->
                updateCard(targetTarget) { card -> card.copy(text = translated) }
                _state.update { it.copy(translationDirection = flowDir) }
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

            is MainScreenEvent.TextEdited -> {
                val flowDir =
                    if (event.target == TalkerTarget.TOP) TranslationDirection.DOWN else TranslationDirection.UP
                updateCard(event.target) { card ->
                    card.copy(text = event.newText)
                }
                if (event.newText.isBlank()) {
                    sttRepository.stopListening()
                    _state.update { it.copy(translationDirection = TranslationDirection.IDLE) }
                } else {
                    _state.update { it.copy(translationDirection = flowDir) }
                }
                triggerTranslation(event.target, event.newText)
            }

            is MainScreenEvent.CardFocused -> {
                val flowDir = if (event.target == TalkerTarget.TOP)
                    TranslationDirection.DOWN
                else
                    TranslationDirection.UP
                _state.update { it.copy(translationDirection = flowDir) }
            }

            is MainScreenEvent.SpeedChanged -> {
                updateCard(event.target) { card ->
                    card.copy(playbackSpeed = event.newSpeed)
                }
            }

            is MainScreenEvent.AutoDetectToggled -> {
                updateCard(event.target) { card ->
                    card.copy(isAutoDetectEnabled = event.enabled)
                }
            }

            is MainScreenEvent.LanguageSelected -> {
                updateCard(event.target) { card ->
                    card.copy(
                        language = event.newLanguage,
                        isAutoDetectEnabled = false
                    )
                }
                val activeText =
                    if (event.target == TalkerTarget.TOP) _state.value.topCard.text else _state.value.bottomCard.text
                triggerTranslation(event.target, activeText)
            }

            is MainScreenEvent.PlayClicked -> {
                val card =
                    if (event.target == TalkerTarget.TOP) _state.value.topCard else _state.value.bottomCard
                if (card.text.isNotBlank()) {
                    playText(
                        text = card.text,
                        language = card.language,
                        rate = card.playbackSpeed
                    )
                }
            }

            is MainScreenEvent.MicButtonClicked -> {
                val wasTappedActive = _state.value.activeMic == event.target

                sttRepository.stopListening()

                if (wasTappedActive) {
                    val hasText =
                        _state.value.topCard.text.isNotBlank() || _state.value.bottomCard.text.isNotBlank()
                    _state.update {
                        it.copy(
                            activeMic = TalkerTarget.NONE,
                            translationDirection = if (hasText) it.translationDirection else TranslationDirection.IDLE
                        )
                    }
                } else {
                    val flowDir =
                        if (event.target == TalkerTarget.TOP) TranslationDirection.DOWN else TranslationDirection.UP
                    _state.update {
                        it.copy(
                            activeMic = event.target,
                            translationDirection = flowDir
                        )
                    }
                    val activeCard =
                        if (event.target == TalkerTarget.TOP) _state.value.topCard else _state.value.bottomCard
                    sttRepository.startListening(activeCard.language)
                }
            }
        }
    }

    private fun updateCard(
        target: TalkerTarget,
        updateBlock: (LanguageCardState) -> LanguageCardState
    ) {
        _state.update { currentState ->
            updateCardState(currentState, target, updateBlock)
        }
    }

    private fun updateCardState(
        state: MainState,
        target: TalkerTarget,
        updateBlock: (LanguageCardState) -> LanguageCardState
    ): MainState {
        return when (target) {
            TalkerTarget.TOP -> state.copy(
                topCard = updateBlock(state.topCard)
            )

            TalkerTarget.BOTTOM -> state.copy(
                bottomCard = updateBlock(state.bottomCard)
            )

            TalkerTarget.NONE -> state
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
