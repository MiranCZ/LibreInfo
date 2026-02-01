package me.miran.mhdstuff.activity

import android.content.Intent
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.miran.mhdstuff.R
import me.miran.mhdstuff.activity.base.KBaseActivity
import me.miran.mhdstuff.exception.RequestException
import me.miran.mhdstuff.parsing.storage.IdStorage
import me.miran.mhdstuff.parsing.types.DateTime
import me.miran.mhdstuff.parsing.types.Diversion
import me.miran.mhdstuff.ui.theme.AppTypography
import me.miran.mhdstuff.util.request.RequestHelper

class DiversionsActivity : KBaseActivity(R.string.diversions) {
    @Composable
    override fun CreateElements() {
        var diversions by remember<MutableState<List<Diversion>?>> { mutableStateOf(null) }

        val context = LocalContext.current
        LaunchedEffect(Unit) {
            val result = withContext(Dispatchers.IO) {

                val storage = IdStorage.getInstance();

                try {
                    Diversion.parseDiversions(
                        RequestHelper.getDiversions(context),
                        storage.lineStorage
                    )
                } catch (e: RequestException) {
                    // TODO handle exception
                    ArrayList()
                }
            }

            diversions = result
        }

        if (diversions != null) {
            val diversions = diversions!!

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
                Text(
                    text = item.title,
                    fontWeight = FontWeight.Black,
                    style = AppTypography.titleMedium
                )

                if (item.from != DateTime.NONE) {
                    Row {
                        Text(
                            "Od: ",
                            style = AppTypography.bodyMedium,
                            fontWeight = FontWeight.Normal
                        )
                        Text(
                            item.from.toString(),
                            style = AppTypography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                if (item.to != DateTime.NONE) {
                    Row {
                        Text(
                            "Do: ",
                            style = AppTypography.bodyMedium,
                            fontWeight = FontWeight.Normal
                        )
                        Text(
                            item.to.toString(),
                            style = AppTypography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                LineList(item.lines, Modifier.padding(top = 8.dp))
            }
        }
    }

}