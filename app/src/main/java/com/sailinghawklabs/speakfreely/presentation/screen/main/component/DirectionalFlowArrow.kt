package com.sailinghawklabs.speakfreely.presentation.screen.main.component

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sailinghawklabs.speakfreely.presentation.screen.main.TranslationDirection
import com.sailinghawklabs.speakfreely.presentation.theme.AppTheme
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

@Composable
fun DirectionalFlowArrow(
    modifier: Modifier = Modifier,
    direction: TranslationDirection
) {
    val targetDegrees = when (direction) {
        TranslationDirection.DOWN -> 180f  // Points DOWN (↓) when translating Top -> Bottom
        TranslationDirection.UP -> 0f      // Points UP (↑) when translating Bottom -> Top
        TranslationDirection.IDLE -> 0f
    }

    val animatedDegrees by animateFloatAsState(
        targetValue = targetDegrees,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "ArrowRotation"
    )

    val icon = if (direction == TranslationDirection.IDLE) {
        Icons.Default.SwapVert // Neutral Bidirectional ⇅ Icon!
    } else {
        Icons.Default.ArrowUpward // Unidirectional Arrow Icon!
    }

    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = "Data Flow Direction",
            modifier = Modifier
                .graphicsLayer { rotationZ = animatedDegrees }
                .size(32.dp),
            tint = if (direction != TranslationDirection.IDLE)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
    }
}

@Preview
@Composable
private fun DirectionalFlowArrowPrev() {
    val directions = listOf(
        TranslationDirection.DOWN,
        TranslationDirection.IDLE,
        TranslationDirection.UP,
        TranslationDirection.IDLE
    )
    var currentIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1.5.seconds)
            currentIndex = (currentIndex + 1) % directions.size
        }
    }

    AppTheme {
        DirectionalFlowArrow(
            direction = directions[currentIndex]
        )
    }
}
