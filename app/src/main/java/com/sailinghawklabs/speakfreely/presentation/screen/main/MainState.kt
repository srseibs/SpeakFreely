package com.sailinghawklabs.speakfreely.presentation.screen.main

import com.sailinghawklabs.speakfreely.domain.model.Language

data class LanguageCardState(
    val text: String = "",
    val language: Language,
    val playbackSpeed: Float = 1.0f,
    val isAutoDetectEnabled: Boolean = false
)

enum class CardPosition {
    TOP,   // Top Card & Top Mic (Foreigner)
    BOTTOM // Bottom Card & Bottom Mic (Phone Owner)
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
    val activeMic: CardPosition? = null
) {
    fun isListening(pos: CardPosition): Boolean = activeMic == pos

    val isSameLanguage: Boolean
        get() = topCard.language.bcp47Code == bottomCard.language.bcp47Code
}