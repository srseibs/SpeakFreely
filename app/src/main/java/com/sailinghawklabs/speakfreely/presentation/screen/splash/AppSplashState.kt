package com.sailinghawklabs.speakfreely.presentation.screen.splash

import com.sailinghawklabs.speakfreely.presentation.screen.util.UiText

data class AppSplashState(
    val statusMessage: UiText? = null,
    val isReady: Boolean = false,
    val isPermissionGranted: Boolean = false,
    val showPermissionRationale: Boolean = false,
    val isPermissionPermanentlyDenied: Boolean = false
)
