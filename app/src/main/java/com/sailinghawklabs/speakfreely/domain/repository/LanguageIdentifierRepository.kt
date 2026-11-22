package com.sailinghawklabs.speakfreely.domain.repository

interface LanguageIdentifierRepository {
    suspend fun identifyLanguage(text: String): String? // Returns BCP-47 tag e.g. "fr", "es", "de"
}
