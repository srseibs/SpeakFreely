package com.sailinghawklabs.speakfreely.presentation.screen.main

import com.sailinghawklabs.speakfreely.domain.model.Language

data class PanelState(
    val spokenText: String = "",
    val translatedText: String = "",
    val spokenLanguage: Language,           // Input language for this panel
    val targetLanguage: Language,           // Output translation language for this panel
    val isListening: Boolean = false,
    val isAutoDetectEnabled: Boolean = true,
    val spokenPlaybackSpeed: Float = 1.0f,    // Playback speed for Play Spoken
    val translatedPlaybackSpeed: Float = 1.0f // Playback speed for Play Translation
) {
    // Helper: returns true when spoken & target languages are identical (e.g. English -> English)
    val isSameLanguage: Boolean
        get() = spokenLanguage.bcp47Code.equals(targetLanguage.bcp47Code, ignoreCase = true)
}

data class MainState(
    val externalPanel: PanelState = PanelState(
        spokenLanguage = Language.DEFAULT_TARGET_LANGUAGE,  // Spanish (es-MX)
        targetLanguage = Language.DEFAULT_OWNER_LANGUAGE, // English (en-US)
        isAutoDetectEnabled = true                        // ⚡ AUTO ON for Foreigner
    ),
    val responsePanel: PanelState = PanelState(
        spokenLanguage = Language.DEFAULT_OWNER_LANGUAGE,  // English (en-US)
        targetLanguage = Language.DEFAULT_TARGET_LANGUAGE,// Spanish (es-MX)
        isAutoDetectEnabled = false                       // 🔒 AUTO OFF for Owner
    )
)