package com.sailinghawklabs.speakfreely.presentation.screen.splash

import com.sailinghawklabs.speakfreely.presentation.screen.util.UiText

enum class LogSeverity {
    INFO,
    WARNING,
    ERROR
}

data class SplashLogEntry(
    val text: UiText,
    val severity: LogSeverity = LogSeverity.INFO
)
