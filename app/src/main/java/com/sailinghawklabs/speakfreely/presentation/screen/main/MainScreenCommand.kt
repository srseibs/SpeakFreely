package com.sailinghawklabs.speakfreely.presentation.screen

// One-time commands from the ViewModel ---> to the Screen
sealed interface MainScreenCommand {
    data object NavigateToSettings : MainScreenCommand
}