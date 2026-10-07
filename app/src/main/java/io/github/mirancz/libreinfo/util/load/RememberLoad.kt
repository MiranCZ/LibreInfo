package io.github.mirancz.libreinfo.util.load

import android.os.SystemClock
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.currentCompositeKeyHashCode
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import io.github.mirancz.libreinfo.ui.LocalSnackbarHostState
import io.github.mirancz.libreinfo.ui.showError
import io.github.mirancz.libreinfo.util.AppLog
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

class LoadScope internal constructor(val isRefresh: Boolean)

/**
 * Holds the current [LoadState] of a [rememberLoad] block together with a [retry] action that
 * re-runs it in place (used by the error UI's "Try again" button).
 */
class LoadResult<T> internal constructor(
    state: State<LoadState<T>>,
    refreshing: State<Boolean>,
    val retry: () -> Unit,
    val refresh: () -> Unit
) {
    val state: LoadState<T> by state
    val isRefreshing: Boolean by refreshing
}

/**
 * Backing state of a [rememberLoad]. When caching it lives in the nav entry's ViewModel store, so
 * a finished load survives the screen leaving and re-entering composition.
 */
internal class LoadHolder<T> : ViewModel() {
    val state = mutableStateOf<LoadState<T>>(LoadState.Loading)
    val refreshing = mutableStateOf(false)
    val tick = mutableIntStateOf(0)

    // tick + keys of the last successful load; a matching one means the cached result is for the same request
    var loadedFor: List<Any?>? = null
    var loadedAtMs = 0L
}

/**
 * Runs [block] on [Dispatchers.IO] and exposes its outcome as a [LoadState]. Re-runs whenever any of
 * [keys] change or [LoadResult.retry] is invoked.
 *
 * A successful result is kept while the surrounding nav entry is on the back stack and isn't re-fetched when the
 * screen is composed again with the same [keys], until it is older than [cacheFor] ([Duration.INFINITE] never
 * expires, `null` disables caching). An expired result stays on screen while it is refreshed in the background.
 * [LoadResult.refresh] and [LoadResult.retry] always re-run the load. [keys] must have stable `equals` for this to work.
 *
 * The [io.github.mirancz.libreinfo.exception.AppError] of an [io.github.mirancz.libreinfo.exception.AppException]
 * maps straight to [LoadState.Error]; any other [Throwable] is logged and becomes a generic error so screens no longer hand-write `catch (RequestException) ... catch (Exception)`
 * ladders.
 */
@Composable
fun <T> rememberLoad(vararg keys: Any?, cacheFor: Duration? = Duration.INFINITE, block: suspend LoadScope.() -> T): LoadResult<T> {
    // the composite key tells apart several cached loads within one entry
    val holder = if (cacheFor != null) {
        viewModel<LoadHolder<T>>(key = currentCompositeKeyHashCode.toString())
    } else {
        remember { LoadHolder() }
    }
    val state = holder.state
    val refreshing = holder.refreshing
    var tick by holder.tick
    val snackbar = LocalSnackbarHostState.current
    val context = LocalContext.current

    LaunchedEffect(tick, *keys) {
        val signature = listOf(tick, *keys)
        val cached = state.value is LoadState.Success && holder.loadedFor == signature
        if (cacheFor != null && cached) {
            val age = (SystemClock.elapsedRealtime() - holder.loadedAtMs).milliseconds
            if (age < cacheFor) return@LaunchedEffect
            refreshing.value = true
        }

        // a refresh keeps the current content on screen, everything else shows the loading UI
        val isRefresh = refreshing.value && state.value is LoadState.Success
        if (!isRefresh) state.value = LoadState.Loading

        try {
            state.value = LoadState.Success(withContext(Dispatchers.IO) { LoadScope(isRefresh).block() })
            holder.loadedFor = signature
            holder.loadedAtMs = SystemClock.elapsedRealtime()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            val error = e.toAppError()
            // a failed refresh shouldn't throw away data the user is already looking at
            if (isRefresh) {
                snackbar.showError(context, error)
            } else {
                state.value = LoadState.Error(error)
            }

            AppLog.e("",e)
        } finally {
            refreshing.value = false
        }
    }

    return remember(holder) {
        LoadResult(
            state, refreshing,
            retry = { tick++ },
            refresh = { if (!refreshing.value) { refreshing.value = true; tick++ } },
        )
    }
}
