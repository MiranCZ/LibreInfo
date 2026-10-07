package io.github.mirancz.libreinfo.util.load

import io.github.mirancz.libreinfo.exception.AppError

sealed interface LoadState<out T> {
    data object Loading : LoadState<Nothing>
    data class Success<T>(val data: T) : LoadState<T>
    data class Error(val error: AppError) : LoadState<Nothing>
}
