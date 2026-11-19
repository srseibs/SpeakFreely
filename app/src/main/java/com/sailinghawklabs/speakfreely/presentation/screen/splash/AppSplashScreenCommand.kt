package com.sailinghawklabs.speakfreely.presentation.screen.splash

// One-time commands from the ViewModel ---> to the Screen
sealed interface AppSplashScreenCommand {
    data object NavigateToMainScreen : AppSplashScreenCommand
}