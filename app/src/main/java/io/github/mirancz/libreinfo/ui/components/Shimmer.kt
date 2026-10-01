package io.github.mirancz.libreinfo.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.valentinilk.shimmer.Shimmer
import com.valentinilk.shimmer.ShimmerBounds
import com.valentinilk.shimmer.rememberShimmer
import com.valentinilk.shimmer.shimmer
import io.github.mirancz.libreinfo.ui.theme.extendedColors
import kotlin.random.Random

@Composable
fun ShimmerBox(modifier: Modifier, shimmer: Shimmer, shape: Shape = RoundedCornerShape(4.dp)) {
    Box(modifier.shimmer(shimmer).background(MaterialTheme.extendedColors.shimmer, shape))
}

@Composable
fun ShimmerText(shimmer: Shimmer, widthFraction: Float = 0.85f, variance: Float = 0.15f, height: Dp = 14.dp) {
    val width = remember {
        (widthFraction + Random.nextFloat() * variance - variance / 2f).coerceIn(
            0.1f,
            1f
        )
    }
    ShimmerBox(Modifier.fillMaxWidth(width).height(height), shimmer)
}

@Composable
fun rememberActivityShimmer() = rememberShimmer(ShimmerBounds.Window)