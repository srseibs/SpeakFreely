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

        // 1. Find local voices matching the exact language + country/region (e.g. en-US or es-MX)
        val exactDialectVoices = tts?.voices?.filter { voice ->
            !voice.isNetworkConnectionRequired &&
                    voice.locale.language == locale.language &&
                    (locale.country.isBlank() || voice.locale.country.equals(
                        locale.country,
                        ignoreCase = true
                    ))
        }

        // 2. Pick exact regional voice, or fallback to any local voice in that language
        val bestVoice = exactDialectVoices?.firstOrNull()
            ?: tts?.voices?.firstOrNull { !it.isNetworkConnectionRequired && it.locale.language == locale.language }

        if (bestVoice != null) {
            println("TTS Selected Regional Voice: name=${bestVoice.name}, locale=${bestVoice.locale}")
            tts?.voice = bestVoice
        }

        println("TTS Speech Rate set to: $speechRate")
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
