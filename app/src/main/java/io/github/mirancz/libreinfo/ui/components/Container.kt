package io.github.mirancz.libreinfo.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun Container(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    innerPadding: Dp = 16.dp,
    color: Color = MaterialTheme.colorScheme.surfaceContainer,
    content: @Composable BoxScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = color),
        onClick = onClick
    ) {
        Box(Modifier.padding(innerPadding), content = content)
    }
}

@Composable
fun Container(
    modifier: Modifier = Modifier,
    innerPadding: Dp = 16.dp,
    color: Color = MaterialTheme.colorScheme.surfaceContainer,
    content: @Composable BoxScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Box(Modifier.padding(innerPadding), content = content)
    }
}