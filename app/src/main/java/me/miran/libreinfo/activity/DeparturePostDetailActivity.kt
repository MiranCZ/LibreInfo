package me.miran.libreinfo.activity

import androidx.compose.animation.Crossfade
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
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.miran.libreinfo.R
import me.miran.libreinfo.activity.base.KBaseActivity
import me.miran.libreinfo.activity.data.DelaysDataHolder
import me.miran.libreinfo.exception.RequestException
import me.miran.libreinfo.parsing.storage.manager.AppContainer
import me.miran.libreinfo.parsing.storage.manager.IdStorage
import me.miran.libreinfo.parsing.types.Post
import me.miran.libreinfo.parsing.types.Time
import me.miran.libreinfo.parsing.types.departure.Departure
import me.miran.libreinfo.util.DeparturesSettings
import me.miran.libreinfo.util.LocalDeparturesSettings
import me.miran.libreinfo.util.OfflineDepartures
import me.miran.libreinfo.util.Text
import me.miran.libreinfo.util.request.RequestHelper

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

        var storage: IdStorage? by remember { mutableStateOf(null) }
        var departureResult: Departure? by remember { mutableStateOf(null) }
        var stopDelays by remember { mutableStateOf(JsonObject()) }

        val delays = DelaysDataHolder.getDelays()
        LaunchedEffect(Unit) {
            stopDelays = withContext(Dispatchers.IO) {
                try {
                    RequestHelper.getStopDelays(context, post.stop.id)
                } catch (e: RequestException) {
                    showErrorSnackBar(e)
                    JsonObject()
                }
            }

            val (depsRes, storageRes) = withContext(Dispatchers.IO) {
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

            departureResult = depsRes
            storage = storageRes
        }

        Crossfade(targetState = departureResult) { departure ->
            if (departure != null && storage != null) {
                CompositionLocalProvider(LocalDeparturesSettings provides departuresSettings) {
                    DepartureDetail(departure, storage!!.apiStorage, stopDelays)
                }
            } else {
                DepartureDetailShimmer(post)
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