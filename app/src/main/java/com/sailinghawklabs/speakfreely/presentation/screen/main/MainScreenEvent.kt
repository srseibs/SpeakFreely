package com.sailinghawklabs.speakfreely.presentation.screen.main

import com.sailinghawklabs.speakfreely.domain.model.Language

// Events from the Screen ---> to the ViewModel

enum class PanelTarget {
    EXTERNAL,
    RESPONSE
}

sealed interface MainScreenEvent {
    data object GoToSettings : MainScreenEvent

    // Panel Actions
    data class MicButtonClicked(val panel: PanelTarget) : MainScreenEvent
    data class PlaySpokenClicked(val panel: PanelTarget) : MainScreenEvent
    data class PlayTranslationClicked(val panel: PanelTarget) : MainScreenEvent

    data class SpokenTextEdited(val panel: PanelTarget, val newText: String) : MainScreenEvent
    data class TranslationTextEdited(val panel: PanelTarget, val newText: String) : MainScreenEvent

    data class SpokenSpeedChanged(val panel: PanelTarget, val newSpeed: Float) : MainScreenEvent
    data class TranslationSpeedChanged(val panel: PanelTarget, val newSpeed: Float) :
        MainScreenEvent

    data class LanguageSelected(val panel: PanelTarget, val newLanguage: Language) : MainScreenEvent
    data class AutoDetectToggled(val panel: PanelTarget, val enabled: Boolean) : MainScreenEvent
}