package com.sailinghawklabs.speakfreely.domain.repository

import com.sailinghawklabs.speakfreely.domain.model.Language

interface TtsRepository {
    suspend fun warmup(): Boolean
    fun speak(text: String, language: Language, speechRate: Float = 1.0f)
    fun stop()
    fun shutdown()
}
