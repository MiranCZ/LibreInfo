package io.github.mirancz.libreinfo.activity

import android.util.TypedValue
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import io.github.mirancz.libreinfo.activity.base.KBaseActivity
import io.github.mirancz.libreinfo.parsing.types.Diversion
import io.github.mirancz.libreinfo.R
import io.github.mirancz.libreinfo.ui.components.Container

class DiversionInfoActivity : KBaseActivity(R.string.diversions) {
    @Composable
    override fun CreateElements() {
        val diversion = intent.getParcelableExtra<Diversion>("diversion")

        val textColor = MaterialTheme.colorScheme.onSurface.toArgb()
        if (diversion != null) {
            Container(
                Modifier
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                EventHeader(diversion) {
                    HTML(diversion.content, Modifier.padding(top = 16.dp)) { tv ->
                        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
                        tv.setTextColor(textColor)
                    }

                }
            }
        } else {
            NothingHere()
        }
    }


}