package com.sailinghawklabs.speakfreely.presentation.screen.splash

// Events to the ViewModel <---- from the Screen
sealed interface AppSplashScreenEvent {
    data class TitleChanged(val string: String) : AppSplashScreenEvent
}