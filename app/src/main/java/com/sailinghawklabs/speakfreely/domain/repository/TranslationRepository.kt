package com.sailinghawklabs.speakfreely.domain.repository

import com.sailinghawklabs.speakfreely.domain.model.Language

interface TranslationRepository {
    suspend fun translate(
        text: String,
        sourceLanguage: Language,
        targetLanguage: Language
    ): Result<String>
}