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
                triggerTranslation(speechResult.text)
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
            SpeechResult.Idle -> localState.copy(isListening = false)
            SpeechResult.Listening -> localState.copy(isListening = true)
            is SpeechResult.PartialResult -> localState.copy(
                inputText = speechResult.text,
                isListening = true
            )

            is SpeechResult.FinalResult -> localState.copy(
                inputText = speechResult.text,
                isListening = false,
                detectedLanguage = Language.fromBcp47Code(speechResult.detectedLanguageCode)
                    ?: localState.detectedLanguage
            )

            is SpeechResult.Error -> localState.copy(
                isListening = false
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

    private fun triggerTranslation(text: String) {
        if (text.isBlank()) {
            _state.update { it.copy(translatedText = "", detectedLanguage = null) }
            return
        }

        viewModelScope.launch {
            // 1. Identify the language on-device in ~10ms
            val detectedCode = languageIdentifierRepository.identifyLanguage(text)
            val detectedLang =
                Language.fromBcp47Code(detectedCode) ?: Language.DEFAULT_TARGET_LANGUAGE

            // 2. Update state with the identified language
            _state.update { it.copy(detectedLanguage = detectedLang) }

            // 3. Translate from detected language -> targetLanguage (Owner language)
            val targetLang = _state.value.targetLanguage

            val result = translationRepository.translate(
                text = text,
                sourceLanguage = detectedLang,
                targetLanguage = targetLang
            )

            result.getOrNull()?.let { translated ->
                _state.update { it.copy(translatedText = translated) }
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

            is MainScreenEvent.InputTextEdited -> {
                _state.update { it.copy(inputText = event.newText) }
                if (event.newText.isBlank()) {
                    sttRepository.stopListening()
                }
                triggerTranslation(event.newText)
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

            MainScreenEvent.MicButtonClicked -> {
                if (_state.value.isListening) {
                    sttRepository.stopListening()
                } else {
                    val currentLanguage =
                        _state.value.detectedLanguage ?: Language.DEFAULT_TARGET_LANGUAGE
                    sttRepository.startListening(currentLanguage)
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
