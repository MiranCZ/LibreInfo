package me.miran.mhdstuff.activity

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.miran.mhdstuff.R
import me.miran.mhdstuff.activity.base.KBaseActivity
import me.miran.mhdstuff.exception.RequestException
import me.miran.mhdstuff.parsing.storage.IdStorage
import me.miran.mhdstuff.parsing.types.DateTime
import me.miran.mhdstuff.parsing.types.Event
import me.miran.mhdstuff.util.request.RequestHelper

class EventsActivity : KBaseActivity(R.string.events) {

    @Composable
    override fun CreateElements() {
        var events by remember<MutableState<List<Event>?>> { mutableStateOf(null) }

        val context = LocalContext.current
        LaunchedEffect(Unit) {
            val result = withContext(Dispatchers.IO) {

                val storage = IdStorage.getInstance();

                try {
                    Event.parseEvents(RequestHelper.getEvents(context), storage.lineStorage());
                } catch (e: RequestException) {
                    // TODO handle exception
                    ArrayList()
                }
            }

            events = result
        }

        if (events != null) {
            val events = events!!

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
            Loading()
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