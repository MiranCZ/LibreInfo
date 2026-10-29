package io.github.mirancz.libreinfo.nav

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.staticCompositionLocalOf
import kotlinx.coroutines.flow.filterNotNull

/**
 * Passes results from a screen back to an earlier screen in the back stack (e.g. [NavRoute.StopPicker]).
 *
 * Results are kept in memory only until consumed by [NavResultEffect].
 */
class NavResults {
    private val results = mutableStateMapOf<String, Any>()

    fun send(key: String, result: Any) {
        results[key] = result
    }

    internal fun peek(key: String): Any? = results[key]

    internal fun consume(key: String): Any? = results.remove(key)
}

val LocalNavResults = staticCompositionLocalOf<NavResults> { error("No NavResults provided") }

/**
 * Calls [onResult] whenever a result is sent to [key], each result is delivered once.
 */
@Composable
fun <T : Any> NavResultEffect(key: String, onResult: (T) -> Unit) {
    val results = LocalNavResults.current
    val currentOnResult by rememberUpdatedState(onResult)

    LaunchedEffect(results, key) {
        snapshotFlow { results.peek(key) }.filterNotNull().collect {
            @Suppress("UNCHECKED_CAST")
            currentOnResult(results.consume(key) as T)
        }
    }
}
