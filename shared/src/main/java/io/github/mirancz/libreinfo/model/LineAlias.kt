package io.github.mirancz.libreinfo.model

import io.github.mirancz.libreinfo.codec.util.toCheckedShort
import kotlinx.serialization.Serializable
import java.awt.Color
import java.util.Locale
import java.util.Optional
import java.util.function.Function

@Serializable
data class LineAlias(
    val id: Short,
    val displayName: String,
    val backgroundColor: Int,
    val textColor: Int
) : Comparable<LineAlias> {

    constructor(id: Int, displayName: String, backgroundColor: Int, textColor: Int) : this(
        id.toCheckedShort(), displayName, backgroundColor, textColor
    )

    fun backgroundColorStr(): String {
        return Color(backgroundColor).asRgbString()
    }

    fun textColorStr(): String {
        return Color(textColor).asRgbString()
    }

    private fun Color.asRgbString(): String {
        val red = red
        val green = green
        val blue = blue

        return "#${red.toHex()}${green.toHex()}${blue.toHex()}"
    }

    private fun Int.toHex(): String {
        var v = toString(16)

        if (v.length < 2) {
           v = "0$v"
        }

        return v
    }

    // TODO this should probably be used in all usages of some line lists
    //  (rather than trusting the API to sort them)
    override fun compareTo(other: LineAlias): Int {
        // TODO compare info could be cached
        return getCompareInfo().compareTo(other.getCompareInfo())
    }

    /**
     * Tries to generate at least somewhat sensible sorting rules for the lines from their names rather than IDs
     */
    private fun getCompareInfo(): CompareInfo {
        var name: String = displayName.lowercase(Locale.getDefault())

        if (name == "x") {
            return CompareInfo(19, LineComparePower.UNKNOWN)
        }
        if (name == "loď") {
            return CompareInfo(100, LineComparePower.UNKNOWN)
        }

        var power = LineComparePower.UNKNOWN

        // power pass
        if (name.startsWith("x")) {
            power = LineComparePower.SUBSTITUTE
            name = name.substring(1)
        } else if (name.startsWith("p")) {
            power = LineComparePower.EXTRA
            name = name.substring(1)
        } else if (name.startsWith("e")) {
            power = LineComparePower.EXPRESS
            name = name.substring(1)
        } else if (isInteger(displayName)) {
            power = LineComparePower.PRIMARY
        }

        // offset pass
        var offset = 0

        var resolved = false
        // night lines
        if (name.startsWith("n")) {
            name = name.substring(1)
            resolved = true
        } else if (name.startsWith("š")) {
            name = name.substring(1)
            resolved = true
        } else if (name.startsWith("r") || name.startsWith("s")) {
            offset = 10000

            name = name.substring(1)
            resolved = true
        }

        if (resolved && power == LineComparePower.UNKNOWN) {
            power = LineComparePower.PRIMARY
        }

        if (isInteger(name)) {
            return CompareInfo(name.toInt() + offset, power)
        }

        // safety fallback where removing the first letter makes a valid line number
        if (isInteger(name.substring(1))) {
            power = LineComparePower.UNKNOWN
            name = name.substring(1)

            return CompareInfo(name.toInt() + offset, power)
        }

        // TODO logging
//        AppLog.w("Failed to generate comparison for '"+lineDisplayName+"'");
        return CompareInfo(id + offset, LineComparePower.UNKNOWN)
    }

    @Suppress("CheckResult")
    private fun isInteger(str: String): Boolean {
        try {
            Integer.parseInt(str)
            return true
        } catch (_: NumberFormatException) {
            return false
        }
    }

    private data class CompareInfo(val number: Int, val power: LineComparePower) : Comparable<CompareInfo> {
        override fun compareTo(other: CompareInfo): Int {
            if (number == other.number) {
                return power.compareTo(other.power)
            }

            return number.compareTo(other.number)
        }
    }

    private enum class LineComparePower : Comparable<LineComparePower> {
        /** normal lines */
        PRIMARY,

        /**  express lines (eq. E75) */
        EXPRESS,

        /** lines used to help with diversions (eq. x10) */
        SUBSTITUTE,

        /** lines used for helping with traffic (eq. P1) */
        EXTRA,

        /**  other or unknown */
        UNKNOWN
    }

}