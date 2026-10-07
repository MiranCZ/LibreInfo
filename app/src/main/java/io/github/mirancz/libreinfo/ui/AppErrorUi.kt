package io.github.mirancz.libreinfo.ui

import android.content.Context
import io.github.mirancz.libreinfo.R
import io.github.mirancz.libreinfo.exception.AppError
import io.github.mirancz.libreinfo.exception.ErrorType

/** The category the error UI uses to pick an icon, heading and retry affordance. */
val AppError.type: ErrorType
    get() = when (this) {
        is AppError.Offline -> ErrorType.OFFLINE
        is AppError.Unreachable, is AppError.Timeout, is AppError.HttpStatus -> ErrorType.SERVER
        is AppError.ReadFailed, is AppError.InvalidResponse -> ErrorType.PARSE
        is AppError.DataLoad -> ErrorType.DATA
        is AppError.UnknownRequest, is AppError.Internal, is AppError.Unknown -> ErrorType.GENERIC
    }

fun AppError.userMessage(context: Context): String = when (this) {
    is AppError.Offline -> context.getString(R.string.error_reach, endpointName(context))
    is AppError.Unreachable -> context.getString(R.string.error_reach, endpointName(context))
    is AppError.Timeout -> context.getString(R.string.error_timeout, seconds, endpointName(context))
    is AppError.HttpStatus -> context.getString(R.string.error_server, code, endpointName(context))
    is AppError.ReadFailed -> context.getString(R.string.error_read, endpointName(context))
    is AppError.InvalidResponse -> context.getString(R.string.error_parse, endpointName(context))
    is AppError.UnknownRequest -> context.getString(R.string.error_unknown, endpointName(context))
    is AppError.DataLoad -> context.getString(R.string.data_load_error)
    is AppError.Internal -> message
    is AppError.Unknown -> context.getString(R.string.generic_error)
}

private fun AppError.Request.endpointName(context: Context): String = endpoint.name.getName(context)
