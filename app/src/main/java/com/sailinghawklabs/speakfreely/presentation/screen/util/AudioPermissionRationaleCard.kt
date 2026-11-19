package com.sailinghawklabs.speakfreely.presentation.screen.util

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AudioPermissionRationaleCard(
    isPermanentlyDenied: Boolean = false,
    onGrantClick: () -> Unit,
    onOpenSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = if (isPermanentlyDenied) "Enable Microphone in Settings" else "Microphone Access Required",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = if (isPermanentlyDenied) {
                    "Microphone access is turned off in device settings. Please enable microphone permission to use speech translation."
                } else {
                    "SpeakFreely needs microphone access to listen and translate spoken conversations in real time."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Button(
                onClick = if (isPermanentlyDenied) onOpenSettingsClick else onGrantClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isPermanentlyDenied) "Open Device Settings" else "Grant Microphone Permission")
            }
        }
    }
}
