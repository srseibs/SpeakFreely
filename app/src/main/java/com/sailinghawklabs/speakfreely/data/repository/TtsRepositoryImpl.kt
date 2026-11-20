package com.sailinghawklabs.speakfreely.data.repository

import android.content.Context
import android.speech.tts.TextToSpeech
import com.sailinghawklabs.speakfreely.domain.model.Language
import com.sailinghawklabs.speakfreely.domain.repository.TtsRepository
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale
import kotlin.coroutines.resume

class TtsRepositoryImpl(
    private val context: Context
) : TtsRepository {

    private var tts: TextToSpeech? = null
    private var isInitialized = false

    override suspend fun warmup(): Boolean = suspendCancellableCoroutine { continuation ->
        if (isInitialized && tts != null) {
            if (continuation.isActive) continuation.resume(true)
            return@suspendCancellableCoroutine
        }

        tts = TextToSpeech(context) { status ->
            val success = (status == TextToSpeech.SUCCESS)
            isInitialized = success
            if (continuation.isActive) {
                continuation.resume(success)
            }
        }
    }

    override fun speak(
        text: String,
        language: Language,
        speechRate: Float
    ) {
        if (!isInitialized || tts == null) return
        val locale = Locale.forLanguageTag(language.bcp47Code)
        tts?.language = locale
        tts?.setSpeechRate(speechRate)
        tts?.speak(text as CharSequence, TextToSpeech.QUEUE_FLUSH, null, "speak_freely_tts")
    }

    override fun stop() {
        tts?.stop()
    }

    override fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        isInitialized = false
    }
}
