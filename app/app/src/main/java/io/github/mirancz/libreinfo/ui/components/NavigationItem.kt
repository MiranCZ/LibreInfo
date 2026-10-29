package io.github.mirancz.libreinfo.ui.components

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
fun NavigationItem(@DrawableRes iconId: Int, @StringRes text: Int, needsWifi: Boolean = false, onClick: () -> Unit) {
    NavigationItem(iconId, stringResource(text), needsWifi, onClick)
}

@Composable
fun NavigationItem(@DrawableRes iconId: Int, text: String, needsWifi: Boolean = false, onClick: () -> Unit) {
    // FIXME reintroduce dimmed; seems to be buggy right now anyway
//    val dimmed = needsWifi && !LocalInternetAvailable.current
    val dimmed = false

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .alpha(if (dimmed) 0.4f else 1f)
            .clickable(null, ripple(), onClick = onClick)
            .padding(18.dp)
            .fillMaxWidth()
    ) {
        Icon(
            painter = painterResource(iconId),
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Text(
            text,
            fontSize = 18.sp,
            letterSpacing = 0.5.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(start = 20.dp)
        )
    }

    Divider()
}