package com.sailinghawklabs.speakfreely.presentation.screen.main

import com.sailinghawklabs.speakfreely.domain.model.Language

data class LanguageCardState(
    val text: String = "",
    val language: Language,
    val playbackSpeed: Float = 1.0f,
    val isAutoDetectEnabled: Boolean = false
)

enum class TalkerTarget {
    TOP,    // Top Person Talking (Foreigner)
    BOTTOM, // Bottom Person Talking (Phone Owner)
    NONE    // Neither Person Talking (Mics Off)
}

enum class TranslationDirection {
    DOWN, // Data flows Top -> Bottom (↓ Arrow)
    UP,   // Data flows Bottom -> Top (↑ Arrow)
    IDLE  // Neutral / Both texts empty (⇅ Neutral Arrow)
}

data class MainState(
    val topCard: LanguageCardState = LanguageCardState(
        language = Language.DEFAULT_TARGET_LANGUAGE,
        isAutoDetectEnabled = true
    ),
    val bottomCard: LanguageCardState = LanguageCardState(
        language = Language.DEFAULT_OWNER_LANGUAGE,
        isAutoDetectEnabled = false
    ),
    val activeMic: TalkerTarget = TalkerTarget.NONE, // Non-nullable! NONE = Mics Off
    val translationDirection: TranslationDirection = TranslationDirection.IDLE
) {
    fun isListening(target: TalkerTarget): Boolean = activeMic == target

    val isSameLanguage: Boolean
        get() = topCard.language.bcp47Code == bottomCard.language.bcp47Code
}
