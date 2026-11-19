package com.sailinghawklabs.speakfreely.presentation.screen.splash

import com.sailinghawklabs.speakfreely.presentation.screen.util.UiText

data class AppSplashState(
    val statusLogs: List<UiText> = emptyList(),
    val isReady: Boolean = false,
    val isPermissionGranted: Boolean = false,
    val showPermissionRationale: Boolean = false,
    val isPermissionPermanentlyDenied: Boolean = false
)
