package com.sailinghawklabs.speakfreely.data.repository

import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import com.sailinghawklabs.speakfreely.domain.model.Language
import com.sailinghawklabs.speakfreely.domain.repository.TranslationRepository
import kotlinx.coroutines.tasks.await

class MlKitTranslationRepositoryImpl : TranslationRepository {
    override suspend fun translate(
        text: String,
        sourceLanguage: Language,
        targetLanguage: Language
    ): Result<String> {
        if (text.isBlank()) return Result.success("")

        return runCatching {
            val sourceCode = TranslateLanguage.fromLanguageTag(sourceLanguage.bcp47Code)
                ?: sourceLanguage.bcp47Code.substringBefore("-")
            val targetCode = TranslateLanguage.fromLanguageTag(targetLanguage.bcp47Code)
                ?: targetLanguage.bcp47Code.substringBefore("-")

            val options = TranslatorOptions.Builder()
                .setSourceLanguage(sourceCode)
                .setTargetLanguage(targetCode)
                .build()

            val translator = Translation.getClient(options)

            // Downloads model automatically if missing (~30 MB)
            translator.downloadModelIfNeeded().await()

            // On-device ML Kit translation
            val translatedResult = translator.translate(text).await()
            translator.close()

            translatedResult
        }

    }
}