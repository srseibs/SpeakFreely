package com.sailinghawklabs.speakfreely.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

private val DarkColorScheme = darkColorScheme(
    primary = CopperPrimary,
    onPrimary = CopperOnPrimary,
    primaryContainer = CopperPrimaryContainer,
    onPrimaryContainer = CopperOnPrimaryContainer,

    secondary = AmberSecondary,
    onSecondary = AmberOnSecondary,
    secondaryContainer = AmberSecondaryContainer,
    onSecondaryContainer = AmberOnSecondaryContainer,

    tertiary = RubyTertiary,
    onTertiary = RubyOnTertiary,
    tertiaryContainer = RubyTertiaryContainer,
    onTertiaryContainer = RubyOnTertiaryContainer,

    background = CharcoalBackground,
    onBackground = CharcoalOnBackground,

    surface = MahoganySurface,
    onSurface = MahoganyOnSurface,
    surfaceVariant = MahoganySurfaceVariant,
    onSurfaceVariant = MahoganyOnSurfaceVariant,

    outline = CopperOutline,
    outlineVariant = CopperOutlineVariant,

    error = ErrorRed,
    onError = OnErrorRed,
    errorContainer = ErrorContainerRed,
    onErrorContainer = OnErrorContainerRed,
)

private val LightColorScheme = lightColorScheme(
    primary = CopperPrimary,
    onPrimary = CopperOnPrimary,
    primaryContainer = CopperOnPrimaryContainer,
    onPrimaryContainer = CopperPrimaryContainer,

    secondary = AmberSecondary,
    onSecondary = AmberOnSecondary,
    secondaryContainer = AmberOnSecondaryContainer,
    onSecondaryContainer = AmberSecondaryContainer,

    background = CharcoalOnBackground,
    onBackground = CharcoalBackground,

    surface = MahoganyOnSurface,
    onSurface = MahoganySurface,

    outline = CopperOutline,
    outlineVariant = CopperOutlineVariant
)

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
            content = content
        )
    }
}
