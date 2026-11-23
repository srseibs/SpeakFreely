package com.sailinghawklabs.speakfreely.presentation.screen.main

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
        val focusManager = LocalFocusManager.current
        val scrollState = rememberScrollState()

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(scaffoldPadding)
                .systemBarsPadding()
                .pointerInput(Unit) {
                    detectTapGestures { focusManager.clearFocus() }
                }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .imePadding()
                    .verticalScroll(scrollState)
                    .padding(24.dp),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

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
