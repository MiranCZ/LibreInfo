package io.github.mirancz.libreinfo.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RippleConfiguration
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import io.github.mirancz.libreinfo.ui.theme.extendedColors

@Composable
fun PrimaryTextButton(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    color: Color = MaterialTheme.colorScheme.primary,
    enabled: Boolean = true,
    border: BorderStroke? = null
) {
    AppButton(
        modifier = modifier,
        onClick = onClick,
        color = color,
        enabled = enabled,
        border = border
    ) {
        Text(text)
    }
}

@Composable
fun SecondaryTextButton(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    color: Color = Color.Transparent,
    enabled: Boolean = true,
    border: BorderStroke? = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline)
) {
    AppButton(
        modifier = modifier,
        onClick = onClick,
        color = color,
        enabled = enabled,
        border = border
    ) {
        Text(text, color = MaterialTheme.extendedColors.onSurfaceMedium)
    }
}

@Composable
fun AppButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    color: Color = MaterialTheme.colorScheme.surfaceContainer,
    enabled: Boolean = true,
    border: BorderStroke? = null,
    content: @Composable RowScope.() -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }

    CompositionLocalProvider(
        LocalRippleConfiguration provides RippleConfiguration(color = MaterialTheme.extendedColors.ripple)
    ) {
        Button(
            onClick = onClick,
            content = content,
            shape = RoundedCornerShape(12.dp),
            border = border,
            enabled = enabled,
            colors = ButtonDefaults.buttonColors(
                containerColor = color,
                contentColor = contentColorFor(color),
                disabledContainerColor = color,
                disabledContentColor = contentColorFor(color),
            ),
            interactionSource = interactionSource,
            modifier = modifier
                .graphicsLayer { alpha = if (enabled) 1f else 0.4f })
    }
}