package io.github.mirancz.request.util

/**
 * This is different than [java.util.Optional] because [Present] may contain `null` values (when ID we want to map is `null` as well, for example)
 */
sealed class Mapped<T> {

    companion object {

        @JvmStatic
        fun <T> of(value: T): Mapped<T> {
            if (value == null) return Empty()
            return Present(value)
        }
    }

    inline fun orElse(defaultValue: () -> T): T =
        when (this) {
            is Empty<T> -> defaultValue()
            is Present<T> -> value
        }

    fun orElseThrow(): T =
        when (this) {
            is Empty<T> -> throw NoSuchElementException("No value present")
            is Present<T> -> value
        }

    fun isEmpty(): Boolean =
        when (this) {
            is Empty<T> -> true
            is Present<T> -> false
        }


    class Empty<T> : Mapped<T>()

    data class Present<T>(val value: T) : Mapped<T>()

}
