package com.sailinghawklabs.speakfreely.data.repository

import com.google.mlkit.nl.languageid.LanguageIdentification
import com.sailinghawklabs.speakfreely.domain.repository.LanguageIdentifierRepository
import kotlinx.coroutines.tasks.await

class MlKitLanguageIdentifierRepositoryImpl : LanguageIdentifierRepository {
    override suspend fun identifyLanguage(text: String): String? {
        if (text.isBlank()) return null
        return runCatching {
            val languageIdentifierClient = LanguageIdentification.getClient()
            val languageCode = languageIdentifierClient.identifyLanguage(text).await()
            languageIdentifierClient.close()
            if (languageCode == "und") null else languageCode
        }.getOrNull()
    }
}