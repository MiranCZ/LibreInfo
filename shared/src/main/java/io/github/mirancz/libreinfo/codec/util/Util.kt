package io.github.mirancz.libreinfo.codec.util

inline fun <reified T> writeMapPool(map: Map<T, Int>, sizeWriter: (Int) -> Unit, valueWriter: (T) -> Unit) {
    val ts = Array<T?>(map.size)  { null }

    for (e in map.entries) {
       ts[e.value] = e.key
    }

    sizeWriter(map.size)

    for (t in ts) {
        valueWriter(t!!)
    }
}

fun Int.toCheckedShort(): Short {
    val v: Int = this

    check(!(v > Short.MAX_VALUE || v < Short.MIN_VALUE)) { "Failed to cast $v as short" }

    return v.toShort()
}

fun Int.toCheckedByte(): Byte {
    val v: Int = this

    check(!(v > Byte.MAX_VALUE || v < Byte.MIN_VALUE)) { "Failed to cast $v as byte" }

    return v.toByte()
}

fun Int.toCheckedUByte(): UByte {
    val v: Int = this

    check(!(v > 255 || v < 0)) { "Failed to cast $v as unsigned byte" }

    return v.toUByte()
}
;