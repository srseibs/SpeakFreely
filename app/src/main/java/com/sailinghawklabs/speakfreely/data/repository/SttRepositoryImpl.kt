package com.sailinghawklabs.speakfreely.data.repository

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import com.sailinghawklabs.speakfreely.domain.model.Language
import com.sailinghawklabs.speakfreely.domain.model.SpeechResult
import com.sailinghawklabs.speakfreely.domain.repository.SttRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

class SttRepositoryImpl(
    private val context: Context
) : SttRepository, RecognitionListener {

    private var speechRecognizer: SpeechRecognizer? = null
    private val _speechResults = MutableStateFlow<SpeechResult>(SpeechResult.Idle)

    override fun isAvailable(): Boolean {
        return SpeechRecognizer.isRecognitionAvailable(context)
    }

    override suspend fun warmup(): Boolean = withContext(Dispatchers.Main) {
        if (!isAvailable()) return@withContext false
        if (speechRecognizer == null) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(this@SttRepositoryImpl)
            }
        }
        return@withContext speechRecognizer != null
    }

    override fun observeSpeechResults(): Flow<SpeechResult> {
        return _speechResults.asStateFlow()
    }

    override fun startListening(language: Language) {
        if (!isAvailable()) {
            _speechResults.value =
                SpeechResult.Error("Speech recognition unavailable on this device")
            return
        }

        if (speechRecognizer == null) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(this@SttRepositoryImpl)
            }
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, language.bcp47Code)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }

        _speechResults.value = SpeechResult.Listening
        speechRecognizer?.startListening(intent)
    }

    override fun stopListening() {
        speechRecognizer?.stopListening()
        _speechResults.value = SpeechResult.Idle
    }

    override fun destroy() {
        speechRecognizer?.destroy()
        speechRecognizer = null
        _speechResults.value = SpeechResult.Idle
    }

    // --- RecognitionListener Callbacks ---

    override fun onReadyForSpeech(params: Bundle?) {
        _speechResults.value = SpeechResult.Listening
    }

    override fun onBeginningOfSpeech() {}

    override fun onRmsChanged(rmsdB: Float) {}

    override fun onBufferReceived(buffer: ByteArray?) {}

    override fun onEndOfSpeech() {}

    override fun onError(error: Int) {
        val errorMessage = when (error) {
            SpeechRecognizer.ERROR_NO_MATCH -> "No speech detected"
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Speech timeout"
            SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
            SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network error"
            else -> "Speech recognition error ($error)"
        }
        _speechResults.value = SpeechResult.Error(errorMessage)
    }

    override fun onResults(results: Bundle?) {
        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        val finalTranscript = matches?.firstOrNull()
        if (!finalTranscript.isNullOrBlank()) {
            _speechResults.value = SpeechResult.FinalResult(text = finalTranscript)
        } else {
            _speechResults.value = SpeechResult.Idle
        }
    }

    override fun onPartialResults(partialResults: Bundle?) {
        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        val partialTranscript = matches?.firstOrNull()
        if (!partialTranscript.isNullOrBlank()) {
            _speechResults.value = SpeechResult.PartialResult(text = partialTranscript)
        }
    }

    override fun onEvent(eventType: Int, params: Bundle?) {}
}