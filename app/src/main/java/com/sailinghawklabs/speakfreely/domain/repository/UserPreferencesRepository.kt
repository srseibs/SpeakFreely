package com.sailinghawklabs.speakfreely.domain.repository

import com.sailinghawklabs.speakfreely.domain.model.ConversationMode
import com.sailinghawklabs.speakfreely.domain.model.Language
import com.sailinghawklabs.speakfreely.domain.model.UserPreferences
import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {
    fun observeUserPreferences(): Flow<UserPreferences>
    suspend fun getUserPreferences(): UserPreferences
    suspend fun setOwnerLanguage(language: Language)
    suspend fun setTargetLanguage(language: Language)
    suspend fun setTtsSpeechRate(rate: Float)
    suspend fun setConversationMode(mode: ConversationMode)
}