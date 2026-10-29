package io.github.mirancz.libreinfo.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.mirancz.libreinfo.R
import io.github.mirancz.libreinfo.exception.AppError
import io.github.mirancz.libreinfo.ui.type
import io.github.mirancz.libreinfo.ui.userMessage

@Composable
fun ErrorWidget(error: AppError, modifier: Modifier = Modifier, onRetry: (() -> Unit)? = null) {
    val context = LocalContext.current
    val type = error.type

    Box(modifier.fillMaxWidth()) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                painter = painterResource(type.icon),
                "error",
                tint = MaterialTheme.colorScheme.error
            )

            Text(
                stringResource(type.title),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                error.userMessage(context),
                fontSize = 24.sp,
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Center
            )


            if (onRetry != null && type.retryable) {
                AppButton(
                    onClick = onRetry,
                    modifier = Modifier.padding(top = 16.dp).fillMaxWidth(0.5f),
                ) {
                    Text(
                        stringResource(R.string.retry),
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}