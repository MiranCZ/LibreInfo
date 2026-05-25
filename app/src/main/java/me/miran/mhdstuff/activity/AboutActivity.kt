package me.miran.mhdstuff.activity

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
import me.miran.mhdstuff.R
import me.miran.mhdstuff.activity.attribution.AttributionActivity
import me.miran.mhdstuff.activity.base.NavigationActivity

class AboutActivity : NavigationActivity(R.string.about) {
    @Composable
    override fun CreateNavigation() {
        Container(Modifier.padding(vertical=16.dp)) {
            Column {
                Text(
                    text = stringResource(R.string.about_text),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )

                Divider()

                Row {
                    Text("Verze ", fontWeight = FontWeight.Bold, fontSize = 24.sp)
                    Text("0.2-Alpha", color = Color.White, fontSize = 24.sp)
                }
            }
        }

        NavigationItem(R.drawable.heart_solid, R.string.data_sources, AttributionActivity::class)
    }
}