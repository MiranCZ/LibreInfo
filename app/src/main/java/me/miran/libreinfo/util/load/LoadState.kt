package me.miran.libreinfo.util.load

import me.miran.libreinfo.exception.AppException

sealed interface LoadState<out T> {
    data object Loading : LoadState<Nothing>
    data class Success<T>(val data: T) : LoadState<T>
    data class Error(val error: AppException) : LoadState<Nothing>
}
