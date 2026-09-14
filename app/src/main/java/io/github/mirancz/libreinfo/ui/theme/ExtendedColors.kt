package io.github.mirancz.libreinfo.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/** App colors that have no slot in the Material 3 color scheme. */
@Immutable
data class ExtendedColors(
    /** Text and icons with emphasis between `onSurface` and `onSurfaceVariant`. */
    val onSurfaceMedium: Color,
    /** Highlighted area placed on top of a `surfaceContainer`. */
    val surfaceHighlight: Color,
    val successContainer: Color,
    val infoContainer: Color,
    val favourite: Color,
    val shimmer: Color,
    val ripple: Color,
)

internal val LightExtendedColors = ExtendedColors(
    onSurfaceMedium = Gray13,
    surfaceHighlight = Gray27,
    successContainer = Green,
    infoContainer = Gray20,
    favourite = Red,
    shimmer = Gray80,
    ripple = Gray87,
)

internal val DarkExtendedColors = ExtendedColors(
    onSurfaceMedium = Gray87,
    surfaceHighlight = Gray19,
    successContainer = Green,
    infoContainer = Gray20,
    favourite = Red,
    shimmer = Gray33,
    ripple = Gray87,
)

internal val LocalExtendedColors = staticCompositionLocalOf { LightExtendedColors }

val MaterialTheme.extendedColors: ExtendedColors
    @Composable
    @ReadOnlyComposable
    get() = LocalExtendedColors.current
