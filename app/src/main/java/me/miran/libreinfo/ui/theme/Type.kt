package me.miran.libreinfo.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

import me.miran.libreinfo.R

val robotoFamily = FontFamily(
    Font(R.font.roboto_light, FontWeight.Light),
    Font(R.font.roboto_regular, FontWeight.Normal),
    Font(R.font.roboto_medium, FontWeight.Medium),
    Font(R.font.roboto_bold, FontWeight.Bold),
    Font(R.font.roboto_black, FontWeight.Black)
)

// Default Material 3 typography values
val baseline = Typography()

private fun TextStyle.withRoboto() =
    copy(
        fontFamily = robotoFamily,
        platformStyle = PlatformTextStyle(
            includeFontPadding = false
        ),
        lineHeight = TextUnit.Unspecified
    )

val fontTest = TextStyle().copy(
    fontFamily = robotoFamily,
    platformStyle = PlatformTextStyle(
        includeFontPadding = false
    )
)

val AppTypography = Typography(
    displayLarge = baseline.displayLarge.withRoboto(),
    displayMedium = baseline.displayMedium.withRoboto(),
    displaySmall = baseline.displaySmall.withRoboto(),
    headlineLarge = baseline.headlineLarge.withRoboto(),
    headlineMedium = baseline.headlineMedium.withRoboto(),
    headlineSmall = baseline.headlineSmall.withRoboto(),
    titleLarge = baseline.titleLarge.withRoboto(),
    titleMedium = baseline.titleMedium.withRoboto().copy(fontSize = 18.sp),
    titleSmall = baseline.titleSmall.withRoboto(),
    bodyLarge = baseline.bodyLarge.withRoboto(),
    bodyMedium = baseline.bodyMedium.withRoboto(),
    bodySmall = baseline.bodySmall.withRoboto(),
    labelLarge = baseline.labelLarge.withRoboto(),
    labelMedium = baseline.labelMedium.withRoboto(),
    labelSmall = baseline.labelSmall.withRoboto(),
)

