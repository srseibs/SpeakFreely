package com.sailinghawklabs.speakfreely.presentation.screen.splash

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.nl.translate.TranslateRemoteModel
import com.sailinghawklabs.speakfreely.domain.repository.SttRepository
import com.sailinghawklabs.speakfreely.domain.repository.TtsRepository
import com.sailinghawklabs.speakfreely.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlin.time.Duration.Companion.milliseconds

class AppSplashViewModel(
    private val repository: UserPreferencesRepository,
    private val ttsRepository: TtsRepository,
    private val sttRepository: SttRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AppSplashState())

    val state = _state
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = AppSplashState()
        )

    init {
        getOneShotData()
    }

    private fun getOneShotData() {
        viewModelScope.launch {
            updateStatus("Loading preferences...", LogSeverity.INFO)
            preloadDataStore()

            updateStatus("Checking ML Kit models...", LogSeverity.INFO)
            getStatusOfML()

            updateStatus("Warming up TTS engine...", LogSeverity.INFO)
            warmUpTTS()

            // If permission is not yet granted, pause until isPermissionGranted becomes true
            if (!_state.value.isPermissionGranted) {
                updateStatus("⚠️ Microphone permission required.", LogSeverity.WARNING)
                _state.update {
                    it.copy(showPermissionRationale = true)
                }
                _state.first { it.isPermissionGranted }
            }

            updateStatus("Initializing Speech Recognizer...", LogSeverity.INFO)
            warmUpSpeechRecognizer()

            updateStatus("Ready!", LogSeverity.INFO)
            delay(300.milliseconds)

            _state.update { it.copy(isReady = true) }
            send(AppSplashScreenCommand.NavigateToMainScreen)
        }
    }

    private suspend fun updateStatus(
        message: String,
        severity: LogSeverity = LogSeverity.INFO
    ) {
        _state.update {
            it.copy(
                statusLogs = it.statusLogs + SplashLogEntry(
                    message = message,
                    severity = severity
                )
            )
        }

        val dwellDuration = when (severity) {
            LogSeverity.INFO -> 300.milliseconds
            LogSeverity.WARNING -> 1200.milliseconds
            LogSeverity.ERROR -> 1800.milliseconds
        }
        delay(dwellDuration)
    }

    private suspend fun preloadDataStore() {
        repository.getUserPreferences()
    }

    private suspend fun getStatusOfML() {
        try {
            val modelManager = RemoteModelManager.getInstance()
            val downloadedModels: Set<TranslateRemoteModel> = modelManager
                .getDownloadedModels(TranslateRemoteModel::class.java)
                .await()

            val downloadedCount = downloadedModels.size
            updateStatus("Offline ML Models: $downloadedCount available", LogSeverity.INFO)
            Log.i("Splash", "ML Model count: $downloadedCount")
        } catch (e: Exception) {
            updateStatus("⚠️ Checking ML Kit models failed", LogSeverity.WARNING)
        }
    }

    private suspend fun warmUpTTS() {
        val isTtsReady = ttsRepository.warmup()
        if (isTtsReady) {
            updateStatus("TTS Engine ready.", LogSeverity.INFO)
        } else {
            updateStatus("⚠️ TTS Engine unavailable (Audio replay disabled)", LogSeverity.WARNING)
        }
    }

    private suspend fun warmUpSpeechRecognizer() {
        val isSttReady = sttRepository.warmup()
        if (isSttReady) {
            updateStatus("Speech Recognizer ready.", LogSeverity.INFO)
        } else {
            updateStatus("⚠️ Speech Recognizer unavailable", LogSeverity.WARNING)
        }
    }

    private val _commands = Channel<AppSplashScreenCommand>()
    val commandsForScreen = _commands.receiveAsFlow()

    private fun send(cmd: AppSplashScreenCommand) {
        viewModelScope.launch { _commands.send(cmd) }
    }

    fun onEvent(event: AppSplashScreenEvent) {
        when (event) {
            is AppSplashScreenEvent.PermissionIsChecked -> {
                if (event.isGranted) {
                    _state.update {
                        it.copy(
                            isPermissionGranted = true,
                            showPermissionRationale = false,
                            isPermissionPermanentlyDenied = false
                        )
                    }
                } else {
                    _state.update {
                        it.copy(
                            isPermissionGranted = false,
                            showPermissionRationale = true,
                            isPermissionPermanentlyDenied = event.isPermanentlyDenied
                        )
                    }
                    viewModelScope.launch {
                        updateStatus(
                            if (event.isPermanentlyDenied) "⚠️ Microphone disabled in Settings."
                            else "⚠️ Microphone permission required.",
                            severity = LogSeverity.WARNING
                        )
                    }
                }
            }
        }
    }
}
