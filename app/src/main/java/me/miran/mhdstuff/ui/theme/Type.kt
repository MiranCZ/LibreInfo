package me.miran.mhdstuff.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

import me.miran.mhdstuff.R

val robotoFamily = FontFamily(
    Font(R.font.roboto_light, FontWeight.Light),
    Font(R.font.roboto_regular, FontWeight.Normal),
    Font(R.font.roboto_medium, FontWeight.Medium),
    Font(R.font.roboto_bold, FontWeight.Bold),
    Font(R.font.roboto_black, FontWeight.Black)
)

// Default Material 3 typography values
val baseline = Typography()

val AppTypography = Typography(
    displayLarge = baseline.displayLarge.copy(fontFamily = robotoFamily),
    displayMedium = baseline.displayMedium.copy(fontFamily = robotoFamily),
    displaySmall = baseline.displaySmall.copy(fontFamily = robotoFamily),
    headlineLarge = baseline.headlineLarge.copy(fontFamily = robotoFamily),
    headlineMedium = baseline.headlineMedium.copy(fontFamily = robotoFamily),
    headlineSmall = baseline.headlineSmall.copy(fontFamily = robotoFamily),
    titleLarge = baseline.titleLarge.copy(fontFamily = robotoFamily),
    titleMedium = baseline.titleMedium.copy(fontFamily = robotoFamily),
    titleSmall = baseline.titleSmall.copy(fontFamily = robotoFamily),
    bodyLarge = baseline.bodyLarge.copy(fontFamily = robotoFamily),
    bodyMedium = baseline.bodyMedium.copy(fontFamily = robotoFamily),
    bodySmall = baseline.bodySmall.copy(fontFamily = robotoFamily),
    labelLarge = baseline.labelLarge.copy(fontFamily = robotoFamily),
    labelMedium = baseline.labelMedium.copy(fontFamily = robotoFamily),
    labelSmall = baseline.labelSmall.copy(fontFamily = robotoFamily),
)

