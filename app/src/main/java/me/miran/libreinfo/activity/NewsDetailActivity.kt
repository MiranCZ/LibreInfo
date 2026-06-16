package me.miran.libreinfo.activity

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
import me.miran.libreinfo.R
import me.miran.libreinfo.activity.base.KBaseActivity
import me.miran.libreinfo.parsing.types.News
import me.miran.libreinfo.ui.theme.AppTypography

class NewsDetailActivity : KBaseActivity(R.string.news) {
    @Composable
    override fun CreateElements() {
        val news = intent.getParcelableExtra<News>("news")

        val context = LocalContext.current
        if (news != null) {
            Container(
                Modifier
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Column {
                    NewsHeader(news)

                    HTML(news.htmlContent, Modifier.padding(top = 16.dp)) { tv ->
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
    private fun NewsHeader(item: News) {
        Text(
            text = item.title,
            fontWeight = FontWeight.Black,
            style = AppTypography.titleMedium
        )

        Text(
            item.date.toString(),
            style = AppTypography.bodyMedium,
            fontWeight = FontWeight.Normal,
            color = colorResource(R.color.secondary_color_tone)
        )

    }


}