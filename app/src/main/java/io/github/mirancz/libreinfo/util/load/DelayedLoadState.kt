package io.github.mirancz.libreinfo.util.load

import android.os.SystemClock
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

/** The [state] to render (`null` for nothing) and whether switching to it should [animate] */
data class DisplayedLoadState<out T>(val state: LoadState<T>?, val animate: Boolean)

/**
 * Smooths the [LoadState.Loading] phase so the loading UI never "glitches" in and out for loads that
 * resolve almost immediately
 *
 *  - For the first [delayMs] of loading nothing is shown (`null`), so a fast load skips the
 *    shimmer UI entirely instead of flashing it for like two frames
 *  - Once the shimmer UI does appear it stays for at least [minShowMs], so it never blinks away the
 *    instant after it showed up
 *  - A load that resolves within [instantMs] shouldn't be animated in, so the app doesn't look slower
 */
@Composable
fun <T> rememberDelayedLoadState(
    state: LoadState<T>,
    delayMs: Long = 200L,
    minShowMs: Long = 400L,
    instantMs: Long = 20L,
): DisplayedLoadState<T> {
    // Start blank only when we begin in a loading state; an already-resolved state shows immediately
    var display by remember { mutableStateOf(state.takeUnless { it is LoadState.Loading }) }
    var animate by remember { mutableStateOf(true) }

    var loadingSince by remember { mutableStateOf<Long?>(null) }
    var shownAt by remember { mutableStateOf<Long?>(null) }

    LaunchedEffect(state) {
        when (state) {
            is LoadState.Loading -> {
                loadingSince = SystemClock.elapsedRealtime()
                animate = true
                display = null
                shownAt = null
                delay(delayMs.milliseconds)
                display = state
                shownAt = SystemClock.elapsedRealtime()
            }

            is LoadState.Success, is LoadState.Error -> {
                shownAt?.let { shown ->
                    val remaining = minShowMs - (SystemClock.elapsedRealtime() - shown)
                    if (remaining > 0) delay(remaining.milliseconds)
                }
                // `null` loadingSince means the load resolved before the loading effect even ran, so don't animate
                animate = loadingSince.let { it != null && SystemClock.elapsedRealtime() - it >= instantMs }
                loadingSince = null
                shownAt = null
                display = state
            }
        }
    }

    return DisplayedLoadState(display, animate)
}
