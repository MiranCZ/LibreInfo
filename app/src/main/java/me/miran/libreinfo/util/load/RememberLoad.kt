package me.miran.libreinfo.util.load

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.miran.libreinfo.R
import me.miran.libreinfo.exception.AppException
import me.miran.libreinfo.util.AppLog
import kotlin.coroutines.cancellation.CancellationException

/**
 * Holds the current [LoadState] of a [rememberLoad] block together with a [retry] action that
 * re-runs it in place (used by the error UI's "Try again" button).
 */
class LoadResult<T> internal constructor(
    state: State<LoadState<T>>,
    val retry: () -> Unit,
) {
    val state: LoadState<T> by state
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
fun <T> rememberLoad(vararg keys: Any?, block: suspend () -> T): LoadResult<T> {
    val state = remember { mutableStateOf<LoadState<T>>(LoadState.Loading) }
    var retryTick by remember { mutableIntStateOf(0) }

    LaunchedEffect(retryTick, *keys) {
        state.value = LoadState.Loading
        state.value = try {
            LoadState.Success(withContext(Dispatchers.IO) { block() })
        } catch (e: CancellationException) {
            // let the coroutine cancel cleanly when the screen leaves composition
            throw e
        } catch (e: AppException) {
            LoadState.Error(e)
        } catch (e: Throwable) {
            // Unwrap an AppException carried as a cause (e.g. StorageInitException) so the real,
            // user-facing message survives instead of a generic one.
            val wrapped = generateSequence(e) { it.cause }.filterIsInstance<AppException>().firstOrNull()
            if (wrapped != null) {
                LoadState.Error(wrapped)
            } else {
                AppLog.e("rememberLoad", "Unexpected error while loading", e)
                LoadState.Error(AppException(R.string.generic_error, e))
            }
        }
    }

    return remember { LoadResult(state, retry = { retryTick++ }) }
}
