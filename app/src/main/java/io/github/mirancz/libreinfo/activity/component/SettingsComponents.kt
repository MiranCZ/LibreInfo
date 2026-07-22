package io.github.mirancz.libreinfo.activity.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.mirancz.libreinfo.R
import io.github.mirancz.libreinfo.util.Settings

@Composable
fun SettingSwitch(label: String, settingKey: String, onChange: (Boolean)->Unit = {}) {
    val settings = Settings.get()

    var checked by remember { mutableStateOf(settings.getBoolean(settingKey, false)) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = colorResource(R.color.secondaryColor))
        AppSwitch(checked = checked, onCheckedChange = {
            checked = it
            settings.putBoolean(settingKey, it).flush()
        })
    }
}