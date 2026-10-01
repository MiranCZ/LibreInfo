package io.github.mirancz.libreinfo.activity.base

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import io.github.mirancz.libreinfo.BuildConfig
import io.github.mirancz.libreinfo.R
import io.github.mirancz.libreinfo.activity.DeparturePostDetailActivity
import io.github.mirancz.libreinfo.activity.TripDetailActivity
import io.github.mirancz.libreinfo.activity.base.snackbar.CustomSnackBarVisuals
import io.github.mirancz.libreinfo.activity.base.snackbar.SnackBarType
import io.github.mirancz.libreinfo.exception.AppException
import io.github.mirancz.libreinfo.parsing.storage.manager.AppContainer
import io.github.mirancz.libreinfo.parsing.types.Post
import io.github.mirancz.libreinfo.parsing.types.departure.Departure
import io.github.mirancz.libreinfo.ui.AppRoot
import io.github.mirancz.libreinfo.ui.ScreenScaffold
import io.github.mirancz.libreinfo.ui.components.Departure
import io.github.mirancz.libreinfo.ui.components.ErrorWidget
import io.github.mirancz.libreinfo.ui.theme.AppTheme
import io.github.mirancz.libreinfo.ui.theme.extendedColors
import io.github.mirancz.libreinfo.util.Text
import kotlinx.coroutines.launch
import java.util.function.Consumer
import kotlin.reflect.KClass

@OptIn(ExperimentalMaterial3Api::class)
abstract class KBaseActivity(name: Text) : ComponentActivity() {

    constructor(nameId: Int) : this(Text.translatable(nameId))
    constructor(nameStr: String) : this(Text.literal(nameStr))
    var name by mutableStateOf(name)

    private val snackBarHostState = SnackbarHostState()

    // FIXME should be final
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setBaseContent {
            CreateElements()
        }
    }

    open fun setBaseContent(
        actions: @Composable RowScope.() -> Unit = {},
        content: @Composable () -> Unit
    ) {
        setContent {
            AppRoot {
                ScreenScaffold(
                    name.getName(this),
                    onBack = if (parentActivityIntent != null) ::onBackPressed else null,
                    actions
                ) { content() }
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

    @Composable
    abstract fun CreateElements()

    /**
     * Optional content pinned to the bottom of the screen, floating over [CreateElements].
     */
    @Composable
    open fun BottomOverlay(modifier: Modifier) {
    }

    fun startActivity(clazz: KClass<out Activity>) {
        startActivity(clazz) {}
    }

    fun startActivity(clazz: KClass<out Activity>, intentSetup: Consumer<Intent>) {
        val intent = Intent(this, clazz.java)

        intentSetup.accept(intent)
        startActivity(intent)
        overridePendingTransition(R.anim.fast_scale_up, R.anim.fast_fade_out)
    }

    fun showSnackBar(message: String, type: SnackBarType) {
        lifecycleScope.launch {
            snackBarHostState.showSnackbar(
                CustomSnackBarVisuals(
                    message = message,
                    type = type
                )
            )
        }
    }

    fun showErrorSnackBar(e: AppException) {
        showSnackBar(e.getPrettyText(this), type = SnackBarType.ERROR)
    }

    override fun finish() {
        super.finish()

        overridePendingTransition(R.anim.fast_fade_in, R.anim.fast_scale_down)
    }

    override fun onNavigateUp(): Boolean {
        val result = super.onNavigateUp()
        overridePendingTransition(R.anim.fast_fade_in, R.anim.fast_scale_down)
        return result
    }


    // TODO eventually remove wrapper
    @Composable
    fun Departure(departure: Departure, post: Post?) {
        Departure(
            departure,
            post,
            onHeaderClick = {
                startActivity(DeparturePostDetailActivity::class) {
                    it.putExtra("post", post)
                }
            }
        ) { vehicleInfo, stopId, tripId ->
            startActivity(TripDetailActivity::class) { intent ->
                if (vehicleInfo.hasDelay()) {
                    intent.putExtra("delay", vehicleInfo.delay())
                }
                if (vehicleInfo.hasId()) {
                    intent.putExtra("vehicleId", vehicleInfo.id())
                }

                intent.putExtra("stopId", stopId)
                intent.putExtra("tripId", tripId)
            }
        }
    }

}