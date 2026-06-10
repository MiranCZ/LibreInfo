package me.miran.libreinfo.activity

import androidx.compose.animation.Crossfade
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.valentinilk.shimmer.Shimmer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.miran.libreinfo.R
import me.miran.libreinfo.activity.base.KBaseActivity
import me.miran.libreinfo.exception.RequestException
import me.miran.libreinfo.parsing.types.News
import me.miran.libreinfo.ui.theme.AppTypography
import me.miran.libreinfo.util.Result
import me.miran.libreinfo.util.request.RequestHelper

// TODO pagination
class NewsActivity : KBaseActivity(R.string.news) {

    @Composable
    override fun CreateElements() {
        var newsResult by remember { mutableStateOf(Result.ok<List<News>?, RequestException>(null)) }

        val context = LocalContext.current

        LaunchedEffect(Unit) {
            val result = withContext(Dispatchers.IO) {

                try {
                    Result.ok<List<News>, RequestException>(
                        News.parseNewsList(
                            RequestHelper.getNews(context).getAsJsonArray("emails"),
                        )
                    )
                } catch (e: RequestException) {
                    Result.err(e)
                }
            }

            newsResult = result
        }


        Crossfade(targetState = newsResult) { local ->
            when (local) {
                is Result.Ok -> {
                    val news = local.value

                    if (news != null) {
                        if (news.isEmpty()) {
                            NothingHere()
                        } else {
                            LazyColumn {
                                items(news) { newsEntry ->
                                    NewsEntry(newsEntry)
                                }
                            }
                        }
                    } else {
                        NewsListShimmer()
                    }
                }

                is Result.Err -> {
                    val error = local.err

                    ErrorWidget(error)
                }
            }
        }
    }

    @Composable
    fun NewsEntry(news: News) {
        Container(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
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