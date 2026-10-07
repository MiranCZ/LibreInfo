package io.github.mirancz.libreinfo.exception

/** An [AppException] for a failed network request */
class RequestException @JvmOverloads constructor(
    override val error: AppError.Request,
    cause: Throwable? = null
) : AppException(error, cause)
