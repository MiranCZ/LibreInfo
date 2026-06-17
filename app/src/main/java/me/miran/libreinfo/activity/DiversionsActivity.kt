package me.miran.libreinfo.activity

import android.content.Intent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.valentinilk.shimmer.Shimmer
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import me.miran.libreinfo.R
import me.miran.libreinfo.activity.base.KBaseActivity
import me.miran.libreinfo.parsing.storage.IdStorage
import me.miran.libreinfo.parsing.types.Diversion
import me.miran.libreinfo.util.load.rememberLoad
import me.miran.libreinfo.util.request.RequestHelper
import kotlin.random.Random

class DiversionsActivity : KBaseActivity(R.string.diversions) {
    @Composable
    override fun CreateElements() {
        val context = LocalContext.current

        val diversions = rememberLoad {
            val storage = IdStorage.getInstance()
            Diversion.parseDiversions(RequestHelper.getDiversions(context), storage.lineStorage)
        }

        AsyncContent(diversions, loading = { DiversionListShimmer() }) { diversionList ->
            if (diversionList.isEmpty()) {
                NothingHere()
            } else {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    for (diversion in diversionList) {
                        Diversion(diversion)
                    }
                }
            }
        }
    }

    @Composable
    fun DiversionListShimmer() {
        val shimmer = rememberActivityShimmer()
        Column(Modifier.verticalScroll(rememberScrollState())) {
            repeat(4) { DiversionEntryShimmer(shimmer) }
        }
    }

    @Composable
    fun DiversionEntryShimmer(shimmer: Shimmer) {
        val lineIconCount = remember { Random.nextInt(1, 7) }

        Container(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            Column {
                ShimmerText(shimmer, widthFraction = 0.7f, variance = 0.35f, height = 20.dp)
                Spacer(Modifier.height(8.dp))
                ShimmerText(shimmer, widthFraction = 0.45f)
                Spacer(Modifier.height(6.dp))
                ShimmerText(shimmer, widthFraction = 0.45f)
                Spacer(Modifier.height(10.dp))
                Row {
                    repeat(lineIconCount) {
                        ShimmerLineIcon(shimmer)
                        Spacer(Modifier.width(8.dp))
                    }
                }
            }
        }
    }

    @Composable
    fun Diversion(item: Diversion) {
        Container(
            onClick = {
                startActivity(
                    DiversionInfoActivity::class
                ) { intent: Intent -> intent.putExtra("diversion", item) }
            },
            Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Column {
                EventHeader(item)
            }
        }
    }

}