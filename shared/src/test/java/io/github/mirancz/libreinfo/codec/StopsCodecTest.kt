package io.github.mirancz.libreinfo.codec

import io.github.mirancz.libreinfo.codec.util.AppInputStream
import io.github.mirancz.libreinfo.codec.util.CheckedOutputStream
import io.github.mirancz.libreinfo.model.Stop
import io.github.mirancz.libreinfo.model.types.Location
import io.github.mirancz.libreinfo.model.types.ParentStopStation
import io.github.mirancz.libreinfo.model.types.PostType
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

class StopsCodecTest {

    @Test
    fun roundTripTest() {
        val input = listOf(
            Stop(0, "Hello", ParentStopStation(0, 213, PostType.PLATFORM), Location(10.0, 20.0)),
            Stop(1000, "ěščřžýáíébye0", ParentStopStation(1000, 41, PostType.PLATFORM), Location(15.5, 16.5)),
            Stop(4104, "World", ParentStopStation(4104, 100, PostType.POST), Location(-15.5, -16.5)),
        )

        val backed =  ByteArrayOutputStream()
        val os = CheckedOutputStream(backed)

        StopsCodec.write(os, input)

        val inp = AppInputStream(ByteArrayInputStream(backed.toByteArray()))

        val output = StopsCodec.read(inp)

        assertEquals(input, output)
    }

}