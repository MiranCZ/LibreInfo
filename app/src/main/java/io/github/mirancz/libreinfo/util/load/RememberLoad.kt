package io.github.mirancz.libreinfo.util.load

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import io.github.mirancz.libreinfo.activity.base.snackbar.CustomSnackBarVisuals
import io.github.mirancz.libreinfo.activity.base.snackbar.SnackBarType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import io.github.mirancz.libreinfo.exception.AppException
import io.github.mirancz.libreinfo.ui.LocalSnackbarHostState
import io.github.mirancz.libreinfo.util.AppLog
import kotlin.coroutines.cancellation.CancellationException

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
 * Runs [block] on [Dispatchers.IO] and exposes its outcome as a [LoadState]. Re-runs whenever any of
 * [keys] change or [LoadResult.retry] is invoked.
 *
 * [AppException]s map straight to [LoadState.Error]; any other [Throwable] is logged and wrapped in a
 * generic [AppException] so screens no longer hand-write `catch (RequestException) ... catch (Exception)`
 * ladders.
 */
@Composable
fun <T> rememberLoad(vararg keys: Any?, block: suspend LoadScope.() -> T): LoadResult<T> {
    val state = remember { mutableStateOf<LoadState<T>>(LoadState.Loading) }
    val refreshing = remember { mutableStateOf(false) }
    var tick by remember { mutableIntStateOf(0) }
    val snackbar = LocalSnackbarHostState.current
    val context = LocalContext.current

    LaunchedEffect(tick, *keys) {
        // a refresh keeps the current content on screen, everything else shows the loading UI
        val isRefresh = refreshing.value && state.value is LoadState.Success
        if (!isRefresh) state.value = LoadState.Loading

        try {
            state.value = LoadState.Success(withContext(Dispatchers.IO) { LoadScope(isRefresh).block() })
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            val error = e.toAppException()
            // a failed refresh shouldn't throw away data the user is already looking at
            if (isRefresh) {
                snackbar.showSnackbar(CustomSnackBarVisuals(error.getPrettyText(context),type= SnackBarType.ERROR))
            } else {
                state.value = LoadState.Error(error)
            }

            AppLog.e("",e)
        } finally {
            refreshing.value = false
        }
    }

    return remember {
        LoadResult(
            state, refreshing,
            retry = { tick++ },
            refresh = { if (!refreshing.value) { refreshing.value = true; tick++ } },
        )
    }
}
