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
import me.miran.mhdstuff.activity.base.KBaseActivity
import me.miran.mhdstuff.R

class AboutActivity : KBaseActivity(R.string.about) {
    @Composable
    override fun CreateElements() {
        Container(Modifier.padding(16.dp)) {
            Column {
                Text(
                    text = stringResource(R.string.about_text),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )

                Divider()

                Row {
                    Text("Verze ", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 24.sp)
                    Text("0.2-Alpha", color = Color.White, fontSize = 24.sp)
                }
            }
        }
    }
}