package me.miran.libreinfo.activity

import androidx.compose.runtime.Composable
import com.google.gson.JsonObject
import me.miran.libreinfo.activity.base.KBaseActivity
import me.miran.libreinfo.activity.data.DelaysDataHolder
import me.miran.libreinfo.exception.RequestException
import me.miran.libreinfo.parsing.storage.IdStorage
import me.miran.libreinfo.parsing.types.Post
import me.miran.libreinfo.parsing.types.Time
import me.miran.libreinfo.parsing.types.departure.Departure
import me.miran.libreinfo.util.OfflineDepartures
import me.miran.libreinfo.util.Text
import me.miran.libreinfo.util.request.RequestHelper

class DeparturePostDetailActivity : KBaseActivity("") {
    @Composable
    override fun CreateElements() {
        val post = intent.getParcelableExtra<Post>("post")!!

        name = Text.literal(post.name)
        setBaseContent {
            // TODO lazy loading?
        }

        createDepartures(post)
    }

    fun createDepartures(post: Post) {
        val delays = DelaysDataHolder.getDelays()

        Thread {
            val storage = IdStorage.getInstance();

            var stopDelays = JsonObject()
            try {
                stopDelays = RequestHelper.getStopDelays(this, post.stop.id)
            } catch (e: RequestException) {
                showErrorSnackBar(e);
            }

            val departureList = OfflineDepartures.getOfflineForPost(
                storage,
                post.stop.id.internal,
                post.postID,
                -1,
                Time.ZERO,
                delays
            )
            val departure =
                departureList.stream().filter { dep: Departure? -> dep!!.postID == post.postID }
                    .findFirst().orElse(null)


            runOnUiThread {
                setBaseContent {
                    DepartureDetail(departure, storage.apiStorage, stopDelays)
                }
            }

        }.start()
    }
}