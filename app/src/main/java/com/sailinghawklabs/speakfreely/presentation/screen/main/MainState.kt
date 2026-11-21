package com.sailinghawklabs.speakfreely.presentation.screen.main

import com.sailinghawklabs.speakfreely.domain.model.Language

data class MainState(
    val inputText: String = "",
    val translatedText: String = "",
    val detectedLanguage: Language? = null, // null = No speech detected yet!
    val targetLanguage: Language = Language.DEFAULT_OWNER_LANGUAGE,
    val ownerLanguage: Language = Language.DEFAULT_OWNER_LANGUAGE,
    val playbackSpeed: Float = 1F,
    val isListening: Boolean = false

) {
    /**
     * Show the Reversal Button ONLY after foreign speech has been detected
     * AND the detected language is different from the active target language.
     */
    val showReversalButton: Boolean
        get() = detectedLanguage != null && detectedLanguage != targetLanguage
}