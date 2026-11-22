package com.sailinghawklabs.speakfreely.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val MyDarkColorScheme = darkColorScheme(
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

@Composable
fun AppTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = MyDarkColorScheme,
        typography = AppTypography,
        content = content
    )
}
