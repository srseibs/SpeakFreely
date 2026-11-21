package com.sailinghawklabs.speakfreely.presentation.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
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

private val MyLightColorScheme = lightColorScheme(
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
    myColorScheme: ColorScheme = MyDarkColorScheme,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = myColorScheme,
        typography = AppTypography,
        content = content
    )
}
