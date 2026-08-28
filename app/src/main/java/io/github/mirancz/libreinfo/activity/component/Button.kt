package io.github.mirancz.libreinfo.activity.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.RippleConfiguration
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.mirancz.libreinfo.R

@Composable
fun PrimaryTextButton(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    color: Color = colorResource(R.color.light_blue),
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
        // black is not visible that well
        // TODO probs should either change color tone or otherwise figure this out
        Text(text, color = Color.White)
    }
}

@Composable
fun SecondaryTextButton(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    color: Color = Color.Transparent,
    enabled: Boolean = true,
    border: BorderStroke? = BorderStroke(1.5.dp, colorResource(R.color.secondary_color_tone))
) {
    AppButton(
        modifier = modifier,
        onClick = onClick,
        color = color,
        enabled = enabled,
        border = border
    ) {
        Text(text, color = colorResource(R.color.secondary_color_light_tone))
    }
}

@Composable
fun AppButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    color: Color = colorResource(R.color.widget_background),
    enabled: Boolean = true,
    border: BorderStroke? = null,
    content: @Composable RowScope.() -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }

    CompositionLocalProvider(
        LocalRippleConfiguration provides RippleConfiguration(color = colorResource(R.color.light_gray))
    ) {
        Button(
            onClick = onClick,
            content = content,
            shape = RoundedCornerShape(12.dp),
            border = border,
            enabled = enabled,
            colors = ButtonDefaults.buttonColors().copy(
                containerColor = color,
                disabledContainerColor = color,
            ),
            interactionSource = interactionSource,
            modifier = modifier
                .graphicsLayer { alpha = if (enabled) 1f else 0.4f })
    }
}