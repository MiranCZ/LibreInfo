package io.github.mirancz.libreinfo.activity

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import io.github.mirancz.libreinfo.activity.base.KBaseActivity
import io.github.mirancz.libreinfo.activity.data.DelaysDataHolder
import io.github.mirancz.libreinfo.exception.RequestException
import io.github.mirancz.libreinfo.parsing.storage.manager.AppContainer
import io.github.mirancz.libreinfo.parsing.types.Post
import io.github.mirancz.libreinfo.parsing.types.Time
import io.github.mirancz.libreinfo.parsing.types.departure.Departure
import io.github.mirancz.libreinfo.util.DeparturesSettings
import io.github.mirancz.libreinfo.util.LocalDeparturesSettings
import io.github.mirancz.libreinfo.util.OfflineDepartures
import io.github.mirancz.libreinfo.util.Text
import io.github.mirancz.libreinfo.util.load.rememberLoad
import io.github.mirancz.libreinfo.util.request.RequestHelper
import io.github.mirancz.libreinfo.R
import io.github.mirancz.libreinfo.parsing.types.dto.StopDelaysResponse

class DeparturePostDetailActivity : KBaseActivity("") {

    private var departuresSettings by mutableStateOf(DeparturesSettings())

    override fun onResume() {
        super.onResume()
        departuresSettings = DeparturesSettings.fromPrefs()
    }

    @Composable
    override fun CreateElements() {
        val post = intent.getParcelableExtra<Post>("post")!!

        LaunchedEffect(post) { name = Text.literal(post.name) }

        val context = LocalContext.current

        var stopDelays by remember { mutableStateOf(StopDelaysResponse(emptyMap())) }

        val delays = DelaysDataHolder.getDelays()

        val result = rememberLoad {
            try {
                stopDelays = RequestHelper.getStopDelays(context, post.stop.id)
            } catch (e: RequestException) {
                showErrorSnackBar(e)
            }

            val storage = AppContainer.storageProvider.getInstance()

            val departureList = OfflineDepartures.getOfflineForPost(
                storage,
                post.stop.id.internal,
                post.postID,
                -1,
                Time.ZERO,
                delays
            )

            val res = departureList.stream().filter { dep: Departure? -> dep!!.postID == post.postID }
                .findFirst().orElse(null)

            Pair(res, storage)
        }

        AsyncContent(result, loading = { DepartureDetailShimmer(post) }) { res ->
            CompositionLocalProvider(LocalDeparturesSettings provides departuresSettings) {
                DepartureDetail(res.first, res.second.apiStorage, stopDelays)
            }
        }
    }


    @Composable
    fun DepartureDetailShimmer(post: Post) {
        val shimmer = rememberActivityShimmer()
        val color = colorResource(R.color.widget_background)
        Container(
            innerPadding = 0.dp,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            LazyColumn(Modifier.padding(vertical = 8.dp, horizontal = 6.dp)) {
                stickyHeader {
                    DeparturePostHeader(
                        post.name, Modifier
                            .background(color)
                            .clickable(interactionSource = null, indication = null) {})

                }

                items(30) { _ ->
                    DepartureEntryRowShimmer(shimmer)
                }
            }
        }
    }

}