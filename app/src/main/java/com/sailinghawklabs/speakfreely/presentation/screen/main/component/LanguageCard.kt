package com.sailinghawklabs.speakfreely.presentation.screen.main.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sailinghawklabs.speakfreely.domain.model.Language
import com.sailinghawklabs.speakfreely.presentation.screen.main.LanguageCardState
import com.sailinghawklabs.speakfreely.presentation.theme.AppTheme

@Composable
fun LanguageCard(
    cardState: LanguageCardState,
    showAutoDetectStatus: Boolean,
    onTextChanged: (String) -> Unit,
    onPlayClick: () -> Unit,
    onSpeedChange: (Float) -> Unit,
    onLanguageClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surface
) {
    val focusManager = LocalFocusManager.current

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 1. Header Row (Language Pill on Left, Split Play Button on Right)
            CardHeaderRow(
                language = cardState.language,
                showAutoDetectStatus = showAutoDetectStatus,
                isAutoDetectEnabled = cardState.isAutoDetectEnabled,
                playbackSpeed = cardState.playbackSpeed,
                onLanguageClick = onLanguageClick,
                onPlayClick = onPlayClick,
                onSpeedChange = onSpeedChange
            )

            // 2. Editable Text Field + Clear [ ✕ ] Button
            OutlinedTextField(
                value = cardState.text,
                onValueChange = { newText ->
                    val sanitized = newText.replace("\n", " ").replace("\r", " ")
                    onTextChanged(sanitized)
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                textStyle = MaterialTheme.typography.bodyLarge,
                placeholder = {
                    Text("Tap to edit text...")
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.clearFocus() }
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                ),
                trailingIcon = {
                    if (cardState.text.isNotEmpty()) {
                        IconButton(onClick = { onTextChanged("") }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear Text",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            )
        }
    }
}


@Composable
private fun CardHeaderRow(
    language: Language,
    showAutoDetectStatus: Boolean,
    isAutoDetectEnabled: Boolean,
    playbackSpeed: Float,
    onLanguageClick: () -> Unit,
    onPlayClick: () -> Unit,
    onSpeedChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val headerToolHeight = 30.dp
    val toolBgColor = MaterialTheme.colorScheme.primaryContainer
    val toolFgColor = MaterialTheme.colorScheme.onPrimaryContainer
    var showSpeedMenu by remember { mutableStateOf(false) }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. Language Control Pill
        Box(
            modifier = Modifier
                .height(headerToolHeight)
                .clip(CircleShape)
                .background(toolBgColor)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onLanguageClick
                )
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            val autoPrefix = if (showAutoDetectStatus) {
                if (isAutoDetectEnabled) "⚡ " else "🔒 "
            } else ""

            Text(
                text = "$autoPrefix${language.displayName} ${language.flagEmoji} ▼",
                style = MaterialTheme.typography.labelMedium,
                color = toolFgColor
            )
        }

        // 2. Split Play Button Pill (Matching Box Primitive)
        Box {
            Row(
                modifier = Modifier
                    .height(headerToolHeight)
                    .clip(CircleShape)
                    .background(toolBgColor),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Play Icon Box
                Box(
                    modifier = Modifier
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onPlayClick
                        )
                        .padding(horizontal = 14.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play",
                        tint = toolFgColor,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // 1dp Divider Line between Play and Arrow
                Box(
                    modifier = Modifier
                        .height(16.dp)
                        .width(1.dp)
                        .background(toolFgColor.copy(alpha = 0.3f))
                )

                // Arrow Dropdown Box
                Box(
                    modifier = Modifier
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { showSpeedMenu = true }
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "▼",
                        style = MaterialTheme.typography.labelSmall,
                        color = toolFgColor
                    )
                }
            }

            DropdownMenu(
                expanded = showSpeedMenu,
                onDismissRequest = { showSpeedMenu = false }
            ) {
                DropdownMenuItem(
                    text = {
                        SpeechSlider(
                            speechRate = playbackSpeed,
                            onSpeechRateChange = onSpeedChange,
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

@Preview
@Composable
private fun LanguageCardPrev() {
    AppTheme {
        LanguageCard(
            cardState = LanguageCardState(
                text = "Hola, como estas?",
                language = Language.DEFAULT_TARGET_LANGUAGE,
                playbackSpeed = 1.0f,
                isAutoDetectEnabled = true
            ),
            showAutoDetectStatus = true,
            onTextChanged = { },
            onPlayClick = { },
            onSpeedChange = { },
            onLanguageClick = {}
        )
    }
}
