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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.mirancz.libreinfo.activity.base.KBaseActivity
import io.github.mirancz.libreinfo.R
import io.github.mirancz.libreinfo.ui.components.Container
import io.github.mirancz.libreinfo.parsing.types.NewsEntry

class NewsDetailActivity : KBaseActivity(R.string.news) {
    @Composable
    override fun CreateElements() {
        val news = intent.getParcelableExtra<NewsEntry>("news")

        val textColor = MaterialTheme.colorScheme.onSurface.toArgb()
        if (news != null) {
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
        } else {
            NothingHere()
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


}