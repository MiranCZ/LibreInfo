package io.github.mirancz.libreinfo.activity.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import io.github.mirancz.libreinfo.R

@Composable
fun ConfirmDialog(
    title: String,
    dismissText: String,
    confirmText: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    ConfirmDialog(
        title, dismissText, confirmText, onDismiss, onDismissClick = onDismiss, onConfirm, content
    )
}

@Composable
fun ConfirmDialog(
    title: String,
    dismissText: String,
    confirmText: String,
    onDismiss: () -> Unit,
    onDismissClick: () -> Unit,
    onConfirm: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Container {
            Column {
                Text(
                    title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = colorResource(R.color.secondaryColor)
                )

                Spacer(Modifier.height(12.dp))

                content()

                Spacer(Modifier.height(16.dp))

                Row(Modifier.fillMaxWidth()) {
                    SecondaryTextButton(dismissText, onClick = onDismissClick, modifier = Modifier.weight(1f))
                    Spacer(Modifier.width(16.dp))
                    PrimaryTextButton(confirmText, onClick = onConfirm, modifier = Modifier.weight(1f))
                }
            }

        }
    }
}