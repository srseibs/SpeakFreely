package com.sailinghawklabs.speakfreely.presentation.screen.splash

enum class LogSeverity {
    INFO,
    WARNING,
    ERROR
}

data class SplashLogEntry(
    val message: String,
    val severity: LogSeverity = LogSeverity.INFO
)
