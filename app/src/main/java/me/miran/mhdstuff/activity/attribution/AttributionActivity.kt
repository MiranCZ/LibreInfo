package me.miran.mhdstuff.activity.attribution

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.miran.mhdstuff.R
import me.miran.mhdstuff.activity.base.KBaseActivity

class AttributionActivity : KBaseActivity(R.string.data_sources) {

    @Composable
    override fun CreateElements() {
        // TODO click into a dedicated screen with more detailed description or something
        AttributionItem(
            "KORDIS JMK a.s.",
            "Koordinace dopravní obslužnosti na území Jihomoravského kraje",
            R.drawable.kordis_icon
        )
    }

    @Composable
    fun AttributionItem(name: String, description: String, @DrawableRes iconId: Int) {
        Container(Modifier.padding(16.dp)) {
            Column() {
                Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(name, fontSize = 24.sp, fontWeight = FontWeight.Black, modifier = Modifier.weight(1f))

                    Image(
                        painter = painterResource(id = iconId),
                        contentDescription = "$name icon"
                    )
                }

                Divider()

                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(description, modifier = Modifier.padding(top = 4.dp, end = 8.dp).weight(1f))
                }
            }
        }
    }

}