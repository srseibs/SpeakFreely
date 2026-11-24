package com.sailinghawklabs.speakfreely.presentation.screen.main

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sailinghawklabs.speakfreely.presentation.screen.main.component.DirectionalFlowArrow
import com.sailinghawklabs.speakfreely.presentation.screen.main.component.LanguageCard
import com.sailinghawklabs.speakfreely.presentation.screen.main.component.LanguagePickerSheet
import com.sailinghawklabs.speakfreely.presentation.screen.main.component.MicBar
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
    var activeLanguagePickerTarget by remember { mutableStateOf<TalkerTarget?>(null) }

    activeLanguagePickerTarget?.let { target ->
        val cardState = if (target == TalkerTarget.TOP) state.topCard else state.bottomCard

        LanguagePickerSheet(
            title = if (target == TalkerTarget.TOP) "Select Foreign Language" else "Select Owner Language",
            selectedLanguage = cardState.language,
            showAutoDetectOption = target == TalkerTarget.TOP,
            isAutoDetectEnabled = cardState.isAutoDetectEnabled,
            onAutoDetectToggled = { enabled ->
                onEvent(MainScreenEvent.AutoDetectToggled(target, enabled))
            },
            onLanguageSelected = { newLanguage ->
                onEvent(MainScreenEvent.LanguageSelected(target, newLanguage))
            },
            onDismissRequest = { activeLanguagePickerTarget = null }
        )
    }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Speak Freely",
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontStyle = FontStyle.Italic,
                        fontWeight = FontWeight.Normal,
                        color = TextCream
                    )
                )
                IconButton(onClick = { onEvent(MainScreenEvent.GoToSettings) }) {
                    Icon(Icons.Default.Settings, contentDescription = "Settings")
                }
            }
        }
    ) { scaffoldPadding ->
        val focusManager = LocalFocusManager.current
        val scrollState = rememberScrollState()

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(scaffoldPadding)
                .pointerInput(Unit) {
                    detectTapGestures { focusManager.clearFocus() }
                }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .imePadding()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 1. Top Mic Bar (Foreigner)
                MicBar(
                    language = state.topCard.language,
                    isListening = state.isListening(TalkerTarget.TOP),
                    onMicClick = { onEvent(MainScreenEvent.MicButtonClicked(TalkerTarget.TOP)) }
                )

                // 2. Top Language Card (Foreign Language)
                LanguageCard(
                    cardState = state.topCard,
                    showAutoDetectStatus = true,
                    onFocus = { onEvent(MainScreenEvent.CardFocused(TalkerTarget.TOP)) },
                    onTextChanged = { text ->
                        onEvent(
                            MainScreenEvent.TextEdited(
                                TalkerTarget.TOP,
                                text
                            )
                        )
                    },
                    onPlayClick = { onEvent(MainScreenEvent.PlayClicked(TalkerTarget.TOP)) },
                    onSpeedChange = { speed ->
                        onEvent(
                            MainScreenEvent.SpeedChanged(
                                TalkerTarget.TOP,
                                speed
                            )
                        )
                    },
                    onLanguageClick = { activeLanguagePickerTarget = TalkerTarget.TOP },
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )

                // 3. Middle Directional Flow Arrow (180° Spring Rotation)
                DirectionalFlowArrow(
                    direction = state.translationDirection
                )

                // 4. Bottom Language Card (Owner Language)
                LanguageCard(
                    cardState = state.bottomCard,
                    showAutoDetectStatus = false,
                    onFocus = { onEvent(MainScreenEvent.CardFocused(TalkerTarget.BOTTOM)) },
                    onTextChanged = { text ->
                        onEvent(
                            MainScreenEvent.TextEdited(
                                TalkerTarget.BOTTOM,
                                text
                            )
                        )
                    },
                    onPlayClick = { onEvent(MainScreenEvent.PlayClicked(TalkerTarget.BOTTOM)) },
                    onSpeedChange = { speed ->
                        onEvent(
                            MainScreenEvent.SpeedChanged(
                                TalkerTarget.BOTTOM,
                                speed
                            )
                        )
                    },
                    onLanguageClick = { activeLanguagePickerTarget = TalkerTarget.BOTTOM },
                    containerColor = MaterialTheme.colorScheme.surface
                )

                // 5. Bottom Mic Bar (Owner)
                MicBar(
                    language = state.bottomCard.language,
                    isListening = state.isListening(TalkerTarget.BOTTOM),
                    onMicClick = { onEvent(MainScreenEvent.MicButtonClicked(TalkerTarget.BOTTOM)) }
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
            state = MainState(),
            onEvent = {}
        )
    }
}
