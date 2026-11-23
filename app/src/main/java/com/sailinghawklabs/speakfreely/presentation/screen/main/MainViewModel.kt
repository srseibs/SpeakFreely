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
        return MainState()
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

            return
        }

        viewModelScope.launch {
            // 1. Identify the language on-device in ~10ms
            val detectedCode = languageIdentifierRepository.identifyLanguage(text)
            val detectedLang =
                Language.fromBcp47Code(detectedCode) ?: Language.DEFAULT_TARGET_LANGUAGE

            // 2. Update state with the identified language


            // 3. Translate from detected language -> targetLanguage (Owner language)


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


            is MainScreenEvent.TranslationTextEdited -> {

            }

            else -> {}


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
