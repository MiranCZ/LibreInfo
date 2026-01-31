package me.miran.mhdstuff.activity.base

import android.app.Activity
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.miran.mhdstuff.R
import kotlin.reflect.KClass

abstract class NavigationActivity(nameId: Int) : KBaseActivity(nameId) {

    @Composable
    final override fun CreateElements() {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            CreateNavigation()
        }
    }


    @Composable
    abstract fun CreateNavigation()


    @Composable
    fun NavigationItem(@DrawableRes iconId: Int, @StringRes textId: Int, start: KClass<out Activity>) {
        NavigationItem(iconId, stringResource(textId), start)
    }

    @Composable
    fun NavigationItem(@DrawableRes iconId: Int, @StringRes textId: Int, onClick: () -> Unit) {
        NavigationItem(iconId, stringResource(textId), onClick)
    }

    @Composable
    fun NavigationItem(@DrawableRes iconId: Int, text: String, start: KClass<out Activity>) {
        NavigationItem(iconId, text) { startActivity(start) }
    }

    @Composable
    fun NavigationItem(@DrawableRes iconId: Int, text: String, onClick: () -> Unit) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clickable(null, ripple(color = Color.White), onClick = onClick)
                .padding(18.dp)
                .fillMaxWidth()
        ) {
            Icon(
                painter = painterResource(iconId),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = colorResource(R.color.light_blue)
            )

            Text(
                text,
                fontSize = 18.sp,
                letterSpacing = 0.5.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White,
                modifier = Modifier.padding(start = 20.dp)
            )
        }

        Divider()
    }
}