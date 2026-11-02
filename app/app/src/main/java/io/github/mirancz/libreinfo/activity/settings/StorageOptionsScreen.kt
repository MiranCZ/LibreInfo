package io.github.mirancz.libreinfo.activity.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.mirancz.libreinfo.BuildConfig
import io.github.mirancz.libreinfo.R
import io.github.mirancz.libreinfo.nav.NavState
import io.github.mirancz.libreinfo.parsing.storage.manager.StorageManager
import io.github.mirancz.libreinfo.ui.ScreenScaffold
import io.github.mirancz.libreinfo.ui.components.Container
import io.github.mirancz.libreinfo.util.UpdateDownloader
import java.nio.file.Files
import java.nio.file.Path
import kotlin.math.max

@Composable
fun StorageOptionsScreen(state: NavState) {
    val context = LocalContext.current
    val updateDir = UpdateDownloader.getUpdatesDir(context)
    val dataDir = StorageManager.getDataDir(context)

    val updateSize = getDirSize(updateDir)
    val dataSize = getDirSize(dataDir)

    val other =
        getDirSize(context.filesDir.toPath(), updateDir, dataDir) +
                getDirSize(context.noBackupFilesDir.toPath(), updateDir, dataDir)

    ScreenScaffold(stringResource(R.string.storage_options), onBack = state.onBack) {
        Container(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            Column() {
                SizeGraph(updateSize, dataSize, other)

                Spacer(Modifier.height(8.dp))

                Column(verticalArrangement = spacedBy(4.dp)) {
                    SizeEntry(stringResource(R.string.storage_data), dataSize, MaterialTheme.colorScheme.primary)

                    @Suppress("KotlinConstantConditions")
                    if (BuildConfig.AUTO_UPDATE_ENABLED) {
                        SizeEntry(stringResource(R.string.storage_autoupdate), updateSize, Color.Cyan)
                    }

                    SizeEntry(stringResource(R.string.storage_other), other, MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun SizeEntry(name: String, size: Long, color: Color) {
    Row(horizontalArrangement = Arrangement.Center) {
        Row(Modifier.weight(0.5f)) {
            Box(Modifier.size(16.dp).background(color, CircleShape))

            Spacer(Modifier.width(4.dp))
            Text(name)
        }

        Text(formatBytes(size))
    }
}

@Composable
private fun SizeGraph(updateSize: Long, dataSize: Long, otherSize: Long) {
    val total = updateSize + dataSize + otherSize

    val minSize = total * 0.025f

    Row(Modifier.height(12.dp).clip(RoundedCornerShape(8.dp))) {
        if (dataSize > 0) {
            Box(
                Modifier
                    .fillMaxHeight()
                    .weight(max(dataSize.toFloat(), minSize))
                    .background(MaterialTheme.colorScheme.primary)
            )
        }

        if (updateSize > 0) {
            Box(
                Modifier
                    .fillMaxHeight()
                    .weight(max(updateSize.toFloat(), minSize))
                    .background(Color.Cyan)
            )
        }

        if (otherSize > 0) {
            Box(
                Modifier
                    .fillMaxHeight()
                    .weight(max(otherSize.toFloat(), minSize))
                    .background(MaterialTheme.colorScheme.onSurfaceVariant)
            )
        }

        Box(
            Modifier
                .fillMaxHeight()
                .weight(1 + total.toFloat() * 0.3f)
                .background(MaterialTheme.colorScheme.background)
        )
    }
    Box(Modifier.background(Color.Gray)) {

    }
}

private fun formatBytes(bytes: Long): String {
    if (bytes < 100) {
        return "${bytes}B"
    }
    if (bytes < 100_000) {
        return "${"%,.2f".format(bytes / 1_000.0)}KB"
    }
    if (bytes < 100_000_000) {
        return "${"%,.2f".format(bytes / 1_000_000.0)}MB"
    }

    return "${"%,.2f".format(bytes / 1_000_000_000.0)}GB"
}

private fun getDirSize(path: Path, vararg ignore: Path): Long {
    for (p in ignore) {
        if (path.toAbsolutePath() == p.toAbsolutePath()) return 0L
    }
    if (ignore.contains(path)) {
        return 0L
    }

    if (path.toFile().isFile) {
        return Files.size(path)
    }
    if (!path.toFile().exists()) {
        return 0L
    }

    var size = 0L

    for (p in Files.list(path)) {
        size += getDirSize(p, *ignore)
    }

    return size;
}