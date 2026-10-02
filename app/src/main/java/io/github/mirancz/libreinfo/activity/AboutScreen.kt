package io.github.mirancz.libreinfo.activity

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.mirancz.libreinfo.BuildConfig
import io.github.mirancz.libreinfo.R
import io.github.mirancz.libreinfo.nav.NavRoute
import io.github.mirancz.libreinfo.nav.NavState
import io.github.mirancz.libreinfo.ui.NavigationScreenScaffold
import io.github.mirancz.libreinfo.ui.ScreenScaffold
import io.github.mirancz.libreinfo.ui.components.Container
import io.github.mirancz.libreinfo.ui.components.Divider
import io.github.mirancz.libreinfo.ui.components.NavigationItem

@Composable
fun AboutScreen(state: NavState) {
    NavigationScreenScaffold(stringResource(R.string.about), onBack = state.onBack) {
        Container(Modifier.padding(vertical = 16.dp)) {
            Column {
                Text(
                    text = stringResource(R.string.about_text),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )

                Divider()

                Row {
                    Text("Verze ", fontWeight = FontWeight.Bold, fontSize = 24.sp)
                    Text(BuildConfig.VERSION_NAME, color = Color.White, fontSize = 24.sp)
                }
            }
        }

        NavigationItem(
            R.drawable.heart_solid,
            R.string.data_sources
        ) { state.onNavigate(NavRoute.About.Attribution) }
    }
}
