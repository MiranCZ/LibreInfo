package io.github.mirancz.libreinfo.activity

import android.util.TypedValue
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.mirancz.libreinfo.parsing.types.Diversion
import io.github.mirancz.libreinfo.R
import io.github.mirancz.libreinfo.nav.NavState
import io.github.mirancz.libreinfo.ui.ScreenScaffold
import io.github.mirancz.libreinfo.ui.components.Container
import io.github.mirancz.libreinfo.ui.components.EventHeader
import io.github.mirancz.libreinfo.ui.components.HTML

@Composable
fun DiversionDetailScreen(state: NavState, diversion: Diversion) {
    ScreenScaffold(stringResource(R.string.diversions), onBack = state.onBack) {
        val textColor = MaterialTheme.colorScheme.onSurface.toArgb()

        Column(Modifier.verticalScroll(rememberScrollState())) {
            Container(
                Modifier
                    .padding(horizontal = 16.dp)
            ) {
                EventHeader(diversion) {
                    HTML(diversion.content, Modifier.padding(top = 16.dp)) { tv ->
                        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
                        tv.setTextColor(textColor)
                    }

                }

            }

            Spacer(Modifier.height(8.dp))
        }
    }
}
