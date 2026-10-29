package io.github.mirancz.libreinfo.activity

import android.util.TypedValue
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.mirancz.libreinfo.R
import io.github.mirancz.libreinfo.nav.NavState
import io.github.mirancz.libreinfo.ui.components.Container
import io.github.mirancz.libreinfo.parsing.types.NewsEntry
import io.github.mirancz.libreinfo.ui.ScreenScaffold
import io.github.mirancz.libreinfo.ui.components.HTML

@Composable
fun NewsDetailScreen(state: NavState, news: NewsEntry) {
    val textColor = MaterialTheme.colorScheme.onSurface.toArgb()

    ScreenScaffold(stringResource(R.string.news), onBack = state.onBack) {
        Container(
            Modifier
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Column {
                NewsHeader(news)

                HTML(news.content, Modifier.padding(top = 16.dp)) { tv ->
                    tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
                    tv.setTextColor(textColor)
                }
            }
        }
    }
}


@Composable
private fun NewsHeader(item: NewsEntry) {
    Text(
        text = item.title,
        fontWeight = FontWeight.Black,
        style = MaterialTheme.typography.titleMedium
    )

    if (item.published != null) {
        Text(
            item.published.toString(),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Normal,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

}