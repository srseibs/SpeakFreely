package com.sailinghawklabs.speakfreely.presentation.screen.main

// Events to the ViewModel <---- from the Screen
sealed interface MainScreenEvent {
    data object GoToSettings : MainScreenEvent
    data object PlayAudioClicked : MainScreenEvent
    data class PlaybackSpeedChanged(val newSpeed: Float) : MainScreenEvent
    data class InputTextEdited(val newText: String) : MainScreenEvent
    data class TranslationTextEdited(val newText: String) : MainScreenEvent
    data object ReverseTranslationClicked : MainScreenEvent

}