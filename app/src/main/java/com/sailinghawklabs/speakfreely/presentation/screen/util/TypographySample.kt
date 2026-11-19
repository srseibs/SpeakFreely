package com.sailinghawklabs.speakfreely.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sailinghawklabs.speakfreely.presentation.theme.AppTheme

@Composable
fun TypographySamples(
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        TypographySample()
        TypographySample(FontStyle.Italic)
    }
}

@Composable
fun TypographySample(
    fontStyle: FontStyle = FontStyle.Normal
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "Display Large",
            style = MaterialTheme.typography.displayLarge,
            fontStyle = fontStyle
        )
        Text(
            text = "Display Medium",
            style = MaterialTheme.typography.displayMedium,
            fontStyle = fontStyle
        )
        Text(
            text = "Display Small",
            style = MaterialTheme.typography.displaySmall,
            fontStyle = fontStyle
        )

        Text(
            text = "Headline Large",
            style = MaterialTheme.typography.headlineLarge,
            fontStyle = fontStyle
        )
        Text(
            text = "Headline Medium",
            style = MaterialTheme.typography.headlineMedium,
            fontStyle = fontStyle
        )
        Text(
            text = "Headline Small",
            style = MaterialTheme.typography.headlineSmall,
            fontStyle = fontStyle
        )

        Text(
            text = "Title Large",
            style = MaterialTheme.typography.titleLarge,
            fontStyle = fontStyle
        )
        Text(
            text = "Title Medium",
            style = MaterialTheme.typography.titleMedium,
            fontStyle = fontStyle
        )
        Text(
            text = "Title Small",
            style = MaterialTheme.typography.titleSmall,
            fontStyle = fontStyle
        )

        Text(
            text = "Body Large",
            style = MaterialTheme.typography.bodyLarge,
            fontStyle = fontStyle
        )
        Text(
            text = "Body Medium",
            style = MaterialTheme.typography.bodyMedium,
            fontStyle = fontStyle
        )
        Text(
            text = "Body Small",
            style = MaterialTheme.typography.bodySmall,
            fontStyle = fontStyle
        )

        Text(
            text = "Label Large",
            style = MaterialTheme.typography.labelLarge,
            fontStyle = fontStyle
        )
        Text(
            text = "Label Medium",
            style = MaterialTheme.typography.labelMedium,
            fontStyle = fontStyle
        )
        Text(
            text = "Label Small",
            style = MaterialTheme.typography.labelSmall,
            fontStyle = fontStyle
        )
    }
}

@Preview
@Composable
private fun TypographySamplePrev() {
    AppTheme() {
        TypographySamples()
    }
}