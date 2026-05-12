package me.miran.mhdstuff.activity

import androidx.compose.runtime.Composable
import com.google.gson.JsonObject
import me.miran.mhdstuff.activity.base.KBaseActivity
import me.miran.mhdstuff.activity.data.DelaysDataHolder
import me.miran.mhdstuff.exception.RequestException
import me.miran.mhdstuff.parsing.storage.IdStorage
import me.miran.mhdstuff.parsing.types.Post
import me.miran.mhdstuff.parsing.types.Time
import me.miran.mhdstuff.parsing.types.departure.Departure
import me.miran.mhdstuff.util.OfflineDepartures
import me.miran.mhdstuff.util.Text
import me.miran.mhdstuff.util.request.RequestHelper

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