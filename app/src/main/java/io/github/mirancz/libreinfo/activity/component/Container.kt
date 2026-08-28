package io.github.mirancz.libreinfo.activity.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.mirancz.libreinfo.R

@Composable
fun Container(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    innerPadding: Dp = 16.dp,
    color: Color = colorResource(
        R.color.widget_background
    ),
    content: @Composable BoxScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors().copy(containerColor = color),
        onClick = onClick
    ) {
        Box(Modifier.padding(innerPadding), content = content)
    }
}

@Composable
fun Container(
    modifier: Modifier = Modifier,
    innerPadding: Dp = 16.dp,
    color: Color = colorResource(R.color.widget_background),
    content: @Composable BoxScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors().copy(containerColor = color)
    ) {
        Box(Modifier.padding(innerPadding), content = content)
    }
}