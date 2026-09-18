package io.github.mirancz.libreinfo.activity

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import com.valentinilk.shimmer.Shimmer
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.mirancz.libreinfo.activity.base.KBaseActivity
import io.github.mirancz.libreinfo.parsing.storage.manager.AppContainer
import io.github.mirancz.libreinfo.parsing.types.DateTime
import io.github.mirancz.libreinfo.R
import io.github.mirancz.libreinfo.ui.components.Container
import io.github.mirancz.libreinfo.parsing.types.Event
import io.github.mirancz.libreinfo.util.load.rememberLoad
import io.github.mirancz.libreinfo.util.request.RequestHelper
import kotlin.random.Random

class EventsActivity : KBaseActivity(R.string.events) {

    @Composable
    override fun CreateElements() {
        val context = LocalContext.current

        val events = rememberLoad {
            val storage = AppContainer.storageProvider.getInstance()
            RequestHelper.getEvents(context).events.map { it.map(storage) }
        }

        AsyncContent(events, loading = { EventListShimmer() }) { eventList ->
            if (eventList.isEmpty()) {
                NothingHere()
            } else {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    for (event in eventList) {
                        Event(event)
                    }
                }
            }
        }
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

        val collapsible =
            item.content.isNotBlank() && (item.content.lines().size > 3 || item.content.length > 150)

        var expanded by remember(item) { mutableStateOf(false) }

        val chevronRotation = animateFloatAsState(
            targetValue = if (expanded) 180f else 0f, label = "eventChevron"
        )

        val toggle = { expanded = !expanded }

        // while collapsed the whole card is one big target. Once expanded only the header is, so
        // that links in the description stay tappable and reading it can't collapse it by accident
        val cardModifier =
            if (collapsible && !expanded) Modifier.clickable(onClick = toggle) else Modifier

        val headerModifier = if (collapsible && expanded) {
            Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable(onClick = toggle)
        } else Modifier

        Container(
            Modifier.padding(horizontal = 16.dp, vertical = 8.dp), innerPadding = 0.dp
        ) {
            Column(cardModifier) {
                Column(headerModifier.padding(16.dp)) {
                    Text(item.title, fontSize = 18.sp, fontWeight = FontWeight.Black)

                    Row(Modifier.padding(top = 8.dp)) {
                        Text(times[0], fontWeight = FontWeight.Bold, fontSize = 15.sp)

                        if (times.size > 1) {
                            Text(" - ", fontWeight = FontWeight.Normal, fontSize = 15.sp)
                            Text(times[1], fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }

                    if (item.delay != null) {
                        Row {
                            Text(
                                stringResource(R.string.vehicle_delay) + " ",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Text(
                                "${item.delay} min",
                                fontWeight = FontWeight.Bold,
                                color = Color.Red,
                                fontSize = 15.sp
                            )
                        }
                    }

                    // the chevron shares its row with the line icons, so an event without lines
                    // still keeps it pinned to the right edge
                    if (!item.lines.isNullOrEmpty() || collapsible) {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            verticalAlignment = Alignment.Bottom
                        ) {
                            if (item.lines != null) {
                                LineList(item.lines, Modifier.weight(1f))
                            } else {
                                Spacer(Modifier.weight(1f))
                            }

                            if (collapsible) {
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    modifier = Modifier.graphicsLayer {
                                        rotationZ = chevronRotation.value
                                    })
                            }
                        }
                    }
                }

                if (item.content.isNotBlank()) {
                    HTML(
                        item.content,
                        Modifier
                            .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                            .animateContentSize(),
                        maxLines = if (collapsible && !expanded) 3 else Int.MAX_VALUE
                    )
                }
            }
        }
    }

}