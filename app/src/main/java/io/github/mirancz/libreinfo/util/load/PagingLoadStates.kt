package io.github.mirancz.libreinfo.util.load

import androidx.paging.LoadState as PagingLoadState

/**
 * Maps a Paging [PagingLoadState] onto the app's [LoadState], so paged screens can render their
 * initial load with the same loading/error UI as [rememberLoad] screens.
 */
fun PagingLoadState.toLoadState(): LoadState<Unit> = when (this) {
    is PagingLoadState.Loading -> LoadState.Loading
    is PagingLoadState.Error -> LoadState.Error(error.toAppException())
    is PagingLoadState.NotLoading -> LoadState.Success(Unit)
}
