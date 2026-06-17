package me.miran.libreinfo.activity

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.valentinilk.shimmer.Shimmer
import me.miran.libreinfo.R
import me.miran.libreinfo.activity.base.KBaseActivity
import me.miran.libreinfo.parsing.types.News
import me.miran.libreinfo.ui.theme.AppTypography
import me.miran.libreinfo.util.load.rememberLoad
import me.miran.libreinfo.util.request.RequestHelper

// TODO pagination
class NewsActivity : KBaseActivity(R.string.news) {

    @Composable
    override fun CreateElements() {
        val context = LocalContext.current

        val news = rememberLoad {
            News.parseNewsList(RequestHelper.getNews(context).getAsJsonArray("emails"))
        }

        AsyncContent(news, loading = { NewsListShimmer() }) { newsList ->
            if (newsList.isEmpty()) {
                NothingHere()
            } else {
                LazyColumn {
                    items(newsList) { newsEntry ->
                        NewsEntry(newsEntry)
                    }
                }
            }
        }
    }

    @Composable
    fun NewsEntry(news: News) {
        Container(
            onClick = {
                startActivity(NewsDetailActivity::class) {
                    it.putExtra("news", news)
                }
            },
            Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Column {
                Text(
                    text = news.title,
                    fontWeight = FontWeight.Black,
                    style = AppTypography.titleMedium
                )

                Text(
                    news.date.toString(),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    modifier = Modifier.padding(top = 8.dp),
                )

                Text(
                    news.plaintext,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp,
                    color = colorResource(R.color.secondary_color_tone)
                )
            }
        }
    }

    @Composable
    fun NewsListShimmer() {
        val shimmer = rememberActivityShimmer()
        Column(Modifier.verticalScroll(rememberScrollState())) {
            repeat(10) { NewsEntryShimmer(shimmer) }
        }
    }

    @Composable
    fun NewsEntryShimmer(shimmer: Shimmer) {
        Container(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            Column {
                ShimmerText(shimmer, height = 20.dp)
                Spacer(Modifier.height(12.dp))
                ShimmerText(shimmer, height = 12.dp, widthFraction = 0.4f)
                Spacer(Modifier.height(4.dp))

                ShimmerBox(Modifier.fillMaxWidth().height(42.dp), shimmer)
            }
        }
    }


}