package me.miran.libreinfo.activity

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.valentinilk.shimmer.Shimmer
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.miran.libreinfo.R
import me.miran.libreinfo.activity.base.KBaseActivity
import me.miran.libreinfo.exception.RequestException
import me.miran.libreinfo.parsing.storage.IdStorage
import me.miran.libreinfo.parsing.types.DateTime
import me.miran.libreinfo.parsing.types.Event
import me.miran.libreinfo.util.Either
import me.miran.libreinfo.util.request.RequestHelper
import kotlin.random.Random

class EventsActivity : KBaseActivity(R.string.events) {

    @Composable
    override fun CreateElements() {
        var eventsResult by remember { mutableStateOf(Either.left<List<Event>?, RequestException>(null)) }

        val context = LocalContext.current
        LaunchedEffect(Unit) {
            val result = withContext(Dispatchers.IO) {

                val storage = IdStorage.getInstance();

                try {
                    Either.left<List<Event>, RequestException>(Event.parseEvents(RequestHelper.getEvents(context), storage.lineStorage()))
                } catch (e: RequestException) {
                    Either.right(e)
                }
            }

            eventsResult = result
        }

        Crossfade(targetState = eventsResult) { local -> when (local) {
            is Either.Left -> {
                val events = local.left

                if (events != null) {
                    if (events.isEmpty()) {
                        NothingHere()
                    } else {
                        Column(Modifier.verticalScroll(rememberScrollState())) {
                            for (event in events) {
                                Event(event)
                            }
                        }
                    }
                } else {
                    EventListShimmer()
                }
            }
            is Either.Right -> {
                val error = local.right;

                ErrorWidget(error)
            }
        } }


    }

    @Composable
    fun EventListShimmer() {
        val shimmer = rememberActivityShimmer()
        Column(Modifier.verticalScroll(rememberScrollState())) {
            repeat(4) { EventEntryShimmer(shimmer) }
        }
    }

    @Composable
    fun EventEntryShimmer(shimmer: Shimmer) {
        val lineIconCount = remember { Random.nextInt(1, 5) }
        val textLineCount = remember { if (Random.nextBoolean()) Random.nextInt(1, 4) else 0 }

        Container(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            Column {
                ShimmerText(shimmer, widthFraction = 0.8f, variance = 0.35f, height = 22.dp)
                Spacer(Modifier.height(10.dp))
                ShimmerText(shimmer, widthFraction = 0.55f)
                Spacer(Modifier.height(8.dp))
                ShimmerText(shimmer, widthFraction = 0.35f)
                Spacer(Modifier.height(10.dp))
                Row {
                    repeat(lineIconCount) {
                        ShimmerLineIcon(shimmer)
                        Spacer(Modifier.width(8.dp))
                    }
                }
                if (textLineCount > 0) {
                    Spacer(Modifier.height(12.dp))
                    repeat(textLineCount) {
                        ShimmerText(shimmer)
                        Spacer(Modifier.height(4.dp))
                    }
                }
            }
        }
    }

    @Composable
    fun Event(item: Event) {

        val times = DateTime.toShortenedInformedString(item.from, item.to)

        Container(
            Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Column {
                Text(item.title,  fontSize = 18.sp, fontWeight = FontWeight.Black)

                Row(Modifier.padding(top = 8.dp)) {
                    Text(times[0], fontWeight = FontWeight.Bold, fontSize = 15.sp)

                    if (times.size > 1) {
                        Text(" - ", fontWeight = FontWeight.Normal, fontSize = 15.sp)
                        Text(times[1], fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }

                Row(Modifier.padding(bottom = 8.dp)) {
                    Text(stringResource(R.string.vehicle_delay) +" ", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text(item.delay + " min", fontWeight = FontWeight.Bold, color = Color.Red, fontSize = 15.sp)
                }

                LineList(item.lines)

                if (!item.text.isBlank()) {
                    HTML(item.text, Modifier.padding(top=12.dp))
                }
            }
        }
    }


}