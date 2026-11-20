package com.sailinghawklabs.speakfreely.presentation.screen.splash

data class AppSplashState(
    val statusLogs: List<SplashLogEntry> = emptyList(),
    val isReady: Boolean = false,
    val isPermissionGranted: Boolean = false,
    val showPermissionRationale: Boolean = false,
    val isPermissionPermanentlyDenied: Boolean = false
)
