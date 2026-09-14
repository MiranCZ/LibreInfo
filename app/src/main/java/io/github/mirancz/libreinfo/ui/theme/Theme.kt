package io.github.mirancz.libreinfo.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

// The app has a single accent, so secondary and tertiary reuse primary.

private val LightColorScheme = lightColorScheme(
    primary = Blue,
    onPrimary = White,
    primaryContainer = Blue,
    onPrimaryContainer = White,
    inversePrimary = Blue,
    secondary = Blue,
    onSecondary = White,
    secondaryContainer = Blue,
    onSecondaryContainer = White,
    tertiary = Blue,
    onTertiary = White,
    tertiaryContainer = Blue,
    onTertiaryContainer = White,
    background = Gray94,
    onBackground = Black,
    surface = Gray94,
    onSurface = Black,
    surfaceVariant = Gray87,
    onSurfaceVariant = Gray10,
    inverseSurface = Gray13,
    inverseOnSurface = White,
    error = Red,
    onError = White,
    errorContainer = RedMuted,
    onErrorContainer = Black,
    outline = Gray10,
    outlineVariant = Gray33,
    scrim = Black,
    surfaceBright = White,
    surfaceDim = Gray87,
    // In light mode widgets are lighter than the background, so the container ramp goes towards white.
    surfaceContainerLowest = White,
    surfaceContainerLow = Gray94,
    surfaceContainer = White,
    surfaceContainerHigh = White,
    surfaceContainerHighest = Gray87,
)

private val DarkColorScheme = darkColorScheme(
    primary = Blue,
    onPrimary = White,
    primaryContainer = Blue,
    onPrimaryContainer = White,
    inversePrimary = Blue,
    secondary = Blue,
    onSecondary = White,
    secondaryContainer = Blue,
    onSecondaryContainer = White,
    tertiary = Blue,
    onTertiary = White,
    tertiaryContainer = Blue,
    onTertiaryContainer = White,
    background = Gray04,
    onBackground = White,
    surface = Gray04,
    onSurface = White,
    surfaceVariant = Gray21,
    onSurfaceVariant = Gray67,
    inverseSurface = Gray94,
    inverseOnSurface = Black,
    error = Red,
    onError = White,
    errorContainer = RedDeep,
    onErrorContainer = White,
    outline = Gray67,
    outlineVariant = Gray33,
    scrim = Black,
    surfaceBright = Gray21,
    surfaceDim = Gray04,
    surfaceContainerLowest = Black,
    surfaceContainerLow = Gray10,
    surfaceContainer = Gray13,
    surfaceContainerHigh = Gray19,
    surfaceContainerHighest = Gray21,
)

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalExtendedColors provides if (darkTheme) DarkExtendedColors else LightExtendedColors
    ) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
            typography = AppTypography,
            content = content
        )
    }
}
