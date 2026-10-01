package io.github.mirancz.libreinfo.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.mirancz.libreinfo.parsing.types.DateTime
import io.github.mirancz.libreinfo.parsing.types.Diversion

@Composable
fun EventHeader(item: Diversion, content: @Composable ColumnScope.() -> Unit = {}) {
    Column {
        Text(
            text = item.title,
            fontWeight = FontWeight.Black,
            style = MaterialTheme.typography.titleMedium
        )

        if (item.from != DateTime.NONE) {
            Row {
                Text(
                    "Od: ",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Normal
                )
                Text(
                    item.from.toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        if (item.to != DateTime.NONE) {
            Row {
                Text(
                    "Do: ",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Normal
                )
                Text(
                    item.to.toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (item.lines != null) {
            LineList(item.lines, Modifier.padding(top = 8.dp))
        }

        content()
    }
}