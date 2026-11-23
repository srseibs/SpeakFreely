package com.sailinghawklabs.speakfreely.presentation.screen.main

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
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
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sailinghawklabs.speakfreely.presentation.screen.main.component.ConversationPanel
import com.sailinghawklabs.speakfreely.presentation.screen.main.component.LanguagePickerSheet
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
    var activeLanguagePickerTarget by remember { mutableStateOf<Pair<PanelTarget, Boolean>?>(null) }
    // Pair(panelTarget, isSpokenLanguage)

    activeLanguagePickerTarget?.let { (panel, isSpoken) ->
        val selectedLang = if (isSpoken) {
            if (panel == PanelTarget.EXTERNAL)
                state.externalPanel.spokenLanguage
            else
                state.responsePanel.spokenLanguage
        } else {
            if (panel == PanelTarget.EXTERNAL)
                state.externalPanel.targetLanguage
            else
                state.responsePanel.targetLanguage
        }

        val isAutoEnabled = if (panel == PanelTarget.EXTERNAL)
            state.externalPanel.isAutoDetectEnabled
        else
            state.responsePanel.isAutoDetectEnabled

        LanguagePickerSheet(
            title = if (isSpoken) "Select Spoken Language" else "Select Translation Language",
            selectedLanguage = selectedLang,
            showAutoDetectOption = isSpoken,
            isAutoDetectEnabled = isAutoEnabled,
            onAutoDetectToggled = { enabled ->
                onEvent(MainScreenEvent.AutoDetectToggled(panel, enabled))
            },
            onLanguageSelected = { newLanguage ->
                if (isSpoken) {
                    onEvent(MainScreenEvent.SpokenLanguageSelected(panel, newLanguage))
                } else {
                    onEvent(MainScreenEvent.TargetLanguageSelected(panel, newLanguage))
                }
            },
            onDismissRequest = { activeLanguagePickerTarget = null }
        )
    }

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
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                // 1. Panel 1: EXTERNAL (Foreign Speaker)
                ConversationPanel(
                    panelTitle = "THEY TALK",
                    panelState = state.externalPanel,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    onMicClick = { onEvent(MainScreenEvent.MicButtonClicked(PanelTarget.EXTERNAL)) },
                    onPlaySpokenClick = { onEvent(MainScreenEvent.PlaySpokenClicked(PanelTarget.EXTERNAL)) },
                    onPlayTranslationClick = {
                        onEvent(
                            MainScreenEvent.PlayTranslationClicked(
                                PanelTarget.EXTERNAL
                            )
                        )
                    },
                    onSpokenTextChange = { text ->
                        onEvent(
                            MainScreenEvent.SpokenTextEdited(
                                PanelTarget.EXTERNAL,
                                text
                            )
                        )
                    },
                    onTranslationTextChange = { text ->
                        onEvent(
                            MainScreenEvent.TranslationTextEdited(
                                PanelTarget.EXTERNAL,
                                text
                            )
                        )
                    },
                    onSpokenSpeedChange = { speed ->
                        onEvent(
                            MainScreenEvent.SpokenSpeedChanged(
                                PanelTarget.EXTERNAL,
                                speed
                            )
                        )
                    },
                    onTranslationSpeedChange = { speed ->
                        onEvent(
                            MainScreenEvent.TranslationSpeedChanged(
                                PanelTarget.EXTERNAL,
                                speed
                            )
                        )
                    },
                    onSpokenLanguageClick = {
                        activeLanguagePickerTarget = Pair(PanelTarget.EXTERNAL, true)
                    },
                    onTranslationLanguageClick = {
                        activeLanguagePickerTarget = Pair(PanelTarget.EXTERNAL, false)
                    }
                )

                // 2. Panel 2: RESPONSE (Phone Owner)
                ConversationPanel(
                    panelTitle = "I TALK",
                    panelState = state.responsePanel,
                    containerColor = MaterialTheme.colorScheme.surface,
                    onMicClick = { onEvent(MainScreenEvent.MicButtonClicked(PanelTarget.RESPONSE)) },
                    onPlaySpokenClick = { onEvent(MainScreenEvent.PlaySpokenClicked(PanelTarget.RESPONSE)) },
                    onPlayTranslationClick = {
                        onEvent(
                            MainScreenEvent.PlayTranslationClicked(
                                PanelTarget.RESPONSE
                            )
                        )
                    },
                    onSpokenTextChange = { text ->
                        onEvent(
                            MainScreenEvent.SpokenTextEdited(
                                PanelTarget.RESPONSE,
                                text
                            )
                        )
                    },
                    onTranslationTextChange = { text ->
                        onEvent(
                            MainScreenEvent.TranslationTextEdited(
                                PanelTarget.RESPONSE,
                                text
                            )
                        )
                    },
                    onSpokenSpeedChange = { speed ->
                        onEvent(
                            MainScreenEvent.SpokenSpeedChanged(
                                PanelTarget.RESPONSE,
                                speed
                            )
                        )
                    },
                    onTranslationSpeedChange = { speed ->
                        onEvent(
                            MainScreenEvent.TranslationSpeedChanged(
                                PanelTarget.RESPONSE,
                                speed
                            )
                        )
                    },
                    onSpokenLanguageClick = {
                        activeLanguagePickerTarget = Pair(PanelTarget.RESPONSE, true)
                    },
                    onTranslationLanguageClick = {
                        activeLanguagePickerTarget = Pair(PanelTarget.RESPONSE, false)
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

            ),
            onEvent = {}
        )
    }
}
