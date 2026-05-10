package me.miran.mhdstuff.activity

import android.content.Intent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.miran.mhdstuff.R
import me.miran.mhdstuff.activity.base.KBaseActivity
import me.miran.mhdstuff.exception.RequestException
import me.miran.mhdstuff.parsing.storage.IdStorage
import me.miran.mhdstuff.parsing.types.Diversion
import me.miran.mhdstuff.util.Either
import me.miran.mhdstuff.util.request.RequestHelper

class DiversionsActivity : KBaseActivity(R.string.diversions) {
    @Composable
    override fun CreateElements() {
        var diversionsResult by remember { mutableStateOf(Either.left<List<Diversion>?, RequestException>(null)) }

        val context = LocalContext.current
        LaunchedEffect(Unit) {
            val result = withContext(Dispatchers.IO) {

                val storage = IdStorage.getInstance();

                try {
                    Either.left<List<Diversion>, RequestException>(Diversion.parseDiversions(
                        RequestHelper.getDiversions(context),
                        storage.lineStorage
                    ))
                } catch (e: RequestException) {
                    Either.right(e)
                }
            }

            diversionsResult = result
        }

        when (val local = diversionsResult) {
            is Either.Left -> {
                val diversions = local.left

                if (diversions != null) {
                    if (diversions.isEmpty()) {
                        NothingHere()
                    } else {
                        Column(Modifier.verticalScroll(rememberScrollState())) {
                            for (diversion in diversions) {
                                Diversion(diversion)
                            }
                        }
                    }
                } else {
                    Loading()
                }
            }
            is Either.Right -> {
                val error = local.right;

                ErrorWidget(error)
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