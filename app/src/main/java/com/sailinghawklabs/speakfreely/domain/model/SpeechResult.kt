package com.sailinghawklabs.speakfreely.domain.model

/**
 * Speech-to-Text (STT) result stream emitted to ViewModels in real time.
 */
sealed interface SpeechResult {
    data object Idle : SpeechResult
    data object Listening : SpeechResult
    data class PartialResult(val text: String) : SpeechResult
    data class FinalResult(val text: String, val detectedLanguageCode: String? = null) :
        SpeechResult

    data class Error(val message: String) : SpeechResult
}