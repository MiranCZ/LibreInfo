package io.github.mirancz.libreinfo.activity

import android.util.TypedValue
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import io.github.mirancz.libreinfo.activity.base.KBaseActivity
import io.github.mirancz.libreinfo.ui.theme.AppTypography
import io.github.mirancz.libreinfo.R
import io.github.mirancz.libreinfo.ui.components.Container
import io.github.mirancz.libreinfo.parsing.types.NewsEntry

class NewsDetailActivity : KBaseActivity(R.string.news) {
    @Composable
    override fun CreateElements() {
        val news = intent.getParcelableExtra<NewsEntry>("news")

        val context = LocalContext.current
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
                        tv.setTextColor(ContextCompat.getColor(context, R.color.secondaryColor))
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
            style = AppTypography.titleMedium
        )

        if (item.published != null) {
            Text(
                item.published.toString(),
                style = AppTypography.bodyMedium,
                fontWeight = FontWeight.Normal,
                color = colorResource(R.color.secondary_color_tone)
            )
        }

    }


}