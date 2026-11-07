package io.github.mirancz.gtfs.export.writer

import io.github.mirancz.gtfs.ParsedGtfs
import io.github.mirancz.gtfs.export.ParseUtil.parseRoute
import io.github.mirancz.gtfs.export.SingleWriter
import io.github.mirancz.libreinfo.codec.LinesCodec
import io.github.mirancz.libreinfo.codec.util.CheckedOutputStream
import io.github.mirancz.libreinfo.model.LineAlias
import java.awt.Color

class LineInfoWriter : SingleWriter("lines") {

    override fun write(gtfs: ParsedGtfs, os: CheckedOutputStream) {
        val routes = gtfs.routes

        val mapped = routes.map { r ->
            val id = parseRoute(r.route_id)

            var backgroundColorStr: String = r.route_color
            if (backgroundColorStr.isBlank()) backgroundColorStr = "00E68C"

            var textColorStr: String = r.route_text_color
            if (textColorStr.isBlank()) textColorStr = "000000"

            val backgroundColor = parseColor(backgroundColorStr)
            val textColor = parseColor(textColorStr)

            LineAlias(
                id,
                r.route_short_name,
                backgroundColor.rgb,
                textColor.rgb
            )
        }

        LinesCodec.write(os, mapped)
    }

    private fun parseColor(color: String): Color {
        require(color.length == 6) { "Unexpected color argument: $color" }

        return Color(
            color.substring(0, 2).toInt(16),
            color.substring(2, 4).toInt(16),
            color.substring(4, 6).toInt(16)
        )
    }


}