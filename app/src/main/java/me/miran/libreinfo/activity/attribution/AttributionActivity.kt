package me.miran.libreinfo.activity.attribution

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.miran.libreinfo.R
import me.miran.libreinfo.activity.base.KBaseActivity

class AttributionActivity : KBaseActivity(R.string.data_sources) {

    @Composable
    override fun CreateElements() {
        // TODO click into a dedicated screen with more detailed description or something
        Column(Modifier.verticalScroll(rememberScrollState())) {
            AttributionItem(
                "KORDIS JMK a.s.",
                "Koordinace dopravní obslužnosti na území Jihomoravského kraje",
                R.drawable.kordis_icon
            )

            AttributionItem(
                "Data Brno",
                "Otevřená platforma sloužící ke sdílení dat o městě Brně",
                R.drawable.data_brno_icon
            )

            AttributionItem(
                "OpenStreetMap",
                "Volně dostupná mapa světa vytvářená komunitou dobrovolníků",
                R.drawable.openstreetmap_icon
            )

            AttributionItem(
                "Font Awesome",
                "Knihovna vektorových ikon a log",
                R.drawable.fontawesome_icon
            )

            AttributionItem(
                "Pfaedle",
                "Nástroj pro přesné mapování tras veřejné dopravy",
                R.drawable.pfaedle_icon
            )
        }
    }

    @Composable
    fun AttributionItem(name: String, description: String, @DrawableRes iconId: Int) {
        Container(Modifier.padding(16.dp)) {
            Column() {
                Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(name, fontSize = 24.sp, fontWeight = FontWeight.Black, modifier = Modifier.weight(1f))

                    Image(
                        painter = painterResource(id = iconId),
                        contentDescription = "$name icon",
                        modifier = Modifier.height(48.dp).width(96.dp),
                        contentScale = ContentScale.Fit
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