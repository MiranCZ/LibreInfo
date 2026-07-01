package me.miran.libreinfo.activity.attribution

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
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
                "KORDIS JMK, a.s.",
                "Koordinace dopravní obslužnosti na území Jihomoravského kraje",
                R.drawable.kordis_icon,
                copyright = "KORDIS JMK, a.s.",
                license = "CC BY 4.0",
                licenseUrl = "https://creativecommons.org/licenses/by/4.0/",
                sourceUrl = "https://data.brno.cz/datasets/379d2e9a7907460c8ca7fda1f3e84328",
                modified = true,
            )

            AttributionItem(
                "Data Brno",
                "Otevřená platforma sloužící ke sdílení dat o městě Brně",
                R.drawable.data_brno_icon,
                copyright = "Statutární město Brno",
                license = "CC BY 4.0",
                licenseUrl = "https://creativecommons.org/licenses/by/4.0/",
                sourceUrl = "https://data.brno.cz",
                modified = true,
            )

            AttributionItem(
                "OpenStreetMap",
                "Volně dostupná mapa světa vytvářená komunitou dobrovolníků",
                R.drawable.openstreetmap_icon,
                copyright = "OpenStreetMap contributors",
                license = "ODbL 1.0",
                licenseUrl = "https://opendatacommons.org/licenses/odbl/1-0/",
                sourceUrl = "https://www.openstreetmap.org/copyright",
            )

            AttributionItem(
                "MapTiler",
                "Poskytovatel mapových podkladů a dlaždic",
                R.drawable.maptiler_icon,
                copyright = "MapTiler",
                license = "Podmínky užití",
                licenseUrl = "https://www.maptiler.com/copyright/",
                licensePrefix = false,
            )

            AttributionItem(
                "Font Awesome",
                "Knihovna vektorových ikon a log",
                R.drawable.fontawesome_icon,
                license = "CC BY 4.0",
                licenseUrl = "https://fontawesome.com/license/free",
            )

            AttributionItem(
                "Pfaedle",
                "Nástroj pro přesné mapování tras veřejné dopravy",
                R.drawable.pfaedle_icon,
                license = "GNU GPL v3",
                licenseUrl = "https://github.com/ad-freiburg/pfaedle/blob/master/LICENSE",
            )

            AttributionItem(
                "Motis",
                "Nástroj pro vyhledávání spojení a plánování cest ve veřejné dopravě",
                R.drawable.motis_icon,
                license = "MIT",
                licenseUrl = "https://github.com/motis-project/motis/blob/master/LICENSE",
            )
        }
    }

    @Composable
    fun AttributionItem(
        name: String,
        description: String,
        @DrawableRes iconId: Int,
        copyright: String? = null,
        license: String? = null,
        licenseUrl: String? = null,
        sourceUrl: String? = null,
        modified: Boolean = false,
        licensePrefix: Boolean = true,
    ) {
        Container(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
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

                if (license != null && licenseUrl != null) {
                    LicenseLine(copyright, license, licenseUrl, sourceUrl, modified, licensePrefix)
                }
            }
        }
    }

    @Composable
    fun LicenseLine(
        copyright: String?,
        license: String,
        licenseUrl: String,
        sourceUrl: String?,
        modified: Boolean,
        licensePrefix: Boolean,
    ) {
        val linkStyle = TextLinkStyles(
            SpanStyle(
                color = colorResource(R.color.light_blue),
                textDecoration = TextDecoration.Underline
            )
        )

        Text(
            buildAnnotatedString {
                copyright?.let { append("© $it · ") }
                if (licensePrefix) {
                    append("Licencováno pod ")
                }
                withLink(LinkAnnotation.Url(licenseUrl, linkStyle)) { append(license) }
                if (modified) {
                    append(" · Upraveno")
                }
                sourceUrl?.let {
                    append(" · ")
                    withLink(LinkAnnotation.Url(it, linkStyle)) { append("Zdroj") }
                }
            },
            fontSize = 12.sp,
            color = colorResource(R.color.secondary_color_light_tone),
            modifier = Modifier.padding(top = 8.dp)
        )
    }

}