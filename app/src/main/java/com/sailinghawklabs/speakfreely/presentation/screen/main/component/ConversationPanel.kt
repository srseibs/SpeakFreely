package com.sailinghawklabs.speakfreely.presentation.screen.main.component

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sailinghawklabs.speakfreely.domain.model.Language
import com.sailinghawklabs.speakfreely.presentation.screen.main.PanelState
import com.sailinghawklabs.speakfreely.presentation.theme.AppTheme

@Composable
fun ConversationPanel(
    panelTitle: String,
    panelState: PanelState,
    onMicClick: () -> Unit,
    onPlaySpokenClick: () -> Unit,
    onPlayTranslationClick: () -> Unit,
    onSpokenTextChange: (String) -> Unit,
    onTranslationTextChange: (String) -> Unit,
    onSpokenSpeedChange: (Float) -> Unit,
    onTranslationSpeedChange: (Float) -> Unit,
    onSpokenLanguageClick: () -> Unit = {},
    onTranslationLanguageClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surface
) {
    var showSpokenSpeedMenu by remember { mutableStateOf(false) }
    var showTranslationSpeedMenu by remember { mutableStateOf(false) }

    // Bold Studio Red Pulse Animation for active recording
    val infiniteTransition = rememberInfiniteTransition(label = "RecPulse")
    val recButtonColor by infiniteTransition.animateColor(
        initialValue = Color(0xFFD32F2F),
        targetValue = Color(0xFF5B0D0A),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "RecButtonColor"
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // 1. Header Row (Panel Title + Top-Right Mic Record Button)
            // ============================================================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = panelTitle,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                // Top-Right Mic Record Button
                Button(
                    onClick = onMicClick,
                    modifier = Modifier.height(30.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (panelState.isListening)
                            recButtonColor
                        else
                            MaterialTheme.colorScheme.primary
                    )
                ) {
                    if (panelState.isListening) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "REC",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Record",
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Record",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // 2. Spoken Section (Shaded Inner Surface Box)
            // ============================================================
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceContainer.copy(0.4f)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Spoken Header Row (Label + Language Pill on Left, Play Button on Right!)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Spoken:",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondary
                            )

                            // Spoken Language Control Pill
                            Surface(
                                onClick = onSpokenLanguageClick,
                                modifier = Modifier.height(30.dp),
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer,
                                tonalElevation = 3.dp
                            ) {
                                val spokenLanguage = panelState.spokenLanguage
                                Box(
                                    modifier = Modifier.padding(horizontal = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${if (panelState.isAutoDetectEnabled) "⚡" else "🔒"} ${spokenLanguage.displayName} ${spokenLanguage.flagEmoji} ▼",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }

                        // Right-Aligned Spoken Section Split Play Button + Speed Dropdown
                        Box {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Button(
                                    onClick = onPlaySpokenClick,
                                    modifier = Modifier.height(30.dp),
                                    contentPadding = PaddingValues(
                                        horizontal = 14.dp,
                                        vertical = 0.dp
                                    ),
                                    shape = RoundedCornerShape(
                                        topStart = 18.dp,
                                        bottomStart = 18.dp,
                                        topEnd = 0.dp,
                                        bottomEnd = 0.dp
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Play Spoken"
                                    )
                                }

                                Button(
                                    onClick = { showSpokenSpeedMenu = true },
                                    modifier = Modifier
                                        .padding(start = 1.dp)
                                        .height(30.dp),
                                    shape = RoundedCornerShape(
                                        topStart = 0.dp,
                                        bottomStart = 0.dp,
                                        topEnd = 18.dp,
                                        bottomEnd = 18.dp
                                    ),
                                    contentPadding = PaddingValues(
                                        horizontal = 10.dp,
                                        vertical = 0.dp
                                    )
                                ) {
                                    Text("▼", style = MaterialTheme.typography.labelSmall)
                                }
                            }

                            DropdownMenu(
                                expanded = showSpokenSpeedMenu,
                                onDismissRequest = { showSpokenSpeedMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = {
                                        SpeechSlider(
                                            speechRate = panelState.spokenPlaybackSpeed,
                                            onSpeechRateChange = onSpokenSpeedChange,
                                            modifier = Modifier
                                                .width(280.dp)
                                                .padding(8.dp)
                                        )
                                    },
                                    onClick = {}
                                )
                            }
                        }
                    }

                    // Spoken Text Field
                    OutlinedTextField(
                        value = panelState.spokenText,
                        onValueChange = { newText ->
                            val sanitized = newText.replace("\n", " ").replace("\r", " ")
                            onSpokenTextChange(sanitized)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        textStyle = MaterialTheme.typography.bodyLarge,
                        placeholder = { Text("Tap to edit spoken text...") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        ),
                        trailingIcon = {
                            if (panelState.spokenText.isNotEmpty()) {
                                IconButton(onClick = { onSpokenTextChange("") }) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Clear Spoken Text",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // 3. Translation Section Header (ALWAYS VISIBLE so target language can be changed anytime!)
            // ============================================================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Translation:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )

                    // Translation Language Control Pill (Always visible!)
                    Surface(
                        onClick = onTranslationLanguageClick,
                        modifier = Modifier.height(30.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        tonalElevation = 3.dp
                    ) {
                        val targetLanguage = panelState.targetLanguage
                        Box(
                            modifier = Modifier.padding(horizontal = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${targetLanguage.displayName} ${targetLanguage.flagEmoji} ▼",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }

            // 4. Translation Text & Play Area (Only shown if languages are DIFFERENT!)
            // ============================================================
            if (!panelState.isSameLanguage) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceContainer.copy(0.4f)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Translation Text Field
                        OutlinedTextField(
                            value = panelState.translatedText,
                            onValueChange = { newText ->
                                val sanitized = newText.replace("\n", " ").replace("\r", " ")
                                onTranslationTextChange(sanitized)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            textStyle = MaterialTheme.typography.bodyLarge,
                            placeholder = { Text("Translation...") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = Color.Transparent,
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent
                            ),
                            trailingIcon = {
                                if (panelState.translatedText.isNotEmpty()) {
                                    IconButton(onClick = { onTranslationTextChange("") }) {
                                        Icon(
                                            imageVector = Icons.Default.Clear,
                                            contentDescription = "Clear Translation Text",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                                alpha = 0.6f
                                            )
                                        )
                                    }
                                }
                            }
                        )

                        // Translation Section Split Play Button + Speed Dropdown
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Box {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Button(
                                        onClick = onPlayTranslationClick,
                                        modifier = Modifier.height(30.dp),
                                        contentPadding = PaddingValues(
                                            horizontal = 14.dp,
                                            vertical = 0.dp
                                        ),
                                        shape = RoundedCornerShape(
                                            topStart = 18.dp,
                                            bottomStart = 18.dp,
                                            topEnd = 0.dp,
                                            bottomEnd = 0.dp
                                        )
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PlayArrow,
                                            contentDescription = "Play Translation"
                                        )
                                    }

                                    Button(
                                        onClick = { showTranslationSpeedMenu = true },
                                        modifier = Modifier
                                            .padding(start = 1.dp)
                                            .height(30.dp),
                                        shape = RoundedCornerShape(
                                            topStart = 0.dp,
                                            bottomStart = 0.dp,
                                            topEnd = 18.dp,
                                            bottomEnd = 18.dp
                                        ),
                                        contentPadding = PaddingValues(
                                            horizontal = 10.dp,
                                            vertical = 0.dp
                                        )
                                    ) {
                                        Text("▼", style = MaterialTheme.typography.labelSmall)
                                    }
                                }

                                DropdownMenu(
                                    expanded = showTranslationSpeedMenu,
                                    onDismissRequest = { showTranslationSpeedMenu = false }
                                ) {
                                    DropdownMenuItem(
                                        text = {
                                            SpeechSlider(
                                                speechRate = panelState.translatedPlaybackSpeed,
                                                onSpeechRateChange = onTranslationSpeedChange,
                                                modifier = Modifier
                                                    .width(280.dp)
                                                    .padding(8.dp)
                                            )
                                        },
                                        onClick = {}
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun ConversationPanelPrev() {
    var isListening by remember { mutableStateOf(false) }
    var spokenSpeed by remember { mutableFloatStateOf(1.20f) }
    var translationSpeed by remember { mutableFloatStateOf(1.0f) }

    val panelState = remember(isListening, spokenSpeed, translationSpeed) {
        PanelState(
            spokenText = "¿Dónde está la estación de tren más cercana?",
            translatedText = "Where is the nearest train station?",
            spokenLanguage = Language.DEFAULT_TARGET_LANGUAGE,
            targetLanguage = Language.DEFAULT_OWNER_LANGUAGE,
            spokenPlaybackSpeed = spokenSpeed,
            translatedPlaybackSpeed = translationSpeed,
            isListening = isListening,
            isAutoDetectEnabled = true
        )
    }

    AppTheme {
        ConversationPanel(
            panelTitle = "EXTERNAL",
            panelState = panelState,
            onMicClick = { isListening = !isListening },
            onPlaySpokenClick = {},
            onPlayTranslationClick = {},
            onSpokenTextChange = {},
            onSpokenSpeedChange = { spokenSpeed = it },
            onTranslationSpeedChange = { translationSpeed = it },
            onTranslationTextChange = {}
        )
    }
}
