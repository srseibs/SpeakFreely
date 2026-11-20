package com.sailinghawklabs.speakfreely.domain.repository

import com.sailinghawklabs.speakfreely.domain.model.Language
import com.sailinghawklabs.speakfreely.domain.model.SpeechResult
import kotlinx.coroutines.flow.Flow

/**
 * Domain repository abstraction for native Android SpeechRecognizer engine.
 */
interface SttRepository {
    /**
     * Warms up and binds the native SpeechRecognizer engine.
     * @return True if SpeechRecognizer is installed and ready, False if unavailable on device.
     */
    suspend fun warmup(): Boolean

    /**
     * Checks if Speech-to-Text recognition service is available on the device.
     */
    fun isAvailable(): Boolean

    /**
     * Continuous reactive Flow stream of speech recognition state and transcripts.
     */
    fun observeSpeechResults(): Flow<SpeechResult>

    /**
     * Begins active microphone listening for speech in the specified [language].
     */
    fun startListening(language: Language)

    /**
     * Stops active microphone listening.
     */
    fun stopListening()

    /**
     * Releases SpeechRecognizer resources.
     */
    fun destroy()
}