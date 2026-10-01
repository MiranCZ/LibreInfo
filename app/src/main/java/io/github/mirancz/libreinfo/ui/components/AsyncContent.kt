package io.github.mirancz.libreinfo.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.mirancz.libreinfo.util.load.LoadResult
import io.github.mirancz.libreinfo.util.load.LoadState
import io.github.mirancz.libreinfo.util.load.rememberDelayedLoadState

/**
 * Renders [result] as the appropriate state: [loading] while in flight, [ErrorWidget] (with a
 * working retry button) on failure, and [content] on success.
 */
@Composable
fun <T> AsyncContent(
    result: LoadResult<T>,
    modifier: Modifier = Modifier,
    loading: @Composable () -> Unit = { Loading() },
    content: @Composable (T) -> Unit,
) {
    AsyncContent(
        result.state,
        result.retry,
        modifier,
        loading,
        content
    )
}

/**
 * Renders [loadState] like the [io.github.mirancz.libreinfo.util.load.LoadResult] overload does, for loads not driven by
 * [io.github.mirancz.libreinfo.util.load.rememberLoad] (e.g. paging), calling [onRetry] on retry.
 */
@Composable
fun <T> AsyncContent(
    loadState: LoadState<T>,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    loading: @Composable () -> Unit = { Loading() },
    content: @Composable (T) -> Unit,
) {
    val (display, animate) = rememberDelayedLoadState(loadState)
    Crossfade(
        targetState = display,
        modifier = modifier,
        animationSpec = if (animate) tween() else snap()
    ) { state ->
        when (state) {
            null -> {}
            is LoadState.Loading -> loading()
            is LoadState.Error -> ErrorWidget(state.error, onRetry = onRetry)
            is LoadState.Success -> content(state.data)
        }
    }
}

