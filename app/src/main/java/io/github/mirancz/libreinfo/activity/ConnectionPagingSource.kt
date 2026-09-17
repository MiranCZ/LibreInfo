package io.github.mirancz.libreinfo.activity

import android.content.Context
import androidx.paging.PagingSource
import androidx.paging.PagingState
import io.github.mirancz.libreinfo.parsing.storage.manager.AppContainer
import io.github.mirancz.libreinfo.parsing.types.DateTime
import io.github.mirancz.libreinfo.parsing.types.stop.Stop
import io.github.mirancz.libreinfo.util.load.toAppException
import io.github.mirancz.libreinfo.util.request.RequestHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.coroutines.cancellation.CancellationException

/**
 * Loads connection search results page by page. The first page is searched by [time]; the pages
 * before and after it are fetched with the cursors the server returns alongside each page.
 */
internal class ConnectionPagingSource(
    private val context: Context,
    private val fromStop: Stop,
    private val toStop: Stop,
    private val time: String,
    private val isArrival: Boolean,
) : PagingSource<String, ConnectionUi>() {

    override suspend fun load(params: LoadParams<String>): LoadResult<String, ConnectionUi> {
        return try {
            // storage access and the request both block, and Paging calls this on the main thread
            withContext(Dispatchers.IO) { loadPage(cursor = params.key) }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            LoadResult.Error(e.toAppException())
        }
    }

    private suspend fun loadPage(cursor: String?): LoadResult.Page<String, ConnectionUi> {
        val storage = AppContainer.storageProvider.getInstance()
        val response = RequestHelper.findConnections(context, fromStop, toStop, time, isArrival, cursor)
        val now = DateTime.now()

        val connections = response.connections.mapIndexed { index, dto ->
            buildConnectionUi(dto.map(storage), storage, now, isClosest = index == response.closestIndex)
        }

        return LoadResult.Page(
            data = connections,
            prevKey = response.previousPageCursor,
            nextKey = response.nextPageCursor,
        )
    }

    // cursors belong to the original search, so a refresh starts over from the searched time
    override fun getRefreshKey(state: PagingState<String, ConnectionUi>): String? = null
}
