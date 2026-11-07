package io.github.mirancz.libreinfo.codec

import io.github.mirancz.libreinfo.codec.util.AppInputStream
import io.github.mirancz.libreinfo.codec.util.CheckedOutputStream
import io.github.mirancz.libreinfo.model.LineAlias
import java.awt.Color
import java.io.IOException

object LinesCodec {

    @JvmStatic
    @Throws(IOException::class)
    fun write(os: CheckedOutputStream, lines: List<LineAlias>) {
        os.writeInt(lines.size)

        for (e in lines) {
            val backgroundColor = Color(e.backgroundColor)
            val textColor = Color(e.textColor)

            os.writeShort(e.id)
            os.writeString(e.displayName)

            os.writeColor(backgroundColor)
            os.writeColor(textColor)
        }
    }

    @JvmStatic
    @Throws(IOException::class)
    fun read(inp: AppInputStream): List<LineAlias> {
        val length = inp.readInt()

        val result = ArrayList<LineAlias>()

        for (i in 0 until length) {
            result.add(LineAlias(
                inp.readShort(),
                inp.readString(),
                inp.readColor().rgb,
                inp.readColor().rgb
            ))
        }

        return result
    }


}