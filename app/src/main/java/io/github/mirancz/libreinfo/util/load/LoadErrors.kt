package io.github.mirancz.libreinfo.util.load

import io.github.mirancz.libreinfo.exception.AppError
import io.github.mirancz.libreinfo.exception.AppException
import io.github.mirancz.libreinfo.util.AppLog

/**
 * Turns any failure of a load into a user-presentable [AppException].
 *
 * An [AppException] is returned as-is, or unwrapped when carried as a cause (e.g. StorageInitException)
 * so the real, user-facing message survives instead of a generic one. Anything else is logged and
 * wrapped in a generic [AppException].
 */
fun Throwable.toAppException(): AppException {
    val appException = generateSequence(this) { it.cause }.filterIsInstance<AppException>().firstOrNull()
    if (appException != null) return appException

    AppLog.e("load", "Unexpected error while loading", this)
    return AppException(AppError.Unknown, this)
}

/** The [AppError] behind any failure of a load, see [toAppException]. */
fun Throwable.toAppError(): AppError = toAppException().error
