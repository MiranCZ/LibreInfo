package me.miran.mhdstuff.activity.base

import android.app.Activity
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
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
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.miran.mhdstuff.R
import kotlin.reflect.KClass

private val LocalInternetAvailable = compositionLocalOf { false }

abstract class NavigationActivity(nameId: Int) : KBaseActivity(nameId) {

    @Composable
    final override fun CreateElements() {
        val context = LocalContext.current
        val connectivityManager = remember { context.getSystemService(ConnectivityManager::class.java) }

        val internetAvailable = produceState(initialValue = run {
            val caps = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) == true
        }) {
            val request = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()

            val callback = object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) { value = true }
                override fun onLost(network: Network) { value = false }
            }

            connectivityManager.registerNetworkCallback(request, callback)
            awaitDispose { connectivityManager.unregisterNetworkCallback(callback) }
        }

        CompositionLocalProvider(LocalInternetAvailable provides internetAvailable.value) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
            ) {
                CreateNavigation()
            }
        }
    }


    @Composable
    abstract fun CreateNavigation()


    @Composable
    fun NavigationItem(@DrawableRes iconId: Int, @StringRes textId: Int, start: KClass<out Activity>, needsWifi: Boolean = false) {
        NavigationItem(iconId, stringResource(textId), start, needsWifi)
    }

    @Composable
    fun NavigationItem(@DrawableRes iconId: Int, @StringRes textId: Int, needsWifi: Boolean = false, onClick: () -> Unit) {
        NavigationItem(iconId, stringResource(textId), needsWifi, onClick)
    }

    @Composable
    fun NavigationItem(@DrawableRes iconId: Int, text: String, start: KClass<out Activity>, needsWifi: Boolean = false) {
        NavigationItem(iconId, text, needsWifi) { startActivity(start) }
    }

    @Composable
    fun NavigationItem(@DrawableRes iconId: Int, text: String, needsWifi: Boolean = false, onClick: () -> Unit) {
        val dimmed = needsWifi && !LocalInternetAvailable.current

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
                tint = colorResource(R.color.light_blue)
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
}