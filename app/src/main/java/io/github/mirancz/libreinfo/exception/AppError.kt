package io.github.mirancz.libreinfo.exception

import io.github.mirancz.libreinfo.util.request.Endpoint

/**
 * What went wrong, as plain data. Turning it into user-facing text, an icon or a retry affordance is
 * done app-side (see `ui/AppErrorUi.kt`), so every case is handled exhaustively in one place.
 */
sealed interface AppError {

    /** Failures of a network request, thrown as [RequestException]. */
    sealed interface Request : AppError {
        val endpoint: Endpoint
    }

    /** No network connectivity at all. */
    data class Offline(override val endpoint: Endpoint) : Request

    /** The server couldn't be reached. */
    data class Unreachable(override val endpoint: Endpoint) : Request

    data class Timeout(override val endpoint: Endpoint, val seconds: Int) : Request

    /** The server answered with a non-success HTTP status. */
    data class HttpStatus(override val endpoint: Endpoint, val code: Int) : Request

    /** The response body was empty or couldn't be read. */
    data class ReadFailed(override val endpoint: Endpoint) : Request

    /** The response was read but couldn't be parsed. */
    data class InvalidResponse(override val endpoint: Endpoint) : Request

    data class UnknownRequest(override val endpoint: Endpoint) : Request

    /** The locally cached static data is missing or corrupt. */
    data object DataLoad : AppError

    /** An internal failure described by a developer-facing [message] (e.g. cache I/O). */
    data class Internal(val message: String) : AppError

    /** Anything that doesn't fit a more specific case. */
    data object Unknown : AppError
}
