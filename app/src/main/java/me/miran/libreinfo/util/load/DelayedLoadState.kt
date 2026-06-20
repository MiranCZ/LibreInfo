package me.miran.libreinfo.util.load

import android.os.SystemClock
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

/**
 * Smooths the [LoadState.Loading] phase so the loading UI never "glitches" in and out for loads that
 * resolve almost immediately
 *
 *  - For the first [delayMs] of loading nothing is shown (returns `null`), so a fast load skips the
 *    shimmer UI entirely instead of flashing it for like two frames
 *  - Once the shimmer UI does appear it stays for at least [minShowMs], so it never blinks away the
 *    instant after it showed up
 *
 * Returns the [LoadState] that should currently be rendered, or `null` while nothing should be shown.
 */
@Composable
fun <T> rememberDelayedLoadState(
    state: LoadState<T>,
    delayMs: Long = 200L,
    minShowMs: Long = 400L,
): LoadState<T>? {
    // Start blank only when we begin in a loading state; an already-resolved state shows immediately
    var display by remember { mutableStateOf(state.takeUnless { it is LoadState.Loading }) }

    var shownAt by remember { mutableStateOf<Long?>(null) }

    LaunchedEffect(state) {
        when (state) {
            is LoadState.Loading -> {
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
                shownAt = null
                display = state
            }
        }
    }

    return display
}
