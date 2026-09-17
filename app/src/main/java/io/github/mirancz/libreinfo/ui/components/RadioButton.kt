package io.github.mirancz.libreinfo.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun RadioButtonHorizontalSelection(modifier: Modifier = Modifier, initialValueIndex: Int = 0, options: List<String>, onSelected: (Int) -> Unit) {
    var selected by remember { mutableIntStateOf(initialValueIndex) }

    onSelected(initialValueIndex)

    Row(modifier, horizontalArrangement = Arrangement.SpaceAround, verticalAlignment = Alignment.CenterVertically) {
        for ((i, e) in options.withIndex()) {
            Row(Modifier.weight(1f).clip(RoundedCornerShape(8.dp)).selectable(
                selected = selected == i,
                onClick = {
                    selected = i
                    onSelected(i)
                },
                role = Role.RadioButton
            ).padding(8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                Text(e, fontWeight = FontWeight.Medium, fontSize = 16.sp)
                Spacer(Modifier.width(4.dp))

                RadioButton(selected == i, onClick = null)
            }
        }
    }

}