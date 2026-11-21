package com.sailinghawklabs.speakfreely.presentation.screen.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sailinghawklabs.speakfreely.presentation.screen.main.component.ReversalSection
import com.sailinghawklabs.speakfreely.presentation.screen.main.component.SpeechCard
import com.sailinghawklabs.speakfreely.presentation.screen.main.component.SpeechSlider
import com.sailinghawklabs.speakfreely.presentation.screen.util.ObserveAsCommand
import com.sailinghawklabs.speakfreely.presentation.theme.AppTheme
import com.sailinghawklabs.speakfreely.presentation.theme.TextCream
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun MainScreen(
    viewModel: MainViewModel = koinViewModel(),
    onGotoSettings: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    MainScreenContent(
        state = state,
        onEvent = viewModel::onEvent
    )

    ObserveAsCommand(flow = viewModel.commandsForScreen) { command ->
        when (command) {
            MainScreenCommand.NavigateToSettings -> onGotoSettings()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreenContent(
    state: MainState,
    onEvent: (MainScreenEvent) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Speak Freely",
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontStyle = FontStyle.Italic,
                            fontWeight = FontWeight.Normal,
                            color = TextCream
                        ),
                    )
                },
                actions = {
                    IconButton(
                        onClick = { onEvent(MainScreenEvent.GoToSettings) }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings"
                        )
                    }
                }
            )
        }
    ) { scaffoldPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(scaffoldPadding)
                .systemBarsPadding()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 1. Top Card (INPUT)
                val inputSubtitle = when {
                    state.isListening -> "Listening..."
                    state.detectedLanguage != null ->
                        "Detected: ${state.detectedLanguage.displayName} ${state.detectedLanguage.flagEmoji}"

                    else -> "Tap Record to speak"
                }

                SpeechCard(
                    headerTitle = "INPUT",
                    headerSubtitle = inputSubtitle,
                    text = state.inputText,
                    onTextChange = { text -> onEvent(MainScreenEvent.InputTextEdited(text)) },
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )

                Spacer(modifier = Modifier.height(32.dp))

                // 2. Middle Dynamic Reversal Button
                if (state.showReversalButton && state.detectedLanguage != null) {
                    ReversalSection(
                        translationLanguage = state.detectedLanguage,
                        onReverseClicked = { onEvent(MainScreenEvent.ReverseTranslationClicked) }
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                }

                // 3. Bottom Card (TRANSLATION)
                SpeechCard(
                    headerTitle = "TRANSLATION",
                    headerSubtitle = "${state.targetLanguage.displayName} ${state.targetLanguage.flagEmoji}",
                    text = state.translatedText,
                    onTextChange = { text -> onEvent(MainScreenEvent.TranslationTextEdited(text)) },
                    containerColor = MaterialTheme.colorScheme.surface,
                    bottomActions = {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(onClick = { onEvent(MainScreenEvent.PlayAudioClicked) }) {
                                Text("▶ Play Audio")
                            }

                            SpeechSlider(
                                speechRate = state.playbackSpeed,
                                onSpeechRateChange = { rate ->
                                    onEvent(MainScreenEvent.PlaybackSpeedChanged(rate))
                                }
                            )
                        }
                    }
                )
            }
        }
    }
}

@Preview
@Composable
private fun Preview() {
    AppTheme {
        MainScreenContent(
            state = MainState(
                inputText = "¿Dónde está la estación de tren más cercana?",
                translatedText = "Where is the nearest train station?"
            ),
            onEvent = {}
        )
    }
}
