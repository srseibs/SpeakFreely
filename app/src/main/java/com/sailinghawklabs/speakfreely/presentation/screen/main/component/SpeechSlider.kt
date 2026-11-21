package com.sailinghawklabs.speakfreely.presentation.screen.main.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sailinghawklabs.speakfreely.presentation.theme.AppTheme
import kotlin.math.roundToInt

@Composable
fun SpeechSlider(
    speechRate: Float, // Internal rate: 0.5f .. 1.5f
    onSpeechRateChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    // 1. Map 0.5f..1.5f -> -50% .. +50%
    val percentOffset = ((speechRate.coerceIn(0.5f, 1.5f) - 1.0f) * 100f).roundToInt()
    val formattedText = if (percentOffset > 0) "+$percentOffset%" else "$percentOffset%"

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(30.dp)
            .padding(2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Speed: $formattedText",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(85.dp)
        )

        Slider(
            value = percentOffset.toFloat(),
            onValueChange = { newPercent ->
                // 2. Map -50% .. +50% back to 0.5f .. 1.5f
                val newRate = 1.0f + (newPercent / 100f)
                onSpeechRateChange(newRate)
            },
            valueRange = -50f..50f,
            steps = 9, // Snaps in 10% steps: -50%, -40%, ... 0%, ... +50%
            modifier = Modifier.weight(1f)
        )
    }
}

@Preview
@Composable
private fun SpeechSliderPrev() {
    AppTheme {
        var speechRate by remember { mutableFloatStateOf(1.0f) }
        SpeechSlider(
            speechRate = speechRate,
            onSpeechRateChange = { speechRate = it }
        )
    }
}