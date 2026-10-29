package io.github.mirancz.libreinfo.ui

import android.content.Context
import androidx.compose.material3.SnackbarHostState
import io.github.mirancz.libreinfo.activity.base.snackbar.CustomSnackBarVisuals
import io.github.mirancz.libreinfo.activity.base.snackbar.SnackBarType
import io.github.mirancz.libreinfo.exception.AppError


suspend fun SnackbarHostState.showInfo(text: String) {
    show(text, SnackBarType.INFO)
}

suspend fun SnackbarHostState.showSuccess(text: String) {
    show(text, SnackBarType.SUCCESS)
}

suspend fun SnackbarHostState.showError(text: String) {
    show(text, SnackBarType.ERROR)
}


suspend fun SnackbarHostState.show(text: String, type: SnackBarType) {
    showSnackbar(
        CustomSnackBarVisuals(
            text,
            type = type
        )
    )
}

suspend fun SnackbarHostState.showError(context: Context, error: AppError) {
    showSnackbar(
        CustomSnackBarVisuals(
            error.userMessage(context),
            type = SnackBarType.ERROR
        )
    )
}