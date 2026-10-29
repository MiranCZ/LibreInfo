package io.github.mirancz.libreinfo.exception

/** Carries an [AppError] through code that fails by throwing. */
open class AppException @JvmOverloads constructor(
    open val error: AppError,
    cause: Throwable? = null
) : Exception(error.toString(), cause) {

    companion object {
        /** The locally cached static data couldn't be loaded. */
        @JvmStatic
        fun dataLoad(cause: Throwable): AppException = AppException(AppError.DataLoad, cause)
    }
}
