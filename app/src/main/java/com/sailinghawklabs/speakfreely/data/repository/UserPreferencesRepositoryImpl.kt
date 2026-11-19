package com.sailinghawklabs.speakfreely.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.sailinghawklabs.speakfreely.domain.model.ConversationMode
import com.sailinghawklabs.speakfreely.domain.model.Language
import com.sailinghawklabs.speakfreely.domain.model.UserPreferences
import com.sailinghawklabs.speakfreely.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class UserPreferencesRepositoryImpl(
    private val dataStore: DataStore<Preferences>
) : UserPreferencesRepository {

    private object PreferencesKeys {
        val OWNER_LANG_TAG = stringPreferencesKey("owner_language_tag")
        val TARGET_LANG_TAG = stringPreferencesKey("target_language_tag")
        val TTS_SPEECH_RATE = floatPreferencesKey("tts_speech_rate")
        val CONVERSATION_MODE = stringPreferencesKey("conversation_mode")
    }

    override fun observeUserPreferences(): Flow<UserPreferences> {
        return dataStore.data.map { prefs ->
            UserPreferences(
                ownerLanguage = Language.fromBcp47Code(
                    bcp47Code = prefs[PreferencesKeys.OWNER_LANG_TAG],
                    default = Language.DEFAULT_OWNER_LANGUAGE
                ),
                targetLanguage = Language.fromBcp47Code(
                    bcp47Code = prefs[PreferencesKeys.TARGET_LANG_TAG],
                    default = Language.DEFAULT_TARGET_LANGUAGE
                ),
                ttsSpeechRate = prefs[PreferencesKeys.TTS_SPEECH_RATE] ?: 1.0f,
                mode = prefs[PreferencesKeys.CONVERSATION_MODE]?.let { name ->
                    runCatching { ConversationMode.valueOf(name) }.getOrDefault(ConversationMode.DEFAULT)
                } ?: ConversationMode.DEFAULT
            )
        }
    }

    override suspend fun getUserPreferences(): UserPreferences {
        return observeUserPreferences().first()
    }

    override suspend fun setOwnerLanguage(language: Language) {
        dataStore.edit { prefs ->
            prefs[PreferencesKeys.OWNER_LANG_TAG] = language.bcp47Code
        }
    }

    override suspend fun setTargetLanguage(language: Language) {
        dataStore.edit { prefs ->
            prefs[PreferencesKeys.TARGET_LANG_TAG] = language.bcp47Code
        }
    }

    override suspend fun setTtsSpeechRate(rate: Float) {
        dataStore.edit { prefs ->
            prefs[PreferencesKeys.TTS_SPEECH_RATE] = rate
        }
    }

    override suspend fun setConversationMode(mode: ConversationMode) {
        dataStore.edit { prefs ->
            prefs[PreferencesKeys.CONVERSATION_MODE] = mode.name
        }
    }
}