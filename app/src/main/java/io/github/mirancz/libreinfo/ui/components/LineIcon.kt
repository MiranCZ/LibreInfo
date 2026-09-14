package io.github.mirancz.libreinfo.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.mirancz.libreinfo.parsing.types.LineAlias

// TODO refactor signature
// NOTE: Reason for a special `scale` argument instead of using `Modifier.scale`
// is that `Modifier.scale` is not measured by other containers with the scale applied
// FIXME figure out if the scaling can be done in a better way
@Composable
fun LineIcon(modifier: Modifier = Modifier, line: LineAlias, padding: Dp = 4.dp, scale: Float = 1f) {
    LineIcon(modifier, line.lineDisplayName, Color(line.textColor), Color(line.backgroundColor()), padding, scale)
}

@Composable
fun LineIcon(
    modifier: Modifier = Modifier,
    text: String,
    textColor: Color,
    backgroundColor: Color,
    padding: Dp = 4.dp,
    scale: Float = 1f
) {
    val shape = RoundedCornerShape(8.dp * scale)
    val size = with(LocalDensity.current) { 31.sp.toDp() * scale }
    val outline = backgroundColor == Color.Black

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .padding(padding)
            .requiredSize(size)
            .clip(shape)
            .background(backgroundColor, shape)
            .then(
                if (outline) Modifier.border(1.5.dp * scale, textColor, shape)
                else Modifier
            )
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 2.dp * scale),
            textAlign = TextAlign.Center,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Visible,
            autoSize = TextAutoSize.StepBased(
                minFontSize = 8.sp * scale,
                maxFontSize = 16.sp * scale,
            ),
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}

