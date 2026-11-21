package com.sailinghawklabs.speakfreely.presentation.screen.main

import com.sailinghawklabs.speakfreely.domain.model.Language

data class MainState(
    val inputText: String = "",
    val inputLanguage: Language = Language.DEFAULT_TARGET_LANGUAGE,
    val translatedText: String = "",
    val targetLanguage: Language = Language.DEFAULT_OWNER_LANGUAGE

)