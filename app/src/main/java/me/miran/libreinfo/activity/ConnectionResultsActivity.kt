package me.miran.libreinfo.activity

import androidx.compose.animation.Crossfade
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.miran.libreinfo.R
import me.miran.libreinfo.activity.base.KBaseActivity
import me.miran.libreinfo.exception.RequestException
import me.miran.libreinfo.parsing.types.stop.Stop
import me.miran.libreinfo.util.Result
import me.miran.libreinfo.util.request.RequestHelper

class ConnectionResultsActivity : KBaseActivity(R.string.connection_results) {

    @Composable
    override fun CreateElements() {
        val fromStop = intent.getParcelableExtra<Stop>("fromStop")!!
        val toStop = intent.getParcelableExtra<Stop>("toStop")!!
        val departureTime = intent.getStringExtra("departureTime")!!

        var result by remember { mutableStateOf(Result.ok<JsonObject?, RequestException>(null)) }
        val context = LocalContext.current

        LaunchedEffect(Unit) {
            val r = withContext(Dispatchers.IO) {
                try {
                    val res = RequestHelper.findConnections(context, fromStop.id.original(), toStop.id.original(), departureTime)
                    Result.ok<JsonObject?, RequestException>(res)
                } catch (e: RequestException) {
                    Result.err(e)
                }
            }
            result = r
        }

        Crossfade(targetState = result) { r ->
            when (r) {
                is Result.Ok -> if (r.value == null) {
                    Loading()
                } else {
                    Text(r.value.toString())
                }
                is Result.Err -> ErrorWidget(r.err)
            }
        }
    }
}
