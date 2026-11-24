package com.sailinghawklabs.speakfreely.presentation.screen.main

import com.sailinghawklabs.speakfreely.domain.model.Language

sealed interface MainScreenEvent {
    data object GoToSettings : MainScreenEvent

    // Mic Actions
    data class MicButtonClicked(val target: TalkerTarget) : MainScreenEvent

    // Card Actions
    data class TextEdited(val target: TalkerTarget, val newText: String) : MainScreenEvent
    data class PlayClicked(val target: TalkerTarget) : MainScreenEvent
    data class CardFocused(val target: TalkerTarget) : MainScreenEvent
    data class SpeedChanged(val target: TalkerTarget, val newSpeed: Float) : MainScreenEvent
    data class LanguageSelected(val target: TalkerTarget, val newLanguage: Language) :
        MainScreenEvent

    data class AutoDetectToggled(val target: TalkerTarget, val enabled: Boolean) : MainScreenEvent
}
