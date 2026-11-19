package com.sailinghawklabs.speakfreely.domain.model

data class UserPreferences(
    val ownerLanguage: Language = Language.DEFAULT_OWNER_LANGUAGE,
    val targetLanguage: Language = Language.DEFAULT_TARGET_LANGUAGE,
    val ttsSpeechRate: Float = 1.0f,
    val mode: ConversationMode = ConversationMode.DEFAULT
)

