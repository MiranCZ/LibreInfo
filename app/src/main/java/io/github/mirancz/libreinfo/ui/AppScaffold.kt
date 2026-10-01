package io.github.mirancz.libreinfo.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.mirancz.libreinfo.BuildConfig
import io.github.mirancz.libreinfo.R
import io.github.mirancz.libreinfo.activity.base.snackbar.CustomSnackBarVisuals
import io.github.mirancz.libreinfo.activity.base.snackbar.SnackBarType
import io.github.mirancz.libreinfo.parsing.storage.manager.AppContainer
import io.github.mirancz.libreinfo.ui.components.ErrorWidget
import io.github.mirancz.libreinfo.ui.theme.AppTheme
import io.github.mirancz.libreinfo.ui.theme.extendedColors

val LocalSnackbarHostState = staticCompositionLocalOf<SnackbarHostState> { error("No SnackbarHostState") }

@Composable
fun AppRoot(content: @Composable () -> Unit) {
    val snackbar = remember { SnackbarHostState() }

    AppTheme {
        CompositionLocalProvider(LocalSnackbarHostState provides snackbar) {
            Box {
                val error = AppContainer.storageProvider.error()

                if (error != null) {
                    // surface a fatal startup/data-init failure
                    ErrorWidget(error)
                } else {
                    content()
                }

                @Suppress("SimplifyBooleanWithConstants", "KotlinConstantConditions")
                if (BuildConfig.BUILD_TYPE != "release") {
                    DevRibbon(Modifier.align(Alignment.BottomEnd).navigationBarsPadding())
                }
            }
        }
    }
}

@Composable
fun NavigationScreenScaffold(
    title: String,
    onBack: (() -> Unit)?,
    actions: @Composable RowScope.() -> Unit = {},
    bottomOverlay: @Composable BoxScope.() -> Unit = {},
    content: @Composable () -> Unit,
) {
    ScreenScaffold(title, onBack, actions, bottomOverlay) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            content()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenScaffold(
    title: String,
    onBack: (() -> Unit)?,
    actions: @Composable RowScope.() -> Unit = {},
    bottomOverlay: @Composable BoxScope.() -> Unit = {},
    content: @Composable () -> Unit,
) {
    val snackBarHostState = LocalSnackbarHostState.current

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackBarHostState) { data ->
                val customVisuals = data.visuals as? CustomSnackBarVisuals

                val type = customVisuals?.type ?: SnackBarType.INFO

                val backgroundColor = when (type) {
                    SnackBarType.SUCCESS -> MaterialTheme.extendedColors.successContainer
                    SnackBarType.ERROR -> MaterialTheme.colorScheme.errorContainer
                    SnackBarType.INFO -> MaterialTheme.extendedColors.infoContainer
                }

                Snackbar(
                    snackbarData = data,
                    containerColor = backgroundColor,
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = title,
                        fontWeight = FontWeight.Medium,
                        fontSize = 20.sp,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(
                                painterResource(R.drawable.chevron_left),
                                "Go back",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                actions = actions
            )
        }
    ) { innerPadding ->
        Box(Modifier
            .padding(innerPadding)
            .fillMaxSize()) {
            content()
            Box(Modifier.align(Alignment.BottomCenter)) {
                bottomOverlay()
            }
        }
    }
}


@Composable
private fun DevRibbon(modifier: Modifier = Modifier) {
    val corner = 96.dp
    // Slides the strip along the diagonal so its center sits over the corner.
    val shift = 22.dp

    Box(modifier
        .size(corner)
        .clipToBounds()) {
        Box(
            Modifier
                .align(Alignment.Center)
                .offset(x = shift, y = shift)
                .rotate(-45f)
                .width(corner * 2)
                .background(MaterialTheme.colorScheme.errorContainer)
                .padding(vertical = 3.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "DEV",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
            )
        }
    }
}
