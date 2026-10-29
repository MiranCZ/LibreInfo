package io.github.mirancz.libreinfo.ui.components

import android.text.TextUtils
import android.text.method.LinkMovementMethod
import android.widget.TextView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.viewinterop.NoOpUpdate
import io.github.mirancz.libreinfo.util.HtmlHelper

/**
 * Renders [htmlString] into a [android.widget.TextView].
 *
 * [maxLines] clips the text to that many lines and ellipsizes it. The ellipsis is only ever
 * drawn by a `StaticLayout`, and `TextView` switches to a `DynamicLayout` as soon as its text
 * is a [android.text.Spannable] - which attaching a movement method forces it to be. A
 * `DynamicLayout` ignores `maxLines` completely and `TextView` falls back to just cropping its
 * own height, so the text ends up clipped with no ellipsis. Hence, the movement method is only
 * attached while the text is shown in full; links inside a clipped preview are not tappable.
 */
@Composable
fun HTML(
    htmlString: String,
    modifier: Modifier = Modifier.Companion,
    maxLines: Int = Int.MAX_VALUE,
    update: (TextView) -> Unit = NoOpUpdate
) {
    val parsed = remember(htmlString) { HtmlHelper.parseHtml(htmlString) }
    val clipped = maxLines != Int.MAX_VALUE

    AndroidView(
        modifier = modifier,
        factory = { context -> TextView(context) },
        update = { view ->
            view.movementMethod = if (clipped) null else LinkMovementMethod.getInstance()
            view.maxLines = maxLines
            view.ellipsize = if (clipped) TextUtils.TruncateAt.END else null

            // has to come after the movement method - TextView decides how to buffer the text,
            // and with that which layout class to use, while the text is being set
            view.text = parsed

            update(view)
        }
    )
}